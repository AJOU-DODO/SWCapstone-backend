package com.dodo.dodoserver.domain.test;

import com.dodo.dodoserver.global.common.ApiResponseDto;
import com.dodo.dodoserver.domain.auth.dto.TokenResponseDto;
import com.dodo.dodoserver.global.security.JwtTokenProvider;
import com.dodo.dodoserver.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.dodo.dodoserver.infrastructure.fcm.FcmService;
import com.dodo.dodoserver.infrastructure.fcm.NotificationEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final TestService testService;

    /**
     * 회원 하드 삭제 (테스트용)
     * 입력받은 userId에 해당하는 사용자의 모든 데이터를 DB에서 영구 삭제합니다.
     */
    @DeleteMapping("/user/hard")
    public ApiResponseDto<String> hardDeleteUser(@RequestParam Long userId) {
        testService.hardDeleteUser(userId);
        return ApiResponseDto.success("사용자(ID: " + userId + ") 데이터가 완전히 삭제되었습니다.");
    }

    /**
     * FCM 알림 전송 테스트 API
     */
    @PostMapping("/fcm")
    public ApiResponseDto<String> testFcm(@RequestParam String token) {
        NotificationEvent event = new NotificationEvent(
                List.of(token),
                "테스트 알림",
                "서버에서 보낸 테스트 메시지입니다.",
                Map.of("type", "TEST", "data", "hello")
        );
        eventPublisher.publishEvent(event);
        return ApiResponseDto.success("알림 전송 이벤트를 발행했습니다.");
    }

    /**
     * 테스트용 토큰 생성
     */
    @GetMapping("/token")
    public ApiResponseDto<TokenResponseDto> createTestToken(
            @RequestParam Long id,
            @RequestParam String email,
            @RequestParam(defaultValue = "ROLE_USER") String role) {

        String accessToken = jwtTokenProvider.createAccessToken(id, email, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        return ApiResponseDto.success(TokenResponseDto.of(accessToken, refreshToken, 3600L, false));
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Dodo! Test API is working.";
    }
}
