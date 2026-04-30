package com.smartcs.lite.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "agent")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 128)
    private String email;

    @Column(name = "password_hash", length = 256)
    private String passwordHash;

    @Column(nullable = false, length = 16)
    @Builder.Default
    private String role = "AGENT";

    @Column(nullable = false, length = 16)
    @Builder.Default
    private String status = "OFFLINE";

    @Column(name = "max_concurrent", nullable = false)
    @Builder.Default
    private Integer maxConcurrent = 5;

    @Column(name = "current_load", nullable = false)
    @Builder.Default
    private Integer currentLoad = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
