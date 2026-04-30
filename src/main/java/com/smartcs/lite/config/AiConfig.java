package com.smartcs.lite.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                    你是一个专业的智能客服助手。
                    请根据知识库内容回答用户问题，回答简洁、专业、友好。
                    如果知识库中没有相关信息，请诚实告知并建议转人工。
                    当用户明确要求转人工时，回复：好的，正在为您转接人工客服，请稍候。[TRANSFER]
                    """)
                .build();
    }
}
