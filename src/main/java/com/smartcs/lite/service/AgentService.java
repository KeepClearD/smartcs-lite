// src/main/java/com/smartcs/lite/service/AgentService.java
package com.smartcs.lite.service;

import com.smartcs.lite.model.entity.Agent;
import com.smartcs.lite.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;

    public List<Agent> list(Long tenantId) {
        return agentRepository.findByTenantId(tenantId);
    }

    public Agent create(Long tenantId, String name, String email, String role) {
        return agentRepository.save(Agent.builder()
                .tenantId(tenantId)
                .name(name)
                .email(email)
                .role(role != null ? role : "AGENT")
                .build());
    }

    public void updateStatus(Long agentId, String status) {
        Agent agent = agentRepository.findById(agentId).orElseThrow();
        agent.setStatus(status);
        agentRepository.save(agent);
    }

    public List<Agent> findAvailable(Long tenantId) {
        return agentRepository.findAvailableAgents(tenantId);
    }

    public long countOnline(Long tenantId) {
        return agentRepository.countByTenantIdAndStatus(tenantId, "ONLINE");
    }
}
