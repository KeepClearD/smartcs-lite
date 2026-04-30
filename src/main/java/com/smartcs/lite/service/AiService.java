// src/main/java/com/smartcs/lite/service/AiService.java
package com.smartcs.lite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final RagService ragService;
    private final ChatMemoryService chatMemoryService;
    private final CacheService cacheService;

    /**
     * 处理用户消息，返回 AI 回复
     */
    public String chat(Long conversationId, Long tenantId, String userMessage) {
        // 1. 检查缓存
        String cached = cacheService.getIfCached(tenantId, userMessage);
        if (cached != null) {
            log.debug("缓存命中: {}", userMessage);
            return cached;
        }

        // 2. RAG 检索知识上下文
        String context = ragService.retrieveContext(tenantId, userMessage);

        // 3. 获取对话历史
        List<Message> history = chatMemoryService.getRecentMessages(conversationId);

        // 4. 构建消息列表
        List<Message> messages = new ArrayList<>();

        // 系统提示词（含知识库上下文）
        String systemPrompt = buildSystemPrompt(context);
        messages.add(new SystemMessage(systemPrompt));
        messages.addAll(history);
        messages.add(new UserMessage(userMessage));

        // 5. 调用 LLM
        try {
            ChatResponse response = chatClient
                    .prompt()
                    .messages(messages)
                    .call()
                    .chatResponse();

            String answer = response.getResult().getOutput().getText();

            // 6. 保存对话记忆
            chatMemoryService.save(conversationId, "user", userMessage);
            chatMemoryService.save(conversationId, "assistant", answer);

            // 7. 缓存结果
            cacheService.cache(tenantId, userMessage, answer);

            return answer;
        } catch (Exception e) {
            log.error("AI 调用失败", e);
            return "抱歉，系统暂时繁忙，请稍后再试或转接人工客服。";
        }
    }

    /**
     * 流式回答（SSE）
     */
    public Flux<String> chatStream(Long conversationId, Long tenantId, String userMessage) {
        String context = ragService.retrieveContext(tenantId, userMessage);
        String systemPrompt = buildSystemPrompt(context);

        return chatClient
                .prompt()
                .system(systemPrompt)
                .user(userMessage)
                .stream()
                .content();
    }

    private String buildSystemPrompt(String context) {
        if (context == null || context.isBlank()) {
            return """
                你是一个专业的智能客服助手。请根据你的知识礼貌地回答用户问题。
                回答简洁明了，一般不超过200字。
                如果你不确定答案，请告知用户你将转接人工客服。
                当用户要求转人工时，请回复：好的，正在为您转接人工客服，请稍候。[TRANSFER]
                """;
        }

        return """
            你是一个专业的智能客服助手。请严格根据以下知识库内容回答用户问题。

            【知识库内容】
            %s

            【回答规则】
            1. 优先使用知识库中的内容回答
            2. 如果知识库中没有相关信息，礼貌告知并建议转人工
            3. 回答简洁明了，一般不超过200字
            4. 不要编造知识库中不存在的信息
            5. 当用户明确要求转人工时，回复：好的，正在为您转接人工客服，请稍候。[TRANSFER]
            """.formatted(context);
    }
}
