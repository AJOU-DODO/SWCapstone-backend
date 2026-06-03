package com.dodo.dodoserver.domain.admin.user.service;

import com.dodo.dodoserver.domain.admin.user.dao.SanctionHistoryRepository;
import com.dodo.dodoserver.domain.admin.user.dao.UserAdminRepository;
import com.dodo.dodoserver.domain.admin.user.dto.UserAdminResponseDto;
import com.dodo.dodoserver.domain.admin.user.dto.UserSanctionRequestDto;
import com.dodo.dodoserver.domain.admin.user.entity.SanctionHistory;
import com.dodo.dodoserver.domain.admin.user.entity.SanctionType;
import com.dodo.dodoserver.domain.auth.dao.RefreshTokenRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final UserAdminRepository userAdminRepository;
    private final SanctionHistoryRepository sanctionHistoryRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 전체 유저 관리자용 정보 조회 (페이징)
     */
    public Page<UserAdminResponseDto> getAllUsers(Pageable pageable) {
        return userAdminRepository.findAllUserAdminInfo(pageable);
    }

    /**
     * 유저 제재 처리
     */
    @Transactional
    public void sanctionUser(Long userId, UserSanctionRequestDto requestDto) {
        User user = userAdminRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime endedAt = requestDto.getSanctionType().calculateEndedAt();
        
        // 1. User 엔티티의 제재 정보 업데이트
        user.applySanction(endedAt);

        // 2. 제재 이력 저장
        SanctionHistory history = SanctionHistory.builder()
                .user(user)
                .sanctionType(requestDto.getSanctionType())
                .reason(requestDto.getReason())
                .endedAt(endedAt)
                .build();
        
        sanctionHistoryRepository.save(history);

        // 3. 기존 리프레쉬 토큰 무효화 (Redis에서 삭제)
        refreshTokenRepository.deleteById(userId);
    }

    /**
     * 유저 제재 즉시 해제
     */
    @Transactional
    public void liftSanction(Long userId) {
        User user = userAdminRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 1. User 엔티티의 제재 정보 초기화
        user.liftSanction();

        // 2. 제재 해제 이력 저장
        SanctionHistory history = SanctionHistory.builder()
                .user(user)
                .sanctionType(SanctionType.LIFTED)
                .reason("관리자에 의한 제재 즉시 해제")
                .endedAt(SanctionType.LIFTED.calculateEndedAt())
                .build();

        sanctionHistoryRepository.save(history);
    }
}
