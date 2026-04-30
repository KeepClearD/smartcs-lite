package com.smartcs.lite.config;

import com.smartcs.lite.websocket.ChatWebSocketHandler;
import com.smartcs.lite.websocket.AgentWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final AgentWebSocketHandler agentWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 客户端聊天连接
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .setAllowedOriginPatterns("*");

        // 座席工作台连接
        registry.addHandler(agentWebSocketHandler, "/ws/agent")
                .setAllowedOriginPatterns("*");
    }
}
