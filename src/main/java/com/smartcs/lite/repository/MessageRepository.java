// src/main/java/com/smartcs/lite/repository/MessageRepository.java
package com.smartcs.lite.repository;

import com.smartcs.lite.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;

public interface MessageRepository extends JpaRepository<Message, Long> {

    Page<Message> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.createdAt >= :since")
    long countSince(Instant since);

    @Query(value = "SELECT content FROM message WHERE conversation_id = :convId " +
            "ORDER BY created_at DESC LIMIT 1", nativeQuery = true)
    String findLastMessageContent(Long convId);
}
