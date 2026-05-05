package com.dodo.dodoserver.domain.mypage.service;

import com.dodo.dodoserver.domain.mypage.dao.MyPageRepository;
import com.dodo.dodoserver.domain.mypage.dto.MyPageStatisticsResponseDto;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MyPageServiceTest {

    @InjectMocks
    private MyPageService myPageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MyPageRepository myPageRepository;

    @Mock
    private PostcardRepository postcardRepository;

    @Test
    @DisplayName("유저가 없는 경우 예외 발생")
    void getUser_fail() {
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        assertThatThrownBy(() -> myPageService.getMyStatistics(1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("마이페이지 통계 조회 성공")
    void getMyStatistics_success() {
        User user = User.builder().id(1L).build();
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        given(myPageRepository.countNestsByUser(user)).willReturn(5L);
        given(myPageRepository.countCommentsByUser(user)).willReturn(10L);
        given(myPageRepository.countPostcardsByUser(user)).willReturn(3L);

        MyPageStatisticsResponseDto result = myPageService.getMyStatistics(1L);

        assertThat(result.getNestCount()).isEqualTo(5);
        assertThat(result.getCommentCount()).isEqualTo(10);
        assertThat(result.getPostcardCount()).isEqualTo(3);
    }
}
