package com.dodo.dodoserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 응답 규격 모든 API는 이 객체를 통해 결과를 반환
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;  // "SUCCESS" 또는 "ERROR"
    private T data;         // 실제 데이터 내용 (오류 시 null)
    private String message; // 상세 메시지 (성공 시 null)


    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", data, null);
    }


    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("ERROR", null, message);
    }
}
