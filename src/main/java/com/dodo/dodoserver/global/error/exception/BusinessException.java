package com.dodo.dodoserver.global.error.exception;

import com.dodo.dodoserver.global.error.ErrorCode;
import lombok.Getter;

/**
 * 프로젝트 내 비즈니스 로직 상의 오류를 처리하기 위한 부모 예외 클래스
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
