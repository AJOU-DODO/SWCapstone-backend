package com.dodo.dodoserver.domain.postcard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PostcardRedisService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "postcard_exchange:date:";
    private static final int MAX_EXCHANGE_COUNT = 3;

    public void checkAndIncrementExchangeCount(Long userId) {
        String key = generateKey(userId);
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            // 처음 생성된 키인 경우 자정까지의 TTL 설정
            redisTemplate.expire(key, getDurationUntilMidnight());
        }

        if (count > MAX_EXCHANGE_COUNT) {
            // 롤백은 따로 하지 않음 (이미 3을 넘었으므로)
            throw new com.dodo.dodoserver.error.exception.BusinessException(com.dodo.dodoserver.error.ErrorCode.EXCHANGE_LIMIT_EXCEEDED);
        }
    }

    public int getRemainingExchangeCount(Long userId) {
        String key = generateKey(userId);
        String countStr = redisTemplate.opsForValue().get(key);
        if (countStr == null) {
            return MAX_EXCHANGE_COUNT;
        }
        return Math.max(0, MAX_EXCHANGE_COUNT - Integer.parseInt(countStr));
    }

    private String generateKey(Long userId) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return KEY_PREFIX + date + ":user:" + userId;
    }

    private Duration getDurationUntilMidnight() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().atTime(LocalTime.MAX);
        return Duration.between(now, midnight);
    }
}
