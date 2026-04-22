package com.dodo.dodoserver.domain.user.service;

import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.category.dto.CategoryResponseDto;
import com.dodo.dodoserver.domain.category.entity.Category;
import com.dodo.dodoserver.domain.user.dao.UserInterestRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.dto.UserInterestRequestDto;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserInterest;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserInterestServiceTest {

    @InjectMocks
    private UserInterestService userInterestService;

    @Mock
    private UserInterestRepository userInterestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("내 관심사 조회 성공")
    void getMyInterests_success() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@example.com").build();
        Category category = Category.builder().id(1L).name("카페").build();
        UserInterest userInterest = UserInterest.builder().user(user).category(category).build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userInterestRepository.findByUserId(user.getId())).willReturn(List.of(userInterest));

        // when
        List<CategoryResponseDto> result = userInterestService.getMyInterests(userId);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("카페");
    }

    @Test
    @DisplayName("관심사 업데이트 성공")
    void updateInterests_success() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@example.com").build();
        UserInterestRequestDto requestDto = new UserInterestRequestDto(List.of(1L, 2L));
        Category category1 = Category.builder().id(1L).name("카페").build();
        Category category2 = Category.builder().id(2L).name("맛집").build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category1));
        given(categoryRepository.findById(2L)).willReturn(Optional.of(category2));

        // when
        userInterestService.updateInterests(userId, requestDto);

        // then
        verify(userInterestRepository, times(1)).deleteByUserId(user.getId());
        verify(userInterestRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("관심사 업데이트 실패 - 카테고리 없음")
    void updateInterests_fail_categoryNotFound() {
        // given
        Long userId = 1L;
        User user = User.builder().id(userId).email("test@example.com").build();
        UserInterestRequestDto requestDto = new UserInterestRequestDto(List.of(99L));

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userInterestService.updateInterests(userId, requestDto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }
}
