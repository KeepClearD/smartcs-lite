package com.smartcs.lite.service;

import com.smartcs.lite.model.dto.MessageVO;
import com.smartcs.lite.model.entity.Message;
import com.smartcs.lite.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    /**
     * 保存消息
     */
    public Message save(Long conversationId, String senderType, String content) {
        return messageRepository.save(Message.builder()
                .conversationId(conversationId)
                .senderType(senderType)
                .content(content)
                .build());
    }

    /**
     * 获取会话历史消息
     */
    public Page<MessageVO> getByConversation(Long conversationId, Pageable pageable) {
        return messageRepository
                .findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(this::toVO);
    }

    private MessageVO toVO(Message msg) {
        return new MessageVO(
                msg.getId(), msg.getConversationId(),
                msg.getSenderType(), msg.getSenderId(),
                null, msg.getMsgType(), msg.getContent(),
                msg.getCreatedAt()
        );
    }
}
