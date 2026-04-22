package com.dodo.dodoserver.domain.test;

import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.domain.auth.dto.TokenResponseDto;
import com.dodo.dodoserver.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dodo.dodoserver.infrastructure.fcm.FcmService;
import com.dodo.dodoserver.infrastructure.fcm.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestAuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final FcmService fcmService;

    /**
     * FCM 알림 전송 테스트 API
     * @param token 대상 기기의 FCM 토큰
     */
    @PostMapping("/fcm")
    @Transactional
    public ApiResponseDto<String> testFcm(@RequestParam String token) {
        NotificationEvent event = new NotificationEvent(
                List.of(token),
                "테스트 알림",
                "서버에서 보낸 테스트 메시지입니다.",
                Map.of("type", "TEST", "data", "hello")
        );


        eventPublisher.publishEvent(event);
        return ApiResponseDto.success("알림 전송 이벤트를 발행했습니다. 로그를 확인하세요.");
    }

    /**
     * 특정 이메일을 기반으로 테스트용 Access Token과 Refresh Token을 생성
     * @param email 토큰에 담을 사용자 이메일 (DB에 없어도 생성 가능)
     * @param role 사용자 권한 (기본값: ROLE_USER)
     */
    @GetMapping("/token")
    public ApiResponseDto<TokenResponseDto> createTestToken(
            @RequestParam String email,
            @RequestParam(defaultValue = "ROLE_USER") String role) {

        String accessToken = jwtTokenProvider.createAccessToken(1L, email, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        // 만료 시간은 임의로 1시간(3600L)으로 표시
        return ApiResponseDto.success(TokenResponseDto.of(accessToken, refreshToken, 3600L, false));
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Dodo! API is working.";
    }
}
