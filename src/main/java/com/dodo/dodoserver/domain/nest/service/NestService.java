package com.dodo.dodoserver.domain.nest.service;

import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.category.entity.Category;
import com.dodo.dodoserver.domain.nest.dao.*;
import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.entity.*;
import com.dodo.dodoserver.domain.user.dao.UserProfileRepository;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.entity.UserProfile;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class NestService {

    private final NestRepository nestRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final NestCategoryRepository nestCategoryRepository;
    private final UnlockHistoryRepository unlockHistoryRepository;
    private final NestReactionRepository nestReactionRepository;
    private final NestCommentRepository nestCommentRepository;
    private final UserProfileRepository userProfileRepository;
    private final NestImageRepository nestImageRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 새로운 둥지(Nest) 생성
     */
    @Transactional
    public NestSummaryResponseDto createNest(String email, NestCreateRequestDto requestDto) {
        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Nest nest = Nest.builder()
                .creator(creator)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .unlockRadius(requestDto.getUnlockRadius())
                .isAd(requestDto.isAd())
                .build();

        Point point = geometryFactory.createPoint(new Coordinate(requestDto.getLongitude(), requestDto.getLatitude()));
        NestLocation location = NestLocation.builder()
                .nest(nest)
                .point(point)
                .build();
        nest.setLocation(location);

        if (requestDto.getImageUrls() != null) {
            List<String> urls = requestDto.getImageUrls();
            IntStream.range(0, urls.size()).forEach(i -> {
                NestImage image = NestImage.builder()
                        .nest(nest)
                        .imageUrl(urls.get(i))
                        .sortOrder(i + 1)
                        .build();
                nest.addImage(image);
            });
        }

        Nest savedNest = nestRepository.save(nest);

        if (requestDto.getCategoryIds() != null) {
            requestDto.getCategoryIds().forEach(categoryId -> {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
                
                NestCategory nestCategory = NestCategory.builder()
                        .nest(savedNest)
                        .category(category)
                        .build();
                nestCategoryRepository.save(nestCategory);
            });
        }

        log.info("새로운 둥지 생성 완료: ID={}, Title={}", savedNest.getId(), savedNest.getTitle());
        return NestSummaryResponseDto.from(savedNest, true);
    }

    /**
     * 둥지 정보 수정
     */
    @Transactional
    public NestSummaryResponseDto updateNest(String email, Long nestId, NestUpdateRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        if (!nest.getCreator().equals(user)) {
            throw new BusinessException(ErrorCode.NOT_NEST_CREATOR);
        }

        // 기본 정보 수정
        if (requestDto.getTitle() != null) nest.setTitle(requestDto.getTitle());
        if (requestDto.getContent() != null) nest.setContent(requestDto.getContent());
        if (requestDto.getUnlockRadius() != null) nest.setUnlockRadius(requestDto.getUnlockRadius());

        // 이미지 수정 (기존 이미지 삭제 후 새로 등록)
        if (requestDto.getImageUrls() != null) {
            nest.getImages().clear();
            List<String> urls = requestDto.getImageUrls();
            IntStream.range(0, urls.size()).forEach(i -> {
                NestImage image = NestImage.builder()
                        .nest(nest)
                        .imageUrl(urls.get(i))
                        .sortOrder(i + 1)
                        .build();
                nest.addImage(image);
            });
        }

        // 카테고리 수정
        if (requestDto.getCategoryIds() != null) {
            // 기존 매핑 삭제 (이 방법은 쿼리 효율을 위해 수동으로 처리하거나 orphanRemoval 사용 가능)
            // 여기서는 수동 삭제 후 재생성 방식을 취함
            nestCategoryRepository.deleteByNest(nest);
            
            requestDto.getCategoryIds().forEach(categoryId -> {
                Category category = categoryRepository.findById(categoryId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
                
                NestCategory nestCategory = NestCategory.builder()
                        .nest(nest)
                        .category(category)
                        .build();
                nestCategoryRepository.save(nestCategory);
            });
        }

        log.info("둥지 수정 완료: ID={}", nestId);
        return NestSummaryResponseDto.from(nest, true);
    }

    /**
     * 둥지 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteNest(String email, Long nestId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        if (!nest.getCreator().equals(user)) {
            throw new BusinessException(ErrorCode.NOT_NEST_CREATOR);
        }

        nestRepository.delete(nest);
        log.info("둥지 삭제 완료: ID={}", nestId);
    }

    /**
     * 둥지 댓글 작성
     */
    @Transactional
    public void createComment(String email, Long nestId, CommentCreateRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        if (!nest.getCreator().equals(user) && !unlockHistoryRepository.existsByUserAndNest(user, nest)) {
            throw new BusinessException(ErrorCode.ONBOARDING_REQUIRED); 
        }

        NestComment parent = null;
        if (requestDto.getParentId() != null) {
            parent = nestCommentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
        }

        NestComment comment = NestComment.builder()
                .nest(nest)
                .user(user)
                .parent(parent)
                .content(requestDto.getContent())
                .build();

        nestCommentRepository.save(comment);
        log.info("댓글 작성 완료: Nest={}, User={}", nestId, email);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public void updateComment(String email, Long commentId, CommentUpdateRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        NestComment comment = nestCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        if (!comment.getUser().equals(user)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        comment.setContent(requestDto.getContent());
    }

    /**
     * 댓글 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteComment(String email, Long commentId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        NestComment comment = nestCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        if (!comment.getUser().equals(user)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        nestCommentRepository.delete(comment);
    }

    /**
     * ID 리스트 둥지 요약 정보 조회
     */
    @Transactional(readOnly = true)
    public List<NestSummaryResponseDto> getNestsByIds(String email, List<Long> nestIds) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Nest> nests = nestRepository.findAllById(nestIds);

        return nests.stream().map(nest -> {
            boolean isUnlocked = unlockHistoryRepository.existsByUserAndNest(user, nest)
                    || nest.getCreator().equals(user);
            return NestSummaryResponseDto.from(nest, isUnlocked);
        }).collect(Collectors.toList());
    }

    /**
     * 둥지 상세 정보 조회
     */
    @Transactional
    public NestDetailResponseDto getNestDetail(String email, Long nestId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        nest.setViewCount(nest.getViewCount() + 1);

        boolean isUnlocked = unlockHistoryRepository.existsByUserAndNest(user, nest) 
                || nest.getCreator().equals(user); // 자기 자신일 경우 해금

        UserProfile creatorProfile = userProfileRepository.findByUser(nest.getCreator()).orElse(null);
        long likeCount = nestReactionRepository.countByNestAndReactionType(nest, ReactionType.LIKE);
        long dislikeCount = nestReactionRepository.countByNestAndReactionType(nest, ReactionType.DISLIKE);

        List<String> categoryNames = nestCategoryRepository.findAllByNest(nest).stream()
                .map(nc -> nc.getCategory().getName())
                .collect(Collectors.toList());

        List<CommentResponseDto> comments = nestCommentRepository.findAllByNestAndParentIsNullOrderByCreatedAtAsc(nest)
                .stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());

        return NestDetailResponseDto.builder()
                .id(nest.getId())
                .title(nest.getTitle())
                .content(nest.getContent())
                .unlockRadius(nest.getUnlockRadius())
                .viewCount(nest.getViewCount())
                .isAd(nest.isAd())
                .createdAt(nest.getCreatedAt())
                .creatorNickname(nest.getCreator().getNickname())
                .creatorProfileImageUrl(creatorProfile != null ? creatorProfile.getProfileImageUrl() : null)
                .categoryNames(categoryNames)
                .imageUrls(nest.getImages().stream().map(NestImage::getImageUrl).collect(Collectors.toList()))
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .isUnlocked(isUnlocked)
                .comments(comments)
                .build();
    }

    /**
     * 현재 위치 기반 반경 내 모든 둥지 핀 정보 조회
     */
    @Transactional(readOnly = true)
    public List<NestPinResponseDto> getNearbyPins(Double latitude, Double longitude, Double radiusMeter) {
        double radius = (radiusMeter != null) ? radiusMeter : 5000.0;
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude)); // (x,y)
        return nestRepository.findNearbyPins(point, radius).stream()
                .map(NestPinResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 현재 위치 기반 반경 내 카테고리별 둥지 리스트 조회
     */
    @Transactional(readOnly = true)
    public Page<NestSummaryResponseDto> getNearNestsByCategory(
            String email, Double latitude, Double longitude, Double radiusMeter, Long categoryId, Pageable pageable) {
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        double radius = (radiusMeter != null) ? radiusMeter : 5000.0;
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        Page<Nest> nests = nestRepository.findNearbyNests(point, radius, categoryId, pageable);
        
        return nests.map(nest -> {
            boolean isUnlocked = unlockHistoryRepository.existsByUserAndNest(user, nest)
                    || nest.getCreator().equals(user);
            return NestSummaryResponseDto.from(nest, isUnlocked);
        });
    }

    /**
     * 사용자 현재 위치 검증을 통한 둥지 해금
     */
    @Transactional
    public void unlockNest(String email, Long nestId, NestUnlockRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        if (nest.getCreator().equals(user) || unlockHistoryRepository.existsByUserAndNest(user, nest)) {
            throw new BusinessException(ErrorCode.ALREADY_UNLOCKED);
        }

        Point userPoint = geometryFactory.createPoint(new Coordinate(requestDto.getLongitude(), requestDto.getLatitude()));
        Double distance = nestRepository.calculateDistance(nestId, userPoint);

        if (distance == null || distance > nest.getUnlockRadius()) {
            log.warn("해금 실패: 거리 초과 (Distance={}, Radius={})", distance, nest.getUnlockRadius());
            throw new BusinessException(ErrorCode.OUT_OF_UNLOCK_RADIUS);
        }

        UnlockHistory history = UnlockHistory.builder()
                .user(user)
                .nest(nest)
                .verifiedPoint(userPoint)
                .build();
        
        unlockHistoryRepository.save(history);
        log.info("둥지 해금 성공: User={}, Nest={}", email, nestId);
    }

    /**
     * 둥지에 대한 리액션(좋아요/싫어요) 등록, 수정 또는 취소
     */
    @Transactional
    public void handleReaction(String email, Long nestId, ReactionType type) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        if (!unlockHistoryRepository.existsByUserAndNest(user, nest) && !nest.getCreator().equals(user)) {
            throw new BusinessException(ErrorCode.ONBOARDING_REQUIRED); 
        }

        Optional<NestReaction> existingReaction = nestReactionRepository.findByUserAndNest(user, nest);

        if (existingReaction.isPresent()) {
            NestReaction reaction = existingReaction.get();
            if (reaction.getReactionType() == type) {
                // 동일한 타입이면 취소(삭제)
                nestReactionRepository.delete(reaction);
                log.info("리액션 취소 완료: User={}, Nest={}, Type={}", email, nestId, type);
            } else {
                // 다른 타입이면 수정
                reaction.setReactionType(type);
                log.info("리액션 수정 완료: User={}, Nest={}, Type={}", email, nestId, type);
            }
        } else {
            // 없으면 신규 등록
            NestReaction reaction = NestReaction.builder()
                    .user(user)
                    .nest(nest)
                    .reactionType(type)
                    .build();
            nestReactionRepository.save(reaction);
            log.info("리액션 등록 완료: User={}, Nest={}, Type={}", email, nestId, type);
        }
    }
}
