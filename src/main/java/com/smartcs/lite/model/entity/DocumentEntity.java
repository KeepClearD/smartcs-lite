package com.smartcs.lite.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "document")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "kb_id", nullable = false)
    private Long kbId;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(name = "file_key", length = 512)
    private String fileKey;

    @Column(name = "file_type", length = 16)
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(nullable = false, length = 16)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "chunk_count")
    @Builder.Default
    private Integer chunkCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
