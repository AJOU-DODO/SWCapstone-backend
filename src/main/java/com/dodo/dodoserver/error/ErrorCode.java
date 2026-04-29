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

    // Auth (A)
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "A001", "유효하지 않은 리프레시 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A002", "DB에 존재하지 않는 토큰입니다. 다시 로그인해주세요."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A003", "인증이 필요한 서비스입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    USER_PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "사용자 프로필을 찾을 수 없습니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "U003", "이미 사용 중인 닉네임입니다."),
    ALREADY_ONBOARDED(HttpStatus.BAD_REQUEST, "U004", "이미 온보딩이 완료된 사용자입니다."),
    ONBOARDING_REQUIRED(HttpStatus.BAD_REQUEST, "U005", "온보딩이 완료되지 않은 사용자입니다."),

    // Category
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CA001", "카테고리를 찾을 수 없습니다."),
    DUPLICATE_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "CA002", "이미 존재하는 카테고리 이름입니다."),

    // Nest (N)
    NEST_NOT_FOUND(HttpStatus.NOT_FOUND, "N001", "둥지를 찾을 수 없습니다."),
    OUT_OF_UNLOCK_RADIUS(HttpStatus.BAD_REQUEST, "N002", "해당 위치에서 너무 멀어 해금할 수 없습니다."),
    ALREADY_UNLOCKED(HttpStatus.BAD_REQUEST, "N003", "이미 해금된 둥지입니다."),
    NOT_NEST_CREATOR(HttpStatus.FORBIDDEN, "N004", "둥지 작성자만 권한이 있습니다."),
    DRAFT_NOT_FOUND(HttpStatus.NOT_FOUND, "N005", "임시 저장된 둥지를 찾을 수 없습니다."),
    DRAFT_NOT_PUBLISHABLE(HttpStatus.BAD_REQUEST, "N006", "필수 정보(제목, 내용, 반경, 카테고리)가 누락되어 발행할 수 없습니다."),
    NEST_NOT_UNLOCKED(HttpStatus.FORBIDDEN, "N007", "해당 둥지의 상세 내용을 보려면 해금이 필요합니다."),

    // Postcard (PC)
    POSTCARD_NOT_FOUND(HttpStatus.NOT_FOUND, "PC001", "엽서를 찾을 수 없습니다."),
    NOT_POSTCARD_OWNER(HttpStatus.FORBIDDEN, "PC002", "엽서의 소유자만 권한이 있습니다."),
    ALREADY_EXCHANGED(HttpStatus.BAD_REQUEST, "PC003", "이미 교환된 엽서는 재등록할 수 없습니다."),
    ALREADY_SHARED(HttpStatus.BAD_REQUEST, "PC004", "이미 둥지에 등록된 엽서입니다."),
    NOT_SHARED(HttpStatus.BAD_REQUEST, "PC005", "현재 공유 중인 상태가 아닙니다."),
    EXCHANGE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "PC006", "오늘의 엽서 교환 횟수를 모두 사용했습니다. (일일 3회)"),
    NO_AVAILABLE_POSTCARD_IN_NEST(HttpStatus.BAD_REQUEST, "PC007", "현재 둥지에 교환 가능한 엽서가 없습니다."),
    CANNOT_EXCHANGE_OWN_POSTCARD(HttpStatus.BAD_REQUEST, "PC008", "자신이 등록한 엽서는 교환할 수 없습니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;
}
