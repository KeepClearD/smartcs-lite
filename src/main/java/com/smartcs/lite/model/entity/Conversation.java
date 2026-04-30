package com.smartcs.lite.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "conversation")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "customer_name", length = 64)
    private String customerName;

    @Column(name = "customer_id", length = 128)
    private String customerId;

    @Column(nullable = false, length = 16)
    @Builder.Default
    private String channel = "WEB";

    @Column(name = "agent_id")
    private Long agentId;

    @Column(nullable = false, length = 16)
    @Builder.Default
    private String status = "BOT";

    @Column(length = 256)
    private String subject;

    private Short satisfaction;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "closed_at")
    private Instant closedAt;
}
