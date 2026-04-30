// src/main/java/com/smartcs/lite/websocket/ChatWebSocketHandler.java
package com.smartcs.lite.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcs.lite.model.dto.WsMessage;
import com.smartcs.lite.model.entity.Conversation;
import com.smartcs.lite.model.entity.Message;
import com.smartcs.lite.service.AiService;
import com.smartcs.lite.service.ConversationService;
import com.smartcs.lite.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final SessionManager sessionManager;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    // session → customerId 映射
    private final ConcurrentHashMap<String, String> sessionCustomerMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 从 URL 参数中获取 tenantId 和 customerId
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        String customerId = extractParam(query, "customerId");
        String tenantIdStr = extractParam(query, "tenantId");

        if (customerId == null || customerId.isBlank()) {
            customerId = "guest-" + UUID.randomUUID().toString().substring(0, 8);
        }

        Long tenantId = tenantIdStr != null ? Long.parseLong(tenantIdStr) : 1L;

        sessionCustomerMap.put(session.getId(), customerId);
        sessionManager.registerCustomer(customerId, session);

        // 创建或获取会话
        Conversation conv = conversationService.getOrCreate(customerId, tenantId);
        sessionManager.bindConversation(customerId, conv.getId());

        // 发送连接成功消息
        sessionManager.sendMessage(session, WsMessageWrapper.of(
                "connected", "连接成功", conv.getId()));

        // 发送欢迎语
        sessionManager.sendMessage(session, WsMessageWrapper.chat(
                "您好！我是智能客服助手，有什么可以帮您的吗？", conv.getId()));

        log.info("客户连接建立: customerId={}, convId={}", customerId, conv.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        try {
            WsMessage wsMsg = objectMapper.readValue(textMessage.getPayload(), WsMessage.class);

            // 使用虚拟线程处理
            Thread.startVirtualThread(() -> {
                try {
                    processMessage(session, wsMsg);
                } catch (Exception e) {
                    log.error("消息处理失败", e);
                    sessionManager.sendMessage(session,
                            WsMessageWrapper.error("消息处理失败，请重试"));
                }
            });

        } catch (Exception e) {
            log.error("消息解析失败", e);
            sessionManager.sendMessage(session, WsMessageWrapper.error("消息格式错误"));
        }
    }

    private void processMessage(WebSocketSession session, WsMessage wsMsg) {
        switch (wsMsg.type()) {
            case "chat" -> handleChat(session, wsMsg);
            case "transfer" -> handleTransfer(session, wsMsg);
            case "close" -> handleClose(session, wsMsg);
            case "satisfaction" -> handleSatisfaction(session, wsMsg);
            default -> log.warn("未知消息类型: {}", wsMsg.type());
        }
    }

    private void handleChat(WebSocketSession session, WsMessage wsMsg) {
        String customerId = sessionCustomerMap.get(session.getId());
        Long conversationId = wsMsg.conversationId() != null
                ? wsMsg.conversationId()
                : sessionManager.getConversationId(customerId);

        if (conversationId == null) {
            sessionManager.sendMessage(session,
                    WsMessageWrapper.error("会话不存在"));
            return;
        }

        Conversation conv = conversationService.getById(conversationId);
        Long tenantId = conv.getTenantId();

        // 保存用户消息
        messageService.save(conversationId, "CUSTOMER", wsMsg.content());

        if ("BOT".equals(conv.getStatus())) {
            // 机器人接待
            sessionManager.sendMessage(session,
                    WsMessageWrapper.typing(conversationId));

            String aiReply = aiService.chat(conversationId, tenantId, wsMsg.content());

            // 检查是否需要转人工
            if (aiReply.contains("[TRANSFER]")) {
                aiReply = aiReply.replace("[TRANSFER]", "").trim();
                messageService.save(conversationId, "BOT", aiReply);
                sessionManager.sendMessage(session,
                        WsMessageWrapper.chat(aiReply, conversationId));

                // 触发转人工
                conversationService.requestTransfer(conversationId);
                sessionManager.sendMessage(session,
                        WsMessageWrapper.status("正在为您转接人工客服，请稍候...",
                                conversationId, "PENDING"));

                // 通知在线座席
                notifyAgentsForTransfer(conv);
                return;
            }

            // 保存并发送 AI 回复
            messageService.save(conversationId, "BOT", aiReply);
            sessionManager.sendMessage(session,
                    WsMessageWrapper.chat(aiReply, conversationId));

        } else if ("AGENT".equals(conv.getStatus())) {
            // 人工接待：转发给座席
            if (conv.getAgentId() != null) {
                WebSocketSession agentSession = sessionManager.getAgentSession(conv.getAgentId());
                if (agentSession != null && agentSession.isOpen()) {
                    sessionManager.sendMessage(agentSession, new WsMessageWrapper(
                            "chat", wsMsg.content(), conversationId, null,
                            Map.of("from", "customer", "customerId", customerId)));
                }
            }
        } else if ("PENDING".equals(conv.getStatus())) {
            // 排队中
            sessionManager.sendMessage(session, WsMessageWrapper.chat(
                    "您正在排队等待人工客服，请耐心等候...", conversationId));
        }
    }

    private void handleTransfer(WebSocketSession session, WsMessage wsMsg) {
        String customerId = sessionCustomerMap.get(session.getId());
        Long conversationId = sessionManager.getConversationId(customerId);

        if (conversationId != null) {
            conversationService.requestTransfer(conversationId);
            sessionManager.sendMessage(session,
                    WsMessageWrapper.status("正在为您转接人工客服...",
                            conversationId, "PENDING"));
            notifyAgentsForTransfer(conversationService.getById(conversationId));
        }
    }

    private void handleClose(WebSocketSession session, WsMessage wsMsg) {
        String customerId = sessionCustomerMap.get(session.getId());
        Long conversationId = wsMsg.conversationId() != null
                ? wsMsg.conversationId()
                : sessionManager.getConversationId(customerId);

        if (conversationId != null) {
            conversationService.close(conversationId);
            sessionManager.sendMessage(session,
                    WsMessageWrapper.status("会话已结束", conversationId, "CLOSED"));
        }
    }

    private void handleSatisfaction(WebSocketSession session, WsMessage wsMsg) {
        String customerId = sessionCustomerMap.get(session.getId());
        Long conversationId = sessionManager.getConversationId(customerId);

        if (conversationId != null && wsMsg.metadata() != null) {
            Object scoreObj = wsMsg.metadata().get("score");
            if (scoreObj instanceof Number score) {
                conversationService.setSatisfaction(conversationId, score.shortValue());
                sessionManager.sendMessage(session,
                        WsMessageWrapper.of("status", "感谢您的评价！", conversationId));
            }
        }
    }

    /**
     * 通知座席有新的转人工请求
     */
    private void notifyAgentsForTransfer(Conversation conv) {
        // 简单实现：通知所有在线座席
        // 后续可改为按技能组路由
        sessionManager.sendMessage(
                sessionManager.getAgentSession(conv.getAgentId()),
                new WsMessageWrapper("transfer_request",
                        "有新的转人工请求", conv.getId(), null,
                        Map.of("customerName", conv.getCustomerName() != null
                                ? conv.getCustomerName() : "访客")));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String customerId = sessionCustomerMap.remove(session.getId());
        if (customerId != null) {
            sessionManager.unregisterCustomer(customerId);
        }
        log.info("客户连接关闭: {}", customerId);
    }

    private String extractParam(String query, String name) {
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                return kv[1];
            }
        }
        return null;
    }
}
