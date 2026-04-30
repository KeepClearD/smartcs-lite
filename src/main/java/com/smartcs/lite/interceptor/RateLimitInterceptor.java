package com.smartcs.lite.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcs.lite.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${smartcs.ai.rate-limit.qps-per-user:2}")
    private int qpsPerUser;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader("X-User-Id");
        if (userId == null) userId = request.getRemoteAddr();

        String key = "smartcs:ratelimit:" + userId;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(1));
        }

        if (count != null && count > qpsPerUser) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(
                    Result.fail(40029, "请求过于频繁，请稍后再试")));
            return false;
        }

        return true;
    }
}
