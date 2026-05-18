package com.dodo.dodoserver.domain.admin.user.service;

import com.dodo.dodoserver.domain.admin.user.dao.AdminEmailWhitelistRepository;
import com.dodo.dodoserver.domain.admin.user.dto.AdminEmailWhitelistRequestDto;
import com.dodo.dodoserver.domain.admin.user.dto.AdminEmailWhitelistResponseDto;
import com.dodo.dodoserver.domain.admin.user.entity.AdminEmailWhitelist;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEmailWhitelistService {

    private final AdminEmailWhitelistRepository adminEmailWhitelistRepository;

    /**
     * 전체 화이트리스트 목록 조회
     */
    public List<AdminEmailWhitelistResponseDto> getWhitelists() {
        return adminEmailWhitelistRepository.findAll().stream()
                .map(AdminEmailWhitelistResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 화이트리스트 이메일 추가
     */
    @Transactional
    public AdminEmailWhitelistResponseDto addWhitelist(AdminEmailWhitelistRequestDto dto) {
        if (adminEmailWhitelistRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_WHITELIST_EMAIL);
        }

        AdminEmailWhitelist whitelist = AdminEmailWhitelist.builder()
                .email(dto.getEmail())
                .remark(dto.getRemark())
                .build();

        return AdminEmailWhitelistResponseDto.from(adminEmailWhitelistRepository.save(whitelist));
    }

    /**
     * 화이트리스트 이메일 삭제
     */
    @Transactional
    public void removeWhitelist(Long id) {
        AdminEmailWhitelist whitelist = adminEmailWhitelistRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.WHITELIST_NOT_FOUND));
        adminEmailWhitelistRepository.delete(whitelist);
    }
}
