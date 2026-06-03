package com.dodo.dodoserver.domain.ad.scheduler;

import com.dodo.dodoserver.domain.ad.dao.NestAdInfoRepository;
import com.dodo.dodoserver.domain.ad.entity.NestAdInfo;
import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.user.dao.AdvertiserAuthorityRepository;
import com.dodo.dodoserver.domain.user.entity.AdvertiserAuthority;
import com.dodo.dodoserver.domain.user.entity.Role;
import com.dodo.dodoserver.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdScheduler {

    private final NestAdInfoRepository nestAdInfoRepository;
    private final NestRepository nestRepository;
    private final AdvertiserAuthorityRepository advertiserAuthorityRepository;

    /**
     * 광고 기한 만료 처리 (매 시간 정각)
     * NestAdInfo의 expired_at이 지난 광고 둥지를 Soft Delete 처리
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void processExpiredAds() {
        LocalDateTime now = LocalDateTime.now();
            List<NestAdInfo> expiredAds = nestAdInfoRepository.findAllByExpiredAtBefore(now);

        if (!expiredAds.isEmpty()) {
            expiredAds.forEach(adInfo -> {
                if (adInfo.getNest().getDeletedAt() == null) {
                    adInfo.getNest().setDeletedAt(now);
                }
            });
            log.info("만료 광고 처리 완료: {}건", expiredAds.size());
        }
    }

    /**
     * 광고주 권한 만료 처리 (매일 자정)
     * AdvertiserAuthority의 expired_at이 지난 유저의 Role을 USER로 강등
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void processExpiredAdvertiserAuthorities() {
        LocalDateTime now = LocalDateTime.now();
        List<AdvertiserAuthority> expiredAuthorities = advertiserAuthorityRepository.findAllByExpiredAtBefore(now);

        if (!expiredAuthorities.isEmpty()) {
            expiredAuthorities.forEach(auth -> {
                User user = auth.getUser();
                user.setRole(Role.USER);
                advertiserAuthorityRepository.delete(auth);
            });
            log.info("만료 광고주 권한 강등 처리 완료: {}명", expiredAuthorities.size());
        }
    }
}
