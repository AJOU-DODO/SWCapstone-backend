package com.dodo.dodoserver.domain.admin.user.service;

import com.dodo.dodoserver.domain.admin.user.dao.AdminEmailWhitelistRepository;
import com.dodo.dodoserver.domain.admin.user.dto.AdminEmailWhitelistRequestDto;
import com.dodo.dodoserver.domain.admin.user.dto.AdminEmailWhitelistResponseDto;
import com.dodo.dodoserver.domain.admin.user.entity.AdminEmailWhitelist;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEmailWhitelistServiceTest {

    @InjectMocks
    private AdminEmailWhitelistService adminEmailWhitelistService;

    @Mock
    private AdminEmailWhitelistRepository adminEmailWhitelistRepository;

    @Test
    @DisplayName("화이트리스트 목록 조회 성공")
    void getWhitelists_success() {
        // given
        AdminEmailWhitelist whitelist = AdminEmailWhitelist.builder().id(1L).email("test@dodo.com").build();
        given(adminEmailWhitelistRepository.findAll()).willReturn(List.of(whitelist));

        // when
        List<AdminEmailWhitelistResponseDto> result = adminEmailWhitelistService.getWhitelists();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("test@dodo.com");
    }

    @Test
    @DisplayName("화이트리스트 추가 성공")
    void addWhitelist_success() {
        // given
        AdminEmailWhitelistRequestDto dto = new AdminEmailWhitelistRequestDto("new@dodo.com", "비고");
        AdminEmailWhitelist whitelist = AdminEmailWhitelist.builder().id(1L).email("new@dodo.com").remark("비고").build();

        given(adminEmailWhitelistRepository.existsByEmail(dto.getEmail())).willReturn(false);
        given(adminEmailWhitelistRepository.save(any(AdminEmailWhitelist.class))).willReturn(whitelist);

        // when
        AdminEmailWhitelistResponseDto result = adminEmailWhitelistService.addWhitelist(dto);

        // then
        assertThat(result.getEmail()).isEqualTo("new@dodo.com");
        assertThat(result.getRemark()).isEqualTo("비고");
        verify(adminEmailWhitelistRepository, times(1)).save(any(AdminEmailWhitelist.class));
    }

    @Test
    @DisplayName("화이트리스트 추가 실패 - 이미 존재하는 이메일")
    void addWhitelist_fail_duplicateEmail() {
        // given
        AdminEmailWhitelistRequestDto dto = new AdminEmailWhitelistRequestDto("duplicate@dodo.com", null);
        given(adminEmailWhitelistRepository.existsByEmail(dto.getEmail())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> adminEmailWhitelistService.addWhitelist(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.DUPLICATE_WHITELIST_EMAIL.getMessage());
    }

    @Test
    @DisplayName("화이트리스트 삭제 성공")
    void removeWhitelist_success() {
        // given
        Long id = 1L;
        AdminEmailWhitelist whitelist = AdminEmailWhitelist.builder().id(id).email("delete@dodo.com").build();
        given(adminEmailWhitelistRepository.findById(id)).willReturn(Optional.of(whitelist));

        // when
        adminEmailWhitelistService.removeWhitelist(id);

        // then
        verify(adminEmailWhitelistRepository, times(1)).delete(whitelist);
    }

    @Test
    @DisplayName("화이트리스트 삭제 실패 - 존재하지 않는 ID")
    void removeWhitelist_fail_notFound() {
        // given
        Long id = 999L;
        given(adminEmailWhitelistRepository.findById(id)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminEmailWhitelistService.removeWhitelist(id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.WHITELIST_NOT_FOUND.getMessage());
    }
}
