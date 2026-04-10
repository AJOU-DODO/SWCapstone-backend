package com.dodo.dodoserver.global.common;

import org.apache.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 응답 규격 모든 API는 이 객체를 통해 결과를 반환
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto<T> {
    private String status;
    private String code;
    private String message;
    private T data;         // 실제 데이터 내용 (오류 시 null)


    public static <T> ApiResponseDto<T> success(T data) {
        return new ApiResponseDto<>("SUCCESS", String.valueOf(HttpStatus.SC_OK), null, data);
    }

    public static <T> ApiResponseDto<T> success(T data, String message) {
        return new ApiResponseDto<>("SUCCESS",String.valueOf(HttpStatus.SC_OK), message, data);
    }


    public static <T> ApiResponseDto<T> error(String code, String message) {
        return new ApiResponseDto<>("ERROR", code,message,null);
    }
}
