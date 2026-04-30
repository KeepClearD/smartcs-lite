package com.smartcs.lite.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WsMessage(
        String type,
        String content,
        Long conversationId,
        String requestId,
        Map<String, Object> metadata
) {}
