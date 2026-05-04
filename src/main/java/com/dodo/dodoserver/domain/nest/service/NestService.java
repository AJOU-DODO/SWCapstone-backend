package com.dodo.dodoserver.domain.nest.service;

import static com.dodo.dodoserver.global.common.constants.NestConstants.*;

import com.dodo.dodoserver.domain.category.dao.CategoryRepository;
import com.dodo.dodoserver.domain.category.entity.Category;
import com.dodo.dodoserver.domain.nest.dao.*;
import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.entity.*;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.postcard.entity.Postcard;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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
    private final CommentLikeRepository commentLikeRepository;
    private final NestNotificationService nestNotificationService;
    private final RedisViewCountService redisViewCountService;
    private final PostcardRepository postcardRepository;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 새로운 둥지(Nest) 생성
     */
    @Transactional
    public NestSimpleResponseDto createNest(Long userId, NestCreateRequestDto requestDto) {
        User creator = userRepository.findById(userId)
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

        // 초기 엽서 등록 처리
        if (requestDto.getPostcardId() != null) {
            Postcard postcard = postcardRepository.findById(requestDto.getPostcardId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.POSTCARD_NOT_FOUND));

            if (!postcard.getCurrentOwner().getId().equals(userId)) {
                throw new BusinessException(ErrorCode.NOT_POSTCARD_OWNER);
            }
            if (postcard.isShared()) {
                throw new BusinessException(ErrorCode.ALREADY_SHARED);
            }
            if (postcard.isExchanged()) {
                throw new BusinessException(ErrorCode.ALREADY_EXCHANGED);
            }

            postcard.shareToNest(savedNest);
        }

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
        return NestSimpleResponseDto.from(savedNest);
    }

    /**
     * 둥지 정보 수정
     */
    @Transactional
    public NestSimpleResponseDto updateNest(Long userId, Long nestId, NestUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId)
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

        // 엽서 추가/변경 로직
        if (requestDto.getPostcardId() != null) {
            Postcard newPostcard = postcardRepository.findById(requestDto.getPostcardId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.POSTCARD_NOT_FOUND));

            if (!newPostcard.getCurrentOwner().getId().equals(userId)) {
                throw new BusinessException(ErrorCode.NOT_POSTCARD_OWNER);
            }
            if (newPostcard.isShared()) {
                throw new BusinessException(ErrorCode.ALREADY_SHARED);
            }
            if (newPostcard.isExchanged()) {
                throw new BusinessException(ErrorCode.ALREADY_EXCHANGED);
            }

            // 이미 둥지에 공유된 엽서가 있는지 확인
            postcardRepository.findSharedPostcardByNest(nest).ifPresent(p -> {
                throw new BusinessException(ErrorCode.ALREADY_SHARED); // 둥지당 엽서는 하나만 가능
            });

            newPostcard.shareToNest(nest);
        }

        log.info("둥지 수정 완료: ID={}", nestId);
        return NestSimpleResponseDto.from(nest);
    }

    /**
     * 둥지 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteNest(Long userId, Long nestId) {
        User user = userRepository.findById(userId)
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
    public void createComment(Long userId, Long nestId, CommentCreateRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        if (!nest.getCreator().equals(user) && !unlockHistoryRepository.existsByUserAndNest(user, nest)) {
            throw new BusinessException(ErrorCode.NEST_NOT_UNLOCKED);
        }

        NestComment parent = null;
        if (requestDto.getParentId() != null) {
            parent = nestCommentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
            
            // 부모 댓글이 현재 둥지에 속해 있는지 검증
            if (!parent.getNest().getId().equals(nestId)) {
                log.warn("댓글 트리 무결성 오류: 부모 댓글(ID={})이 요청된 둥지(ID={})에 속하지 않음", parent.getId(), nestId);
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        NestComment comment = NestComment.builder()
                .nest(nest)
                .user(user)
                .parent(parent)
                .content(requestDto.getContent())
                .build();

        NestComment savedComment = nestCommentRepository.save(comment);
        log.info("댓글 작성 완료: Nest={}, User={}", nestId, userId);

        nestNotificationService.sendCommentNotification(user, nest, parent, savedComment);
    }

    /**
     * 댓글 수정
     */
    @Transactional
    public void updateComment(Long userId, Long commentId, CommentUpdateRequestDto requestDto) {
        User user = userRepository.findById(userId)
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
    public void deleteComment(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        NestComment comment = nestCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        if (!comment.getUser().equals(user)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }

        nestCommentRepository.delete(comment);
    }

    /**
     * ID 리스트 둥지 요약 정보 조회 (정렬 추가)
     */
    @Transactional(readOnly = true)
    public List<NestSummaryResponseDto> getNestsByIds(Long userId, List<Long> nestIds, Sort sort) {
        if (nestIds == null || nestIds.isEmpty()) return Collections.emptyList();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<NestQueryDto> nestDtos = nestRepository.findNestsByIdsCustom(nestIds, sort);
        if (nestDtos.isEmpty()) return Collections.emptyList();

        List<Nest> nestEntities = nestDtos.stream().map(NestQueryDto::getNest).collect(Collectors.toList());

        // 해금 이력 일괄 조회
        Set<Long> unlockedNestIds = unlockHistoryRepository.findAllByUserAndNestIn(user, nestEntities).stream()
                .map(uh -> uh.getNest().getId())
                .collect(Collectors.toSet());

        // 엽서 정보 일괄 조회
        Map<Long, Long> nestPostcardMap = postcardRepository.findAllByNestInAndIsSharedTrue(nestEntities).stream()
                .collect(Collectors.toMap(p -> p.getNest().getId(), Postcard::getId));

        return nestDtos.stream().map(dto -> {
            Nest nest = dto.getNest();
            boolean isUnlocked = nest.getCreator().equals(user) || unlockedNestIds.contains(nest.getId());
            return NestSummaryResponseDto.from(nest, isUnlocked, dto.getLikeCount(), dto.getDistance(), dto.getCategoryNames(), nestPostcardMap.get(nest.getId()));
        }).collect(Collectors.toList());
    }

    /**
     * 둥지 상세 정보 조회
     */
    @Transactional
    public NestDetailResponseDto getNestDetail(Long userId, Long nestId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        // Redis 기반 조회수 증가 (중복 방지 포함)
        redisViewCountService.incrementViewCount(nestId, userId);

        boolean isUnlocked = unlockHistoryRepository.existsByUserAndNest(user, nest) 
                || nest.getCreator().equals(user); // 자기 자신일 경우 해금

        UserProfile creatorProfile = userProfileRepository.findByUser(nest.getCreator()).orElse(null);
        long likeCount = nestReactionRepository.countByNestAndReactionType(nest, ReactionType.LIKE);
        long dislikeCount = nestReactionRepository.countByNestAndReactionType(nest, ReactionType.DISLIKE);

        List<String> categoryNames = nestCategoryRepository.findAllByNest(nest).stream()
                .map(nc -> nc.getCategory().getName())
                .collect(Collectors.toList());

        // 엽서 정보 조회
        Postcard sharedPostcard = postcardRepository.findSharedPostcardByNest(nest).orElse(null);

        return NestDetailResponseDto.builder()
                .id(nest.getId())
                .title(nest.getTitle())
                .content(nest.getContent())
                .unlockRadius(nest.getUnlockRadius())
                .viewCount(nest.getViewCount() + redisViewCountService.getCachedViewCount(nestId))
                .isAd(nest.isAd())
                .createdAt(nest.getCreatedAt())
                .creatorNickname(nest.getCreator().getNickname())
                .creatorProfileImageUrl(creatorProfile != null ? creatorProfile.getProfileImageUrl() : null)
                .categoryNames(categoryNames)
                .imageUrls(nest.getImages().stream().map(NestImage::getImageUrl).collect(Collectors.toList()))
                .likeCount(likeCount)
                .dislikeCount(dislikeCount)
                .isUnlocked(isUnlocked)
                .hasPostcard(sharedPostcard != null)
                .postcardId(sharedPostcard != null ? sharedPostcard.getId() : null)
                .build();
    }

    /**
     * 둥지 ID로 댓글 리스트 조회 (트리 구조, N+1 문제 완벽 해결)
     */
    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByNestId(Long currentUserId, Long nestId, String sortBy) {
        User currentUser = currentUserId != null ? userRepository.findById(currentUserId).orElse(null) : null;
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        List<NestComment> allComments = nestCommentRepository.findAllByNestWithUser(nest);

        if (allComments.isEmpty()) return Collections.emptyList();

        Set<User> authors = allComments.stream().map(NestComment::getUser).collect(Collectors.toSet());
        Map<Long, String> profileImageMap = userProfileRepository.findAllByUserIn(authors).stream() // (profile Id, image url)
                .filter(p -> p.getUser() != null)
                .collect(Collectors.toMap(p -> p.getUser().getId(), UserProfile::getProfileImageUrl, (oldV, newV) -> oldV));

        // 좋아요 여부 일괄 조회 및 캐싱
        Set<Long> likedCommentIds = new HashSet<>();
        if (currentUser != null) {
            likedCommentIds = commentLikeRepository.findAllByUserAndCommentIn(currentUser, allComments).stream()
                    .map(cl -> cl.getComment().getId())
                    .collect(Collectors.toSet());
        }

        // 부모 ID를 기준으로 그룹화 (트리 구조 생성용)
        Map<Long, List<NestComment>> childrenMap = allComments.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        // 최상위 댓글 추출 및 정렬
        List<NestComment> topComments = allComments.stream()
                .filter(c -> c.getParent() == null)
                .collect(Collectors.toList());
        sortComments(topComments, sortBy);

        Set<Long> finalLikedCommentIds = likedCommentIds; // 람다 Effectively Final
        return topComments.stream()
                .map(c -> convertToCommentResponseDto(c, childrenMap, profileImageMap, finalLikedCommentIds))
                .collect(Collectors.toList());
    }

    /**
     * 메모리 상에서 최상위 댓글 정렬
     */
    private void sortComments(List<NestComment> comments, String sortBy) {
        if ("LIKE".equalsIgnoreCase(sortBy)) {
            comments.sort(Comparator.comparing(NestComment::getLikeCount).reversed()
                    .thenComparing(Comparator.comparing(NestComment::getCreatedAt).reversed()));
        } else if ("LATEST".equalsIgnoreCase(sortBy)) {
            comments.sort(Comparator.comparing(NestComment::getCreatedAt).reversed());
        } else {
            comments.sort(Comparator.comparing(NestComment::getCreatedAt));
        }
    }

    /**
     * DTO 변환 (Map을 활용하여 LAZY 로딩 차단)
     */
    private CommentResponseDto convertToCommentResponseDto(
            NestComment comment, 
            Map<Long, List<NestComment>> childrenMap, 
            Map<Long, String> profileImageMap, 
            Set<Long> likedCommentIds) {
        
        List<NestComment> children = childrenMap.getOrDefault(comment.getId(), Collections.emptyList());
        // 대댓글은 생성순으로 정렬
        children.sort(Comparator.comparing(NestComment::getCreatedAt));

        return CommentResponseDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .nickname(comment.getUser().getNickname())
                .profileImageUrl(profileImageMap.get(comment.getUser().getId()))
                .createdAt(comment.getCreatedAt())
                .likeCount(comment.getLikeCount())
                .isLiked(likedCommentIds.contains(comment.getId()))
                .children(children.stream()
                        .map(child -> convertToCommentResponseDto(child, childrenMap, profileImageMap, likedCommentIds))
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 댓글 좋아요 처리 (Toggle 방식)
     */
    @Transactional
    public void handleCommentLike(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        
        NestComment comment = nestCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));

        Optional<CommentLike> existingLike = commentLikeRepository.findByUserAndComment(user, comment);

        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            comment.decreaseLikeCount();
            log.info("댓글 좋아요 취소: User={}, Comment={}", userId, commentId);
        } else {
            CommentLike like = CommentLike.builder()
                    .user(user)
                    .comment(comment)
                    .build();
            commentLikeRepository.save(like);
            comment.increaseLikeCount();
            log.info("댓글 좋아요 등록: User={}, Comment={}", userId, commentId);
            nestNotificationService.sendCommentLikeNotification(user, comment);
        }
    }


    /**
     * 현재 위치 기반 반경 내 모든 둥지 핀 정보 조회
     */
    @Transactional(readOnly = true)
    public List<NestPinResponseDto> getNearbyPins(Double latitude, Double longitude, Double radiusMeter, List<Long> categoryIds) {
        double radius = (radiusMeter != null) ? radiusMeter : DEFAULT_FIND_RADIUS_METER;
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude)); // (x,y)
        return nestRepository.findNearbyPins(point, radius, categoryIds);
    }

    /**
     * 현재 위치 기반 반경 내 카테고리별 둥지 리스트 조회
     */
    @Transactional(readOnly = true)
    public Page<NestSummaryResponseDto> getNearNestsByCategory(
            Long userId, Double latitude, Double longitude, Double radiusMeter, List<Long> categoryIds, Pageable pageable) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        double radius = (radiusMeter != null) ? radiusMeter : DEFAULT_FIND_RADIUS_METER;
        Point point = geometryFactory.createPoint(new Coordinate(longitude, latitude));

        Page<NestQueryDto> nests = nestRepository.findNearbyNests(point, radius, categoryIds, pageable);
        
        if (nests.isEmpty()) return Page.empty(pageable);

        List<Nest> nestEntities = nests.getContent().stream()
                .map(NestQueryDto::getNest)
                .collect(Collectors.toList());

        // 현재 페이지의 해금 이력 일괄 조회 (N+1 방지)
        Set<Long> unlockedNestIds = unlockHistoryRepository.findAllByUserAndNestIn(user, nestEntities).stream()
                .map(uh -> uh.getNest().getId())
                .collect(Collectors.toSet());

        // 현재 페이지의 엽서 정보 일괄 조회
        Map<Long, Long> nestPostcardMap = postcardRepository.findAllByNestInAndIsSharedTrue(nestEntities).stream()
                .collect(Collectors.toMap(p -> p.getNest().getId(), Postcard::getId));

        return nests.map(dto -> {
            Nest nestEntity = dto.getNest();
            boolean isUnlocked = nestEntity.getCreator().equals(user) || unlockedNestIds.contains(nestEntity.getId());
            return NestSummaryResponseDto.from(nestEntity, isUnlocked, dto.getLikeCount(), dto.getDistance(), dto.getCategoryNames(), nestPostcardMap.get(nestEntity.getId()));
        });
    }

    /**
     * 사용자 현재 위치 검증을 통한 둥지 해금
     */
    @Transactional
    public void unlockNest(Long userId, Long nestId, NestUnlockRequestDto requestDto) {
        User user = userRepository.findById(userId)
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
        log.info("둥지 해금 성공: User={}, Nest={}", userId, nestId);
    }

    /**
     * 둥지에 대한 리액션(좋아요/싫어요) 등록, 수정 또는 취소
     */
    @Transactional
    public void handleReaction(Long userId, Long nestId, ReactionType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        if (!unlockHistoryRepository.existsByUserAndNest(user, nest) && !nest.getCreator().equals(user)) {
            throw new BusinessException(ErrorCode.NEST_NOT_UNLOCKED);
        }

        Optional<NestReaction> existingReaction = nestReactionRepository.findByUserAndNest(user, nest);

        if (existingReaction.isPresent()) {
            NestReaction reaction = existingReaction.get();
            if (reaction.getReactionType() == type) {
                // 동일한 타입이면 취소(삭제)
                nestReactionRepository.delete(reaction);
                log.info("리액션 취소 완료: User={}, Nest={}, Type={}", userId, nestId, type);
            } else {
                // 다른 타입이면 수정
                reaction.setReactionType(type);
                log.info("리액션 수정 완료: User={}, Nest={}, Type={}", userId, nestId, type);
                sendReactionNotification(type, user, nest);
            }
        } else {
            // 없으면 신규 등록
            NestReaction reaction = NestReaction.builder()
                    .user(user)
                    .nest(nest)
                    .reactionType(type)
                    .build();
            nestReactionRepository.save(reaction);
            log.info("리액션 등록 완료: User={}, Nest={}, Type={}", userId, nestId, type);
            sendReactionNotification(type, user, nest);
        }
    }

    private void sendReactionNotification(ReactionType type, User user, Nest nest) {
        if (type == ReactionType.LIKE) {
            nestNotificationService.sendNestLikeNotification(user, nest);
        }
    }
}
