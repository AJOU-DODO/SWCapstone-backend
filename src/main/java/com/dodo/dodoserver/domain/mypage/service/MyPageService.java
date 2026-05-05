package com.dodo.dodoserver.domain.mypage.service;

import com.dodo.dodoserver.domain.mypage.dao.MyPageRepository;
import com.dodo.dodoserver.domain.mypage.dto.MyPageStatisticsResponseDto;
import com.dodo.dodoserver.domain.nest.dto.NestCommentResponseDto;
import com.dodo.dodoserver.domain.nest.dto.NestDraftResponseDto;
import com.dodo.dodoserver.domain.nest.dto.NestSimpleResponseDto;
import com.dodo.dodoserver.domain.nest.entity.ReactionType;
import com.dodo.dodoserver.domain.postcard.dto.PostcardResponseDto;
import com.dodo.dodoserver.domain.postcard.service.PostcardService;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.global.error.ErrorCode;
import com.dodo.dodoserver.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;
    private final MyPageRepository myPageRepository;
    private final PostcardService postcardService;

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<NestSimpleResponseDto> getMyNests(Long userId, Long categoryId, Pageable pageable) {
        User user = getUser(userId);
        return myPageRepository.findNestsByUserAndCategory(user, categoryId, pageable)
                .map(nest -> NestSimpleResponseDto.from(nest, true, null));
    }

    @Transactional(readOnly = true)
    public Page<NestCommentResponseDto> getMyComments(Long userId, Pageable pageable) {
        User user = getUser(userId);
        return myPageRepository.findCommentsByUser(user, pageable)
                .map(NestCommentResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Page<NestDraftResponseDto> getMyDrafts(Long userId, Pageable pageable) {
        User user = getUser(userId);
        return myPageRepository.findDraftsByUser(user, pageable)
                .map(NestDraftResponseDto::from);
    }

    @Transactional(readOnly = true)
    public MyPageStatisticsResponseDto getMyStatistics(Long userId) {
        User user = getUser(userId);
        long nestCount = myPageRepository.countNestsByUser(user);
        long commentCount = myPageRepository.countCommentsByUser(user);
        long postcardCount = myPageRepository.countPostcardsByUser(user);
        return MyPageStatisticsResponseDto.of(nestCount, commentCount, postcardCount);
    }

    @Transactional(readOnly = true)
    public Page<NestSimpleResponseDto> getMyUnlockedNests(Long userId, Pageable pageable) {
        User user = getUser(userId);
        return myPageRepository.findUnlockedNestsByUser(user, pageable)
                .map(nest -> NestSimpleResponseDto.from(nest, true, null));
    }

    @Transactional(readOnly = true)
    public Page<NestCommentResponseDto> getReactionsOnMyNests(Long userId, Pageable pageable) {
        User user = getUser(userId);
        return myPageRepository.findCommentsOnUserNests(user, pageable)
                .map(NestCommentResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Page<NestSimpleResponseDto> getMyLikedNests(Long userId, Pageable pageable) {
        User user = getUser(userId);
        return myPageRepository.findLikedNestsByUser(user, pageable)
                .map(nest -> NestSimpleResponseDto.from(nest, true, ReactionType.LIKE));
    }

    @Transactional(readOnly = true)
    public Page<PostcardResponseDto> getMyPostcards(Long userId, String filter, Pageable pageable) {
        return postcardService.getPostcardInventory(userId, filter, pageable);
    }
}
