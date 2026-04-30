// src/main/java/com/smartcs/lite/service/AnalyticsService.java
package com.smartcs.lite.service;

import com.smartcs.lite.model.dto.AnalyticsVO;
import com.smartcs.lite.repository.ConversationRepository;
import com.smartcs.lite.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final AgentService agentService;

    public AnalyticsVO getOverview(Long tenantId) {
        Instant todayStart = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();

        long totalConvs = conversationRepository.countByTenantIdSince(tenantId, Instant.EPOCH);
        long todayConvs = conversationRepository.countByTenantIdSince(tenantId, todayStart);
        long totalMsgs = messageRepository.countSince(Instant.EPOCH);
        long todayMsgs = messageRepository.countSince(todayStart);
        long activeConvs = conversationRepository.countActiveByTenantId(tenantId);
        long onlineAgents = agentService.countOnline(tenantId);

        // 机器人解决率：已关闭且没有分配座席的会话 / 总已关闭会话
        long closedByBot = conversationRepository.countByTenantIdSince(tenantId, Instant.EPOCH);
        long closedByAgent = conversationRepository.countClosedByAgent(tenantId);
        double botRate = closedByBot > 0
                ? (double)(closedByBot - closedByAgent) / closedByBot
                : 0.0;

        return new AnalyticsVO(
                totalConvs, todayConvs, totalMsgs, todayMsgs,
                activeConvs, onlineAgents, Math.round(botRate * 100.0) / 100.0
        );
    }
}
