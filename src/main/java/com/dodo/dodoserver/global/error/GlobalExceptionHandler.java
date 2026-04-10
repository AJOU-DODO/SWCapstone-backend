package com.dodo.dodoserver.global.error;

import java.util.Objects;

import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.global.error.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 모든 컨트롤러의 예외를 전역적으로 잡아 처리하는 클래스
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 로직 상의 커스텀 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponseDto<Void>> handleBusinessException(BusinessException e) {
        log.warn("handleBusinessException: {}", e.getErrorCode());
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDto.error(e.getErrorCode().getCode(), e.getMessage()));
    }

    /**
     * Valid 검증 실패 시 발생하는 예외를 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponseDto<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.warn("ValidationException in field: {}", Objects.requireNonNull(e.getBindingResult().getFieldError()).getField());
        return ResponseEntity
            .status(ErrorCode.INPUT_VALIDATION_ERROR.getStatus())
            .body(ApiResponseDto.error(ErrorCode.INPUT_VALIDATION_ERROR.getCode(), ErrorCode.INPUT_VALIDATION_ERROR.getMessage()));
    }

    /**
     * 그 외 정의되지 않은 예외 처리 (예: NPE, DB 에러 등)
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponseDto<Void>> handleException(Exception e) {
        log.warn("handleException", e);
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponseDto.error(errorCode.getCode(), errorCode.getMessage()));
    }
}
