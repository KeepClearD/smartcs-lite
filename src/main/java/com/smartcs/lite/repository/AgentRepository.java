package com.smartcs.lite.repository;

import com.smartcs.lite.model.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, Long> {

    List<Agent> findByTenantId(Long tenantId);

    Optional<Agent> findByTenantIdAndEmail(Long tenantId, String email);

    @Query("SELECT a FROM Agent a WHERE a.tenantId = :tenantId " +
            "AND a.status = 'ONLINE' AND a.currentLoad < a.maxConcurrent " +
            "ORDER BY a.currentLoad ASC")
    List<Agent> findAvailableAgents(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, String status);
}
