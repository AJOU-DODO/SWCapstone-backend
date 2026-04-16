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

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 새로운 둥지(Nest)를 생성합니다.
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
        return NestSummaryResponseDto.from(savedNest);
    }

    /**
     * 둥지 상세 정보를 조회합니다. (해금 여부에 따라 내용 제한 가능)
     */
    @Transactional
    public NestDetailResponseDto getNestDetail(String email, Long nestId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        // 1. 조회수 증가
        nest.setViewCount(nest.getViewCount() + 1);

        // 2. 해금 여부 확인
        boolean isUnlocked = unlockHistoryRepository.existsByUserAndNest(user, nest) 
                || nest.getCreator().equals(user);

        // 3. 작성자 프로필 정보 조회
        UserProfile creatorProfile = userProfileRepository.findByUser(nest.getCreator()).orElse(null);

        // 4. 리액션 수 집계
        long likeCount = nestReactionRepository.countByNestAndReactionType(nest, ReactionType.LIKE);
        long dislikeCount = nestReactionRepository.countByNestAndReactionType(nest, ReactionType.DISLIKE);

        // 5. 카테고리 이름 목록 추출
        List<String> categoryNames = nestCategoryRepository.findAll().stream()
                .filter(nc -> nc.getNest().equals(nest))
                .map(nc -> nc.getCategory().getName())
                .collect(Collectors.toList());

        // 6. 댓글 트리 구조 생성
        List<CommentResponseDto> comments = nestCommentRepository.findAllByNestAndParentIsNullOrderByCreatedAtAsc(nest)
                .stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());

        return NestDetailResponseDto.builder()
                .id(nest.getId())
                .title(nest.getTitle())
                .content(isUnlocked ? nest.getContent() : "해금이 필요한 콘텐츠입니다.")
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
     * 현재 위치 기반 반경 내의 모든 둥지 핀 정보를 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<NestPinResponseDto> getNearbyPins(Double latitude, Double longitude, Double radiusMeter) {
        double radius = (radiusMeter != null) ? radiusMeter : 5000.0;
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        return nestRepository.findNearbyPins(point, radius);
    }

    /**
     * 현재 위치 기반 반경 내의 카테고리별 둥지 리스트를 조회합니다.
     */
    @Transactional(readOnly = true)
    public Page<NestSummaryResponseDto> getNearbyNests(
            Double latitude, Double longitude, Double radiusMeter, Long categoryId, Pageable pageable) {
        
        double radius = (radiusMeter != null) ? radiusMeter : 5000.0;
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        Page<Nest> nests = nestRepository.findNearbyNests(point, radius, categoryId, pageable);
        
        return nests.map(NestSummaryResponseDto::from);
    }

    /**
     * 사용자의 현재 위치를 검증하여 둥지를 해금합니다.
     */
    @Transactional
    public void unlockNest(String email, Long nestId, NestUnlockRequestDto requestDto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        if (unlockHistoryRepository.existsByUserAndNest(user, nest)) {
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
     * 둥지에 대한 리액션(좋아요/싫어요)을 등록하거나 수정합니다.
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

        NestReaction reaction = nestReactionRepository.findByUserAndNest(user, nest)
                .orElse(NestReaction.builder().user(user).nest(nest).build());
        
        reaction.setReactionType(type);
        nestReactionRepository.save(reaction);
    }
}
