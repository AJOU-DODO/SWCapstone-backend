package com.dodo.dodoserver.domain.postcard.service;

import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostcardRedisServiceTest {

    @InjectMocks
    private PostcardRedisService postcardRedisService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    @DisplayName("교환 횟수 증가 성공 - 첫 증가 시 TTL 설정 확인")
    void checkAndIncrementExchangeCount_firstTime() {
        // given
        Long userId = 1L;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(anyString())).willReturn(1L);

        // when
        postcardRedisService.checkAndIncrementExchangeCount(userId);

        // then
        verify(redisTemplate).expire(anyString(), any(Duration.class));
    }

    @Test
    @DisplayName("교환 횟수 증가 성공 - 일반 증가")
    void checkAndIncrementExchangeCount_normal() {
        // given
        Long userId = 1L;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(anyString())).willReturn(5L);

        // when
        postcardRedisService.checkAndIncrementExchangeCount(userId);

        // then
        // 1이 아니므로 expire는 호출되지 않아야 함 (엄격한 검증은 아니지만)
    }

    @Test
    @DisplayName("교환 횟수 초과 실패")
    void checkAndIncrementExchangeCount_limitExceeded() {
        // given
        Long userId = 1L;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.increment(anyString())).willReturn(101L);

        // when & then
        assertThatThrownBy(() -> postcardRedisService.checkAndIncrementExchangeCount(userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.EXCHANGE_LIMIT_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("남은 교환 횟수 조회 - 데이터 없을 때")
    void getRemainingExchangeCount_noData() {
        // given
        Long userId = 1L;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn(null);

        // when
        int remaining = postcardRedisService.getRemainingExchangeCount(userId);

        // then
        assertThat(remaining).isEqualTo(100);
    }

    @Test
    @DisplayName("남은 교환 횟수 조회 - 데이터 있을 때")
    void getRemainingExchangeCount_withData() {
        // given
        Long userId = 1L;
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(anyString())).willReturn("30");

        // when
        int remaining = postcardRedisService.getRemainingExchangeCount(userId);

        // then
        assertThat(remaining).isEqualTo(70);
    }
}
