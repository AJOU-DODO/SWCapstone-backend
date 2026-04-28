package com.dodo.dodoserver.domain.nest.service;

import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisViewCountService {

    private final StringRedisTemplate redisTemplate;
    private final NestRepository nestRepository;

    private static final String VIEW_USER_KEY = "nest:view:%d:user:%d";
    private static final String VIEW_COUNT_KEY = "nest:viewCount:%d";
    private static final String UPDATED_NESTS_KEY = "nest:updatedViews";

    /**
     * 조회수 증가 로직 (중복 방지 포함)
     */
    public void incrementViewCount(Long nestId, Long userId) {
        String userKey = String.format(VIEW_USER_KEY, nestId, userId);
        
        // 해당 유저가 이 둥지를 24시간 내에 조회한 적이 있는지 확인
        Boolean isFirstView = redisTemplate.opsForValue().setIfAbsent(userKey, "v", Duration.ofDays(1));

        if (Boolean.TRUE.equals(isFirstView)) {
            // 중복이 아닐 경우 조회수 카운터 증가
            String countKey = String.format(VIEW_COUNT_KEY, nestId);
            redisTemplate.opsForValue().increment(countKey);
            // 업데이트 대상 둥지 목록(Set)에 추가
            redisTemplate.opsForSet().add(UPDATED_NESTS_KEY, String.valueOf(nestId));
        }
    }

    /**
     * Redis의 현재 증가분 조회
     */
    public Long getCachedViewCount(Long nestId) {
        String countKey = String.format(VIEW_COUNT_KEY, nestId);
        String count = redisTemplate.opsForValue().get(countKey);
        return count != null ? Long.parseLong(count) : 0L;
    }

    /**
     * 10분마다 Redis의 조회수 증가분을 DB에 반영
     */
    @Scheduled(cron = "0 0/10 * * * *")
    @Transactional
    public void syncViewCountToDb() {
        Set<String> updatedNestIds = redisTemplate.opsForSet().members(UPDATED_NESTS_KEY);
        if (updatedNestIds == null || updatedNestIds.isEmpty()) {
            return;
        }

        log.info("Redis 조회수 DB 동기화 시작: {} 건", updatedNestIds.size());

        for (String nestIdStr : updatedNestIds) {
            Long nestId = Long.parseLong(nestIdStr);
            String countKey = String.format(VIEW_COUNT_KEY, nestId);
            
            // 값을 가져오는 동시에 삭제하여 원자성 확보 시도 (getAndDelete는 Spring Data Redis 2.6+ 지원)
            String countStr = redisTemplate.opsForValue().getAndDelete(countKey);
            
            if (countStr != null) {
                Long increment = Long.parseLong(countStr);
                nestRepository.incrementViewCount(nestId, increment);
            }
            
            // 동기화 완료된 nestId를 Set에서 제거
            redisTemplate.opsForSet().remove(UPDATED_NESTS_KEY, nestIdStr);
        }
        
        log.info("Redis 조회수 DB 동기화 완료");
    }
}
