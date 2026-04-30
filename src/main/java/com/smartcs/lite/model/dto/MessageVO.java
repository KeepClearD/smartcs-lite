package com.smartcs.lite.model.dto;

import java.time.Instant;

public record MessageVO(
        Long id,
        Long conversationId,
        String senderType,
        Long senderId,
        String senderName,
        String msgType,
        String content,
        Instant createdAt
) {}
