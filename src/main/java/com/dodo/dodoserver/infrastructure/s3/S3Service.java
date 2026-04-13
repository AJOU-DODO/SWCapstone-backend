package com.dodo.dodoserver.infrastructure.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.s3.cloudfront-domain}")
    private String cloudFrontDomain;

    /**
     * S3 업로드를 위한 Presigned URL을 생성합니다.
     * @param prefix 파일이 저장될 폴더 경로 (예: "profiles/")
     * @param fileName 원본 파일명
     * @return 생성된 Presigned URL과 최종 저장될 CloudFront 파일 주소
     */
    public PresignedUrlResponse getPresignedUrl(String prefix, String fileName) {
        String uniqueFileName = createUniqueFileName(fileName);
        String key = prefix + uniqueFileName;

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5)) // URL 유효 시간: 5분
                .putObjectRequest(objectRequest)
                .build();

        String presignedUrl = s3Presigner.presignPutObject(presignRequest).url().toString();
        
        // S3 버킷 주소 대신 CloudFront 도메인 주소로 최종 URL 생성
        String fileUrl = String.format("%s/%s", cloudFrontDomain, key);

        return new PresignedUrlResponse(presignedUrl, fileUrl);
    }

    private String createUniqueFileName(String fileName) {
        return UUID.randomUUID().toString() + "_" + fileName;
    }

    /**
     * 여러 개의 파일 업로드를 위한 Presigned URL 리스트를 생성합니다.
     */
    public java.util.List<PresignedUrlResponse> getPresignedUrls(String prefix, java.util.List<String> fileNames) {
        return fileNames.stream()
                .map(fileName -> getPresignedUrl(prefix, fileName))
                .collect(java.util.stream.Collectors.toList());
    }

    public record PresignedUrlResponse(String presignedUrl, String fileUrl) {}
}
