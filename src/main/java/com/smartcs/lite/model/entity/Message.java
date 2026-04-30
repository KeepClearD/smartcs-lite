package com.smartcs.lite.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "message")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "sender_type", nullable = false, length = 16)
    private String senderType;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "msg_type", nullable = false, length = 16)
    @Builder.Default
    private String msgType = "TEXT";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private String metadata = "{}";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
