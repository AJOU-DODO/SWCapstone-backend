package com.dodo.dodoserver.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 전역적으로 사용할 에러 코드들을 관리하는 Enum
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Global
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "G001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "G002", "허용되지 않은 메소드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "G003", "서버 내부 오류입니다."),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "G004", "접근 권한이 없습니다."),
    INPUT_VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "G005", "입력 데이터 유효성 검증에 실패했습니다."), // @Valid 관련 예외 추가

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    USER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "사용자 프로필을 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "U003", "이미 사용 중인 닉네임입니다."),
    ALREADY_ONBOARDED(HttpStatus.BAD_REQUEST, "U004", "이미 온보딩이 완료된 사용자입니다."),
    ONBOARDING_REQUIRED(HttpStatus.BAD_REQUEST, "U005", "온보딩이 완료되지 않은 사용자입니다."),

    // Category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CA001", "카테고리를 찾을 수 없습니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;
}
