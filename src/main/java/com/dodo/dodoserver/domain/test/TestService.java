package com.dodo.dodoserver.domain.test;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

    private final EntityManager entityManager;
    private final StringRedisTemplate redisTemplate;

    /**
     * 사용자와 관련된 모든 데이터를 하드 삭제 (Native SQL 사용으로 Soft Delete 및 FK 제약 무시 시도)
     */
    @Transactional
    public void hardDeleteUser(Long userId) {
        log.info("회원 하드 삭제 시작: userId={}", userId);

        // Redis 리프레시 토큰 삭제 (Key: refreshToken:userId)
        redisTemplate.delete("refreshToken:" + userId);

        // 1. 댓글 및 좋아요 정리
        // MySQL Error 1093 방지를 위해 서브쿼리를 한 번 더 감싸서 임시 테이블로 처리
        entityManager.createNativeQuery("UPDATE nest_comments SET parent_id = NULL WHERE parent_id IN (SELECT * FROM (SELECT id FROM nest_comments WHERE user_id = :userId) AS temp)")
                .setParameter("userId", userId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM comment_likes WHERE user_id = :userId").setParameter("userId", userId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM nest_comments WHERE user_id = :userId").setParameter("userId", userId).executeUpdate();

        // 2. 반응 및 해금 기록 정리
        entityManager.createNativeQuery("DELETE FROM nest_reactions WHERE user_id = :userId").setParameter("userId", userId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM unlock_histories WHERE user_id = :userId").setParameter("userId", userId).executeUpdate();

        // 3. 사용자가 만든 둥지 관련 전체 삭제
        // 둥지에 달린 타인의 댓글/좋아요/반응 먼저 삭제
        String nestIdSubQuery = "(SELECT id FROM nests WHERE creator_id = :userId)";
        entityManager.createNativeQuery("DELETE FROM comment_likes WHERE comment_id IN (SELECT id FROM nest_comments WHERE nest_id IN " + nestIdSubQuery + ")")
                .setParameter("userId", userId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM nest_comments WHERE nest_id IN " + nestIdSubQuery)
                .setParameter("userId", userId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM nest_reactions WHERE nest_id IN " + nestIdSubQuery)
                .setParameter("userId", userId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM unlock_histories WHERE nest_id IN " + nestIdSubQuery)
                .setParameter("userId", userId).executeUpdate();
        
        entityManager.createNativeQuery("DELETE FROM nest_images WHERE nest_id IN " + nestIdSubQuery)
                .setParameter("userId", userId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM nest_locations WHERE nest_id IN " + nestIdSubQuery)
                .setParameter("userId", userId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM nest_categories WHERE nest_id IN " + nestIdSubQuery)
                .setParameter("userId", userId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM nests WHERE creator_id = :userId")
                .setParameter("userId", userId).executeUpdate();

        // 4. 기타 정보 삭제
        entityManager.createNativeQuery("DELETE FROM nest_drafts WHERE creator_id = :userId").setParameter("userId", userId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM user_devices WHERE user_id = :userId").setParameter("userId", userId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM user_interests WHERE user_id = :userId").setParameter("userId", userId).executeUpdate();
        entityManager.createNativeQuery("DELETE FROM user_profiles WHERE user_id = :userId").setParameter("userId", userId).executeUpdate();

        // 5. 최종 유저 삭제
        entityManager.createNativeQuery("DELETE FROM users WHERE id = :userId").setParameter("userId", userId).executeUpdate();

        log.info("회원 하드 삭제 완료: userId={}", userId);
    }
}
