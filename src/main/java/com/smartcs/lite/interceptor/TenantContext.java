package com.smartcs.lite.interceptor;

public final class TenantContext {

    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();

    public static void set(Long tenantId) {
        CURRENT.set(tenantId);
    }

    public static Long get() {
        Long id = CURRENT.get();
        if (id == null) {
            return 1L; // 默认租户
        }
        return id;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
