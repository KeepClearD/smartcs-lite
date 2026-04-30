package com.smartcs.lite.repository;

import com.smartcs.lite.model.entity.ChatMemoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChatMemoryRepository extends JpaRepository<ChatMemoryEntity, Long> {

    @Query(value = "SELECT * FROM chat_memory WHERE conversation_id = :convId " +
            "ORDER BY created_at DESC LIMIT :limit", nativeQuery = true)
    List<ChatMemoryEntity> findRecent(Long convId, int limit);

    @Modifying
    @Transactional
    void deleteByConversationId(Long conversationId);
}
