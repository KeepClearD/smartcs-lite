package com.smartcs.lite.controller;

import com.smartcs.lite.common.Result;
import com.smartcs.lite.interceptor.TenantContext;
import com.smartcs.lite.model.dto.ConversationVO;
import com.smartcs.lite.model.dto.MessageVO;
import com.smartcs.lite.service.ConversationService;
import com.smartcs.lite.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final MessageService messageService;

    @GetMapping
    public Result<Page<ConversationVO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long tenantId = TenantContext.get();
        return Result.ok(conversationService.list(tenantId, status, PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public Result<ConversationVO> get(@PathVariable Long id) {
        var conv = conversationService.getById(id);
        return Result.ok(new ConversationVO(
                conv.getId(), conv.getTenantId(),
                conv.getCustomerName(), conv.getCustomerId(),
                conv.getChannel(), conv.getAgentId(), null,
                conv.getStatus(), conv.getSubject(), conv.getSatisfaction(),
                conv.getCreatedAt(), conv.getUpdatedAt(), conv.getClosedAt(),
                null
        ));
    }

    @GetMapping("/{id}/messages")
    public Result<Page<MessageVO>> messages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return Result.ok(messageService.getByConversation(id, PageRequest.of(page, size)));
    }

    @PutMapping("/{id}/assign")
    public Result<Void> assign(@PathVariable Long id, @RequestParam Long agentId) {
        conversationService.assignAgent(id, agentId);
        return Result.ok();
    }

    @PutMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        conversationService.close(id);
        return Result.ok();
    }
}
