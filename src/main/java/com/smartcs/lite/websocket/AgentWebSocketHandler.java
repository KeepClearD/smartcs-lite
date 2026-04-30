// src/main/java/com/smartcs/lite/websocket/AgentWebSocketHandler.java
package com.smartcs.lite.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcs.lite.model.dto.WsMessage;
import com.smartcs.lite.model.entity.Conversation;
import com.smartcs.lite.service.AgentService;
import com.smartcs.lite.service.ConversationService;
import com.smartcs.lite.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AgentWebSocketHandler extends TextWebSocketHandler {

    private final SessionManager sessionManager;
    private final ConversationService conversationService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    private final AgentService agentService;

    private final ConcurrentHashMap<String, Long> sessionAgentMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : "";
        String agentIdStr = extractParam(query, "agentId");

        if (agentIdStr == null) {
            sessionManager.sendMessage(session, WsMessageWrapper.error("缺少 agentId 参数"));
            return;
        }

        Long agentId = Long.parseLong(agentIdStr);
        sessionAgentMap.put(session.getId(), agentId);
        sessionManager.registerAgent(agentId, session);

        // 更新座席状态为在线
        agentService.updateStatus(agentId, "ONLINE");

        sessionManager.sendMessage(session,
                WsMessageWrapper.of("connected", "座席连接成功", null));
        log.info("座席连接建立: agentId={}", agentId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) {
        try {
            WsMessage wsMsg = objectMapper.readValue(textMessage.getPayload(), WsMessage.class);

            Thread.startVirtualThread(() -> {
                try {
                    processMessage(session, wsMsg);
                } catch (Exception e) {
                    log.error("座席消息处理失败", e);
                }
            });

        } catch (Exception e) {
            log.error("座席消息解析失败", e);
        }
    }

    private void processMessage(WebSocketSession session, WsMessage wsMsg) {
        Long agentId = sessionAgentMap.get(session.getId());

        switch (wsMsg.type()) {
            case "chat" -> handleAgentChat(session, agentId, wsMsg);
            case "accept" -> handleAccept(session, agentId, wsMsg);
            case "transfer" -> handleTransfer(session, agentId, wsMsg);
            case "close" -> handleClose(session, agentId, wsMsg);
            case "status" -> handleStatusUpdate(session, agentId, wsMsg);
            default -> log.warn("未知座席消息类型: {}", wsMsg.type());
        }
    }

    private void handleAgentChat(WebSocketSession session, Long agentId, WsMessage wsMsg) {
        Long conversationId = wsMsg.conversationId();

        // 保存座席消息
        messageService.save(conversationId, "AGENT", wsMsg.content());

        // 转发给客户
        Conversation conv = conversationService.getById(conversationId);
        WebSocketSession customerSession = sessionManager.getCustomerSession(conv.getCustomerId());
        if (customerSession != null && customerSession.isOpen()) {
            sessionManager.sendMessage(customerSession,
                    WsMessageWrapper.chat(wsMsg.content(), conversationId));
        }
    }

    private void handleAccept(WebSocketSession session, Long agentId, WsMessage wsMsg) {
        // 座席接手会话
        conversationService.assignAgent(wsMsg.conversationId(), agentId);

        // 通知客户
        Conversation conv = conversationService.getById(wsMsg.conversationId());
        WebSocketSession customerSession = sessionManager.getCustomerSession(conv.getCustomerId());
        if (customerSession != null && customerSession.isOpen()) {
            sessionManager.sendMessage(customerSession,
                    WsMessageWrapper.status("人工客服已接入", conv.getId(), "AGENT"));
        }

        // 确认座席
        sessionManager.sendMessage(session,
                WsMessageWrapper.of("accepted", "已接手会话", wsMsg.conversationId()));
    }

    private void handleTransfer(WebSocketSession session, Long agentId, WsMessage wsMsg) {
        // 转接给其他座席（简单实现：转回机器人）
        conversationService.getById(wsMsg.conversationId()); // 验证会话存在
        // 后续实现转接给指定座席
    }

    private void handleClose(WebSocketSession session, Long agentId, WsMessage wsMsg) {
        conversationService.close(wsMsg.conversationId());

        // 通知客户
        Conversation conv = conversationService.getById(wsMsg.conversationId());
        WebSocketSession customerSession = sessionManager.getCustomerSession(conv.getCustomerId());
        if (customerSession != null && customerSession.isOpen()) {
            sessionManager.sendMessage(customerSession,
                    WsMessageWrapper.status("会话已结束，感谢您的咨询！",
                            conv.getId(), "CLOSED"));
        }
    }

    private void handleStatusUpdate(WebSocketSession session, Long agentId, WsMessage wsMsg) {
        if (wsMsg.metadata() != null) {
            String status = (String) wsMsg.metadata().get("status");
            if (status != null) {
                agentService.updateStatus(agentId, status);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long agentId = sessionAgentMap.remove(session.getId());
        if (agentId != null) {
            sessionManager.unregisterAgent(agentId);
            agentService.updateStatus(agentId, "OFFLINE");
        }
        log.info("座席连接关闭: agentId={}", agentId);
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
