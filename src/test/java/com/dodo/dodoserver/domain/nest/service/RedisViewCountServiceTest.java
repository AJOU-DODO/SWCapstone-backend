package com.dodo.dodoserver.domain.nest.service;

import com.dodo.dodoserver.domain.ad.dao.NestAdInfoRepository;
import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisViewCountServiceTest {

    @InjectMocks
    private RedisViewCountService redisViewCountService;

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private NestRepository nestRepository;
    @Mock
    private NestAdInfoRepository nestAdInfoRepository;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private SetOperations<String, String> setOperations;

    @BeforeEach
    void setUp() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(redisTemplate.opsForSet()).willReturn(setOperations);
    }

    @Test
    @DisplayName("광고 노출수 증가 - Redis 저장 확인")
    void incrementAdImpressionCount_success() {
        // given
        List<Long> nestIds = List.of(100L, 200L);

        // when
        redisViewCountService.incrementAdImpressionCount(nestIds);

        // then
        verify(valueOperations).increment("nest:ad:impressionCount:100");
        verify(setOperations).add("nest:ad:updatedImpressions", "100");
        verify(valueOperations).increment("nest:ad:impressionCount:200");
        verify(setOperations).add("nest:ad:updatedImpressions", "200");
    }

    @Test
    @DisplayName("광고 노출수 DB 동기화 성공")
    void syncAdImpressionCountToDb_success() {
        // given
        String updatedImpressionsKey = "nest:ad:updatedImpressions";
        given(setOperations.members("nest:updatedViews")).willReturn(Set.of());
        given(setOperations.members("nest:ad:updatedClicks")).willReturn(Set.of());
        given(setOperations.members(updatedImpressionsKey)).willReturn(Set.of("100"));
        given(valueOperations.getAndDelete("nest:ad:impressionCount:100")).willReturn("5");

        // when
        redisViewCountService.syncToDb();

        // then
        verify(nestAdInfoRepository).incrementImpressionsBatch(100L, 5L);
        verify(setOperations).remove(updatedImpressionsKey, "100");
    }
}
