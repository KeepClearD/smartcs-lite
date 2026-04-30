package com.smartcs.lite.common;

public enum ErrorCode {
    PARAM_ERROR(40001, "参数错误"),
    NOT_FOUND(40004, "资源不存在"),
    UNAUTHORIZED(40001, "未授权"),
    RATE_LIMITED(40029, "请求过于频繁，请稍后再试"),
    AI_ERROR(50001, "AI 服务异常"),
    INTERNAL_ERROR(50000, "系统内部错误");

    public final int code;
    public final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() { return code; }
    public String message() { return message; }
}
