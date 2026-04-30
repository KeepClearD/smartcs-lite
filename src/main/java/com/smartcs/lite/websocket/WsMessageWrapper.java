package com.smartcs.lite.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * WebSocket 消息包装器
 */
public record WsMessageWrapper(
        String type,
        String content,
        Long conversationId,
        String requestId,
        Map<String, Object> metadata
) {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public String toJson() {
        try {
            return MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{\"type\":\"error\",\"content\":\"序列化失败\"}";
        }
    }

    public static WsMessageWrapper of(String type, String content, Long conversationId) {
        return new WsMessageWrapper(type, content, conversationId, null, null);
    }

    public static WsMessageWrapper chat(String content, Long conversationId) {
        return new WsMessageWrapper("chat", content, conversationId, null, null);
    }

    public static WsMessageWrapper typing(Long conversationId) {
        return new WsMessageWrapper("typing", "", conversationId, null, null);
    }

    public static WsMessageWrapper status(String content, Long conversationId, String status) {
        return new WsMessageWrapper("status", content, conversationId, null,
                Map.of("status", status));
    }

    public static WsMessageWrapper error(String content) {
        return new WsMessageWrapper("error", content, null, null, null);
    }
}
