package com.smartcs.lite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final VectorStore vectorStore;
    private final JdbcTemplate jdbcTemplate;

    @Value("${smartcs.ai.rag.top-k:3}")
    private int topK;

    @Value("${smartcs.ai.rag.similarity-threshold:0.5}")
    private double similarityThreshold;

    /**
     * 检索相关知识上下文
     */
    public String retrieveContext(Long tenantId, String question) {
        // 1. PGvector 向量检索
        List<Document> vectorResults = vectorSearch(tenantId, question);

        if (vectorResults.isEmpty()) {
            return "";
        }

        return vectorResults.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n---\n"));
    }

    /**
     * PGvector 向量检索
     */
    private List<Document> vectorSearch(Long tenantId, String question) {
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(question)
                    .topK(topK)
                    .similarityThreshold(similarityThreshold)
                    .build();

            return vectorStore.similaritySearch(request);
        } catch (Exception e) {
            log.warn("向量检索失败，降级为空结果: {}", e.getMessage());
            return List.of();
        }
    }
}
