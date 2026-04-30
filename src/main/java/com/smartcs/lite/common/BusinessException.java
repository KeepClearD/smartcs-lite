package com.smartcs.lite.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.code = errorCode.code();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
