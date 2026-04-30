package com.smartcs.lite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final StringRedisTemplate redisTemplate;

    @Value("${smartcs.ai.cache.ttl-seconds:7200}")
    private long ttlSeconds;

    public String getIfCached(Long tenantId, String question) {
        String key = buildKey(tenantId, question);
        return redisTemplate.opsForValue().get(key);
    }

    public void cache(Long tenantId, String question, String answer) {
        String key = buildKey(tenantId, question);
        redisTemplate.opsForValue().set(key, answer, Duration.ofSeconds(ttlSeconds));
    }

    private String buildKey(Long tenantId, String question) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(question.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
            String hex = bytesToHex(hash).substring(0, 16);
            return "smartcs:cache:" + tenantId + ":" + hex;
        } catch (Exception e) {
            return "smartcs:cache:" + tenantId + ":" + Math.abs(question.hashCode());
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
