package com.smartcs.lite.model.dto;

import java.time.Instant;

public record ConversationVO(
        Long id,
        Long tenantId,
        String customerName,
        String customerId,
        String channel,
        Long agentId,
        String agentName,
        String status,
        String subject,
        Short satisfaction,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt,
        String lastMessage
) {}
