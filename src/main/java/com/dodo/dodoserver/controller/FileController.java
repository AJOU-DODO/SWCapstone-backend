package com.dodo.dodoserver.controller;

import com.dodo.dodoserver.dto.ApiResponseDto;
import com.dodo.dodoserver.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    /**
     * 프로필 이미지 업로드를 위한 Presigned URL을 요청합니다.
     * @param fileName 업로드할 파일명
     */
    @GetMapping("/presigned-url/profile")
    public ApiResponseDto<S3Service.PresignedUrlResponse> getProfilePresignedUrl(@RequestParam String fileName) {
        return ApiResponseDto.success(s3Service.getPresignedUrl("profiles/", fileName));
    }
}
