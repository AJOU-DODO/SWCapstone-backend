package com.dodo.dodoserver.domain.postcard.service;

import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.nest.dao.UnlockHistoryRepository;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.postcard.dao.PostcardReactionRepository;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.postcard.dto.*;
import com.dodo.dodoserver.domain.postcard.entity.Postcard;
import com.dodo.dodoserver.domain.postcard.entity.PostcardReaction;
import com.dodo.dodoserver.domain.postcard.entity.PostcardReactionType;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        return PostcardResponseDto.from(postcardRepository.save(postcard), userId);
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
        // 둥지 및 해금 여부 확인
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        if (!unlockHistoryRepository.existsByUserAndNest(user, nest)) {
            throw new BusinessException(ErrorCode.NEST_NOT_UNLOCKED);
        }


        // 내 엽서 락 획득 및 검증
        Postcard myPostcard = postcardRepository.findByIdForUpdate(requestDto.getMyPostcardId())
                .orElseThrow(() -> new BusinessException(ErrorCode.POSTCARD_NOT_FOUND));

        validateMyPostcard(user, myPostcard);

        // 둥지의 유일한 엽서 락 획득 및 검증
        Postcard targetPostcard = postcardRepository.findSharedPostcardByNestForUpdate(nest)
                .orElseThrow(() -> new BusinessException(ErrorCode.NO_AVAILABLE_POSTCARD_IN_NEST));

        // 본인 엽서 교환 차단
        if (targetPostcard.getOriginalAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_EXCHANGE_OWN_POSTCARD);
        }

        // 일일 교환 횟수 제한 
        postcardRedisService.checkAndIncrementExchangeCount(userId);

        // Atomic Swap
        targetPostcard.exchangeToUser(user);
        myPostcard.shareToNest(nest);

        // 알림 발행
        postcardNotificationService.sendPostcardExchangedNotification(targetPostcard, nest);

        return PostcardResponseDto.from(targetPostcard, userId);
    }

    @Transactional(readOnly = true)
    public PostcardResponseDto getPostcardDetail(Long userId, Long postcardId) {
        Postcard postcard = postcardRepository.findById(postcardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POSTCARD_NOT_FOUND));
        
        PostcardReactionType reactionType = postcardReactionRepository.findByPostcard(postcard)
                .map(PostcardReaction::getReactionType)
                .orElse(null);
                
        return PostcardResponseDto.from(postcard, reactionType, userId);
    }

    @Transactional(readOnly = true)
    public Page<PostcardResponseDto> getPostcardInventory(Long userId, String filter, Pageable pageable) {
        User user = getUser(userId);
        
        Page<Postcard> inventory;
        if ("CREATED".equalsIgnoreCase(filter)) {
            inventory = postcardRepository.findCreatedByUser(user, pageable);
        } else if ("ACQUIRED".equalsIgnoreCase(filter)) {
            inventory = postcardRepository.findAcquiredByUser(user, pageable);
        } else {
            inventory = postcardRepository.findInventoryByUser(user, pageable);
        }
        
        if (inventory.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Postcard> postcardList = inventory.getContent();

        // 리액션 일괄 조회 (N+1 방지)
        Map<Long, PostcardReactionType> reactionMap = postcardReactionRepository.findAllByPostcardIn(postcardList)
                .stream()
                .collect(Collectors.toMap(
                        r -> r.getPostcard().getId(),
                        PostcardReaction::getReactionType
                ));

        return inventory.map(p -> PostcardResponseDto.from(p, reactionMap.get(p.getId()), userId));
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

        return PostcardResponseDto.from(postcard, userId);
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
    public void addReaction(Long userId, Long postcardId, PostcardReactionType reactionType) {
        User user = getUser(userId);
        Postcard postcard = postcardRepository.findById(postcardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POSTCARD_NOT_FOUND));

        if (postcard.getCurrentOwner() == null || !postcard.getCurrentOwner().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_POSTCARD_OWNER);
        }

        if (postcard.getOriginalAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_REACTION_OWN_POSTCARD);
        }

        postcardReactionRepository.findByPostcardAndUser(postcard, user)
                .ifPresentOrElse(
                        reaction -> {
                            if (reaction.getReactionType() == reactionType) {
                                postcardReactionRepository.delete(reaction);
                            } else {
                                reaction.setReactionType(reactionType);
                                postcardNotificationService.sendPostcardReactionNotification(user, postcard, reactionType);
                            }
                        },
                        () -> {
                            PostcardReaction reaction = PostcardReaction.builder()
                                    .postcard(postcard)
                                    .user(user)
                                    .reactionType(reactionType)
                                    .build();
                            postcardReactionRepository.save(reaction);
                            postcardNotificationService.sendPostcardReactionNotification(user, postcard, reactionType);
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
