package com.smartcs.lite.controller;

import com.smartcs.lite.common.Result;
import com.smartcs.lite.interceptor.TenantContext;
import com.smartcs.lite.model.entity.Agent;
import com.smartcs.lite.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @GetMapping
    public Result<List<Agent>> list() {
        return Result.ok(agentService.list(TenantContext.get()));
    }

    @PostMapping
    public Result<Agent> create(@RequestBody Map<String, String> body) {
        return Result.ok(agentService.create(
                TenantContext.get(),
                body.get("name"),
                body.get("email"),
                body.get("role")
        ));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        agentService.updateStatus(id, status);
        return Result.ok();
    }
}
