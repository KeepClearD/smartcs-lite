package com.smartcs.lite.service;

import com.smartcs.lite.model.entity.ChatMemoryEntity;
import com.smartcs.lite.repository.ChatMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMemoryService {

    private final ChatMemoryRepository repository;

    @Value("${smartcs.ai.memory.max-messages:10}")
    private int maxMessages;

    public void save(Long conversationId, String role, String content) {
        repository.save(ChatMemoryEntity.builder()
                .conversationId(conversationId)
                .role(role)
                .content(content)
                .build());
    }

    public List<Message> getRecentMessages(Long conversationId) {
        List<ChatMemoryEntity> entities = repository.findRecent(conversationId, maxMessages);
        List<Message> messages = new ArrayList<>();
        // 倒序回来变成时间正序
        for (int i = entities.size() - 1; i >= 0; i--) {
            messages.add(toMessage(entities.get(i)));
        }
        return messages;
    }

    public void clear(Long conversationId) {
        repository.deleteByConversationId(conversationId);
    }

    private Message toMessage(ChatMemoryEntity entity) {
        return switch (entity.getRole()) {
            case "user" -> new UserMessage(entity.getContent());
            case "assistant" -> new AssistantMessage(entity.getContent());
            case "system" -> new SystemMessage(entity.getContent());
            default -> new UserMessage(entity.getContent());
        };
    }
}
