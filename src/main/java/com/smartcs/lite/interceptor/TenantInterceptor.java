package com.smartcs.lite.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader("X-Tenant-Id");
        if (tenantId != null && !tenantId.isBlank()) {
            TenantContext.set(Long.parseLong(tenantId));
        } else {
            TenantContext.set(1L); // 默认租户
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}
