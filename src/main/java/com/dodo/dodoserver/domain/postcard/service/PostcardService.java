package com.dodo.dodoserver.domain.postcard.service;

import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.nest.dao.UnlockHistoryRepository;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.postcard.dao.PostcardReactionRepository;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.postcard.dto.*;
import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import com.dodo.dodoserver.domain.postcard.entity.PostcardReaction;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostcardService {

    private final PostcardRepository postcardRepository;
    private final PostcardRedisService postcardRedisService;
    private final NestRepository nestRepository;
    private final UnlockHistoryRepository unlockHistoryRepository;
    private final PostcardNotificationService postcardNotificationService;
    private final PostcardReactionRepository postcardReactionRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostcardResponseDto createPostcard(Long userId, PostcardCreateRequestDto requestDto) {
        User user = getUser(userId);
        Postcard postcard = Postcard.builder()
                .originalAuthor(user)
                .currentOwner(user)
                .imageUrl(requestDto.getImageUrl())
                .content(requestDto.getContent())
                .isShared(false)
                .isExchanged(false)
                .build();

        return PostcardResponseDto.from(postcardRepository.save(postcard));
    }

    @Transactional(readOnly = true)
    public PostcardExchangeCheckResponseDto checkExchangeAvailability(Long userId) {
        int remainingCount = postcardRedisService.getRemainingExchangeCount(userId);
        
        if (remainingCount <= 0) {
            return PostcardExchangeCheckResponseDto.builder()
                    .canExchange(false)
                    .remainingCount(0)
                    .reason(ErrorCode.EXCHANGE_LIMIT_EXCEEDED.getMessage())
                    .build();
        }

        return PostcardExchangeCheckResponseDto.builder()
                .canExchange(true)
                .remainingCount(remainingCount)
                .build();
    }

    @Transactional
    public PostcardResponseDto exchangePostcard(Long userId, Long nestId, PostcardExchangeRequestDto requestDto) {
        User user = getUser(userId);
        // 1. 둥지 및 해금 여부 확인
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        if (!unlockHistoryRepository.existsByUserAndNest(user, nest)) {
            throw new BusinessException(ErrorCode.NEST_NOT_UNLOCKED);
        }

        // 2. 일일 교환 횟수 제한 확인 및 증가 (Atomic INCR)
        postcardRedisService.checkAndIncrementExchangeCount(userId);

        // 3. 내 엽서 락 획득 및 검증
        Postcard myPostcard = postcardRepository.findByIdForUpdate(requestDto.getMyPostcardId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POSTCARD_NOT_FOUND));

        validateMyPostcard(user, myPostcard);

        // 4. 둥지의 유일한 엽서 락 획득 및 검증
        Postcard targetPostcard = postcardRepository.findSharedPostcardByNestForUpdate(nest)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_AVAILABLE_POSTCARD_IN_NEST));

        // 5. 본인 엽서 교환 차단
        if (targetPostcard.getOriginalAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_EXCHANGE_OWN_POSTCARD);
        }

        // 6. Atomic Swap
        targetPostcard.exchangeToUser(user);
        myPostcard.shareToNest(nest);

        // 7. 알림 발행 (FCM 토큰 조회 등이 포함되므로 커밋 후 비동기 처리가 권장되나, 현재는 서비스 내에서 호출)
        postcardNotificationService.sendPostcardExchangedNotification(targetPostcard, nest);

        return PostcardResponseDto.from(targetPostcard);
    }

    @Transactional(readOnly = true)
    public PostcardResponseDto getPostcardDetail(Long userId, Long postcardId) {
        Postcard postcard = postcardRepository.findById(postcardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POSTCARD_NOT_FOUND));
        return PostcardResponseDto.from(postcard);
    }

    @Transactional
    public PostcardResponseDto updatePostcard(Long userId, Long postcardId, PostcardCreateRequestDto requestDto) {
        Postcard postcard = postcardRepository.findById(postcardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POSTCARD_NOT_FOUND));

        if (!postcard.getCurrentOwner().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POSTCARD_OWNER);
        }

        if (postcard.isExchanged() || postcard.isShared()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        postcard.setImageUrl(requestDto.getImageUrl());
        postcard.setContent(requestDto.getContent());

        return PostcardResponseDto.from(postcard);
    }

    @Transactional
    public void deletePostcard(Long userId, Long postcardId) {
        Postcard postcard = postcardRepository.findById(postcardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POSTCARD_NOT_FOUND));

        if (!postcard.getCurrentOwner().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POSTCARD_OWNER);
        }

        if (postcard.isShared()) {
            throw new BusinessException(ErrorCode.ALREADY_SHARED);
        }

        postcardRepository.delete(postcard);
    }

    @Transactional
    public void addReaction(Long userId, Long postcardId, com.dodo.dodoserver.domain.nest.entity.ReactionType reactionType) {
        User user = getUser(userId);
        Postcard postcard = postcardRepository.findById(postcardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POSTCARD_NOT_FOUND));

        postcardReactionRepository.findByPostcardAndUser(postcard, user)
                .ifPresentOrElse(
                        reaction -> {
                            if (reaction.getReactionType() == reactionType) {
                                postcardReactionRepository.delete(reaction);
                            } else {
                                reaction.setReactionType(reactionType);
                            }
                        },
                        () -> {
                            PostcardReaction reaction = PostcardReaction.builder()
                                    .postcard(postcard)
                                    .user(user)
                                    .reactionType(reactionType)
                                    .build();
                            postcardReactionRepository.save(reaction);
                            
                            // 좋아요 반응인 경우 알림 발송
                            if (reactionType == com.dodo.dodoserver.domain.nest.entity.ReactionType.LIKE) {
                                postcardNotificationService.sendPostcardLikeNotification(user, postcard);
                            }
                        }
                );
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void validateMyPostcard(User user, Postcard myPostcard) {
        if (!myPostcard.getCurrentOwner().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_POSTCARD_OWNER);
        }
        if (myPostcard.isShared()) {
            throw new BusinessException(ErrorCode.ALREADY_SHARED);
        }
        if (myPostcard.isExchanged()) {
            throw new BusinessException(ErrorCode.ALREADY_EXCHANGED);
        }
    }
}
