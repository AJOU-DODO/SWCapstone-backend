package com.dodo.dodoserver.domain.mypage.service;

import com.dodo.dodoserver.domain.mypage.dao.MyPageRepository;
import com.dodo.dodoserver.domain.mypage.dto.*;
import com.dodo.dodoserver.domain.nest.entity.ReactionType;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
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
    private final PostcardRepository postcardRepository;

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Page<MyPageNestResponseDto> getMyNests(Long userId, Long categoryId, Pageable pageable) {
        User user = getUser(userId);
        return myPageRepository.findNestsByUserAndCategory(user, categoryId, pageable)
                .map(nest -> MyPageNestResponseDto.from(nest, true));
    }

    @Transactional(readOnly = true)
    public Page<MyPageCommentResponseDto> getMyComments(Long userId, Pageable pageable) {
        User user = getUser(userId);
        return myPageRepository.findCommentsByUser(user, pageable)
                .map(MyPageCommentResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Page<MyPageDraftResponseDto> getMyDrafts(Long userId, Pageable pageable) {
        User user = getUser(userId);
        return myPageRepository.findDraftsByUser(user, pageable)
                .map(MyPageDraftResponseDto::from);
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
    public Page<MyPageNestResponseDto> getMyUnlockedNests(Long userId, Pageable pageable) {
        User user = getUser(userId);
        return myPageRepository.findUnlockedNestsByUser(user, pageable)
                .map(nest -> MyPageNestResponseDto.from(nest, true));
    }

    @Transactional(readOnly = true)
    public Page<MyPageCommentResponseDto> getCommentsOnMyNests(Long userId, Pageable pageable) {
        User user = getUser(userId);
        return myPageRepository.findCommentsOnUserNests(user, pageable)
                .map(MyPageCommentResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Page<MyPageNestResponseDto> getMyLikedNests(Long userId, Pageable pageable) {
        User user = getUser(userId);
        return myPageRepository.findLikedNestsByUser(user, pageable)
                .map(nest -> MyPageNestResponseDto.from(nest, true));
    }

    @Transactional(readOnly = true)
    public Page<MyPagePostcardResponseDto> getMyPostcards(Long userId, String filter, Pageable pageable) {
        User user = getUser(userId);
        
        Page<Postcard> postcards;
        if ("CREATED".equalsIgnoreCase(filter)) {
            postcards = postcardRepository.findCreatedByUser(user, pageable);
        } else if ("ACQUIRED".equalsIgnoreCase(filter)) {
            postcards = postcardRepository.findAcquiredByUser(user, pageable);
        } else {
            postcards = postcardRepository.findInventoryByUser(user, pageable);
        }

        return postcards.map(postcard -> MyPagePostcardResponseDto.from(postcard, userId));
    }
}
