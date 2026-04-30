package com.smartcs.lite.service;

import com.smartcs.lite.common.BusinessException;
import com.smartcs.lite.common.ErrorCode;
import com.smartcs.lite.interceptor.TenantContext;
import com.smartcs.lite.model.dto.ConversationVO;
import com.smartcs.lite.model.entity.Agent;
import com.smartcs.lite.model.entity.Conversation;
import com.smartcs.lite.model.entity.Message;
import com.smartcs.lite.repository.AgentRepository;
import com.smartcs.lite.repository.ConversationRepository;
import com.smartcs.lite.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AgentRepository agentRepository;

    /**
     * 获取或创建会话
     */
    @Transactional
    public Conversation getOrCreate(String customerId, Long tenantId) {
        return conversationRepository
                .findFirstByCustomerIdAndTenantIdAndStatusNot(customerId, tenantId, "CLOSED")
                .orElseGet(() -> conversationRepository.save(Conversation.builder()
                        .tenantId(tenantId)
                        .customerId(customerId)
                        .customerName("访客-" + customerId.substring(0, 6))
                        .status("BOT")
                        .build()));
    }

    /**
     * 获取会话列表
     */
    public Page<ConversationVO> list(Long tenantId, String status, Pageable pageable) {
        Page<Conversation> page;
        if (status != null && !status.isBlank()) {
            page = conversationRepository.findByTenantIdAndStatusOrderByUpdatedAtDesc(
                    tenantId, status, pageable);
        } else {
            page = conversationRepository.findByTenantIdOrderByUpdatedAtDesc(tenantId, pageable);
        }

        List<ConversationVO> vos = page.getContent().stream()
                .map(this::toVO)
                .toList();

        return new PageImpl<>(vos, pageable, page.getTotalElements());
    }

    /**
     * 获取会话详情
     */
    public Conversation getById(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    /**
     * 座席接手会话
     */
    @Transactional
    public void assignAgent(Long conversationId, Long agentId) {
        Conversation conv = getById(conversationId);
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        conv.setAgentId(agentId);
        conv.setStatus("AGENT");
        conversationRepository.save(conv);

        agent.setCurrentLoad(agent.getCurrentLoad() + 1);
        if (agent.getCurrentLoad() >= agent.getMaxConcurrent()) {
            agent.setStatus("BUSY");
        }
        agentRepository.save(agent);

        log.info("会话 {} 已分配给座席 {}", conversationId, agentId);
    }

    /**
     * 请求转人工
     */
    @Transactional
    public void requestTransfer(Long conversationId) {
        Conversation conv = getById(conversationId);
        conv.setStatus("PENDING");
        conversationRepository.save(conv);

        log.info("会话 {} 请求转人工", conversationId);
    }

    /**
     * 关闭会话
     */
    @Transactional
    public void close(Long conversationId) {
        Conversation conv = getById(conversationId);

        if (conv.getAgentId() != null) {
            Agent agent = agentRepository.findById(conv.getAgentId()).orElse(null);
            if (agent != null) {
                agent.setCurrentLoad(Math.max(0, agent.getCurrentLoad() - 1));
                if (agent.getCurrentLoad() < agent.getMaxConcurrent()
                        && "BUSY".equals(agent.getStatus())) {
                    agent.setStatus("ONLINE");
                }
                agentRepository.save(agent);
            }
        }

        conv.setStatus("CLOSED");
        conv.setClosedAt(Instant.now());
        conversationRepository.save(conv);

        log.info("会话 {} 已关闭", conversationId);
    }

    /**
     * 设置满意度
     */
    @Transactional
    public void setSatisfaction(Long conversationId, short score) {
        Conversation conv = getById(conversationId);
        conv.setSatisfaction(score);
        conversationRepository.save(conv);
    }

    private ConversationVO toVO(Conversation conv) {
        String lastMessage = messageRepository.findLastMessageContent(conv.getId());
        String agentName = null;
        if (conv.getAgentId() != null) {
            agentName = agentRepository.findById(conv.getAgentId())
                    .map(Agent::getName).orElse(null);
        }
        return new ConversationVO(
                conv.getId(), conv.getTenantId(),
                conv.getCustomerName(), conv.getCustomerId(),
                conv.getChannel(), conv.getAgentId(), agentName,
                conv.getStatus(), conv.getSubject(), conv.getSatisfaction(),
                conv.getCreatedAt(), conv.getUpdatedAt(), conv.getClosedAt(),
                lastMessage
        );
    }
}
