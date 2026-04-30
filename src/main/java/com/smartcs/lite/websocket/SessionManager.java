package com.smartcs.lite.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class SessionManager {

    // 客户端连接: customerId → session
    private final ConcurrentHashMap<String, WebSocketSession> customerSessions = new ConcurrentHashMap<>();

    // 座席连接: agentId → session
    private final ConcurrentHashMap<Long, WebSocketSession> agentSessions = new ConcurrentHashMap<>();

    // 客户端 → 会话映射: customerId → conversationId
    private final ConcurrentHashMap<String, Long> customerConvMap = new ConcurrentHashMap<>();

    // ==================== 客户端 ====================

    public void registerCustomer(String customerId, WebSocketSession session) {
        customerSessions.put(customerId, session);
        log.debug("客户连接注册: {}", customerId);
    }

    public void unregisterCustomer(String customerId) {
        customerSessions.remove(customerId);
        customerConvMap.remove(customerId);
        log.debug("客户连接注销: {}", customerId);
    }

    public WebSocketSession getCustomerSession(String customerId) {
        return customerSessions.get(customerId);
    }

    public void bindConversation(String customerId, Long conversationId) {
        customerConvMap.put(customerId, conversationId);
    }

    public Long getConversationId(String customerId) {
        return customerConvMap.get(customerId);
    }

    // ==================== 座席 ====================

    public void registerAgent(Long agentId, WebSocketSession session) {
        agentSessions.put(agentId, session);
        log.debug("座席连接注册: agentId={}", agentId);
    }

    public void unregisterAgent(Long agentId) {
        agentSessions.remove(agentId);
        log.debug("座席连接注销: agentId={}", agentId);
    }

    public WebSocketSession getAgentSession(Long agentId) {
        return agentSessions.get(agentId);
    }

    // ==================== 工具方法 ====================

    public void sendMessage(WebSocketSession session, WsMessageWrapper msg) {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new org.springframework.web.socket.TextMessage(
                        msg.toJson()));
            } catch (Exception e) {
                log.error("消息发送失败", e);
            }
        }
    }
}
