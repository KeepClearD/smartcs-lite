package com.smartcs.lite.repository;

import com.smartcs.lite.model.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Page<Conversation> findByTenantIdOrderByUpdatedAtDesc(Long tenantId, Pageable pageable);

    Page<Conversation> findByTenantIdAndStatusOrderByUpdatedAtDesc(
            Long tenantId, String status, Pageable pageable);

    Optional<Conversation> findFirstByCustomerIdAndTenantIdAndStatusNot(
            String customerId, Long tenantId, String status);

    List<Conversation> findByAgentIdAndStatus(Long agentId, String status);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.tenantId = :tenantId " +
            "AND c.createdAt >= :since")
    long countByTenantIdSince(Long tenantId, Instant since);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.tenantId = :tenantId " +
            "AND c.status <> 'CLOSED'")
    long countActiveByTenantId(Long tenantId);

    @Query("SELECT COUNT(c) FROM Conversation c WHERE c.tenantId = :tenantId " +
            "AND c.status = 'CLOSED' AND c.agentId IS NOT NULL")
    long countClosedByAgent(Long tenantId);
}
