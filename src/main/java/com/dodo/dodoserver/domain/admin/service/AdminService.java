package com.dodo.dodoserver.domain.admin.service;

import com.dodo.dodoserver.domain.admin.dao.SanctionHistoryRepository;
import com.dodo.dodoserver.domain.admin.dao.UserAdminRepository;
import com.dodo.dodoserver.domain.admin.dto.UserAdminResponseDto;
import com.dodo.dodoserver.domain.admin.dto.UserSanctionRequestDto;
import com.dodo.dodoserver.domain.admin.entity.SanctionHistory;
import com.dodo.dodoserver.domain.admin.entity.SanctionType;
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

        LocalDateTime endedAt = calculateEndedAt(requestDto.getSanctionType());
        
        // 1. User 엔티티의 sanctionedUntil 업데이트
        user.setSanctionedUntil(endedAt);

        // 2. 제재 이력 저장
        SanctionHistory history = SanctionHistory.builder()
                .user(user)
                .sanctionType(requestDto.getSanctionType())
                .reason(requestDto.getReason())
                .endedAt(endedAt)
                .build();
        
        sanctionHistoryRepository.save(history);
    }

    private LocalDateTime calculateEndedAt(SanctionType type) {
        if (type == SanctionType.PERMANENT) {
            // 영구 정지: 9999년 12월 31일
            return LocalDateTime.of(9999, 12, 31, 23, 59, 59);
        }
        return LocalDateTime.now().plusDays(type.getDays());
    }
}
