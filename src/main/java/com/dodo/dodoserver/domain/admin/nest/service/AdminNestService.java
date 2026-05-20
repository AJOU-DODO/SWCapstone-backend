package com.dodo.dodoserver.domain.admin.nest.service;

import com.dodo.dodoserver.domain.admin.nest.dto.AdminCommentResponseDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestDeleteRequestDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestDetailResponseDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestResponseDto;
import com.dodo.dodoserver.domain.nest.dao.*;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestComment;
import com.dodo.dodoserver.domain.nest.entity.NestImage;
import com.dodo.dodoserver.domain.postcard.dao.PostcardRepository;
import com.dodo.dodoserver.domain.report.dao.ReportRepository;
import com.dodo.dodoserver.domain.report.entity.ReportStatus;
import com.dodo.dodoserver.domain.user.dao.UserDeviceRepository;
import com.dodo.dodoserver.domain.user.entity.UserDevice;
import com.dodo.dodoserver.infrastructure.fcm.FcmService;
import com.dodo.dodoserver.infrastructure.fcm.NotificationEvent;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.dodo.dodoserver.domain.report.entity.ReportType;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dodo.dodoserver.domain.report.entity.QReport.report;
import static com.dodo.dodoserver.global.common.constants.NotificationConstants.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNestService {

    private final NestRepository nestRepository;
    private final NestCommentRepository nestCommentRepository;
    private final NestCategoryRepository nestCategoryRepository;
    private final NestReactionRepository nestReactionRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final PostcardRepository postcardRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final FcmService fcmService;
    private final JPAQueryFactory queryFactory;

    public Page<AdminNestResponseDto> getNestsForAdmin(
            Pageable pageable, 
            LocalDate startDate, 
            LocalDate endDate, 
            String sort, 
            String includeDeleted) {
        return nestRepository.findNestsForAdmin(pageable, startDate, endDate, sort, includeDeleted);
    }

    public AdminNestDetailResponseDto getNestDetailForAdmin(Long nestId) {
        // @SQLRestriction을 우회하여 삭제된 둥지도 상세 조회가 가능해야 함
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        var nestCategories = nestCategoryRepository.findAllByNest(nest);
        
        List<Long> categoryIds = nestCategories.stream()
                .map(nc -> nc.getCategory().getId())
                .collect(Collectors.toList());

        List<String> categoryNames = nestCategories.stream()
                .map(nc -> nc.getCategory().getName())
                .collect(Collectors.toList());

        return AdminNestDetailResponseDto.builder()
                .nestId(nest.getId())
                .title(nest.getTitle())
                .content(nest.getContent())
                .authorNickname(nest.getCreator().getNickname())
                .latitude(nest.getLocation() != null ? nest.getLocation().getLatitude() : 0.0)
                .longitude(nest.getLocation() != null ? nest.getLocation().getLongitude() : 0.0)
                .imageUrls(nest.getImages().stream().map(NestImage::getImageUrl).collect(Collectors.toList()))
                .categoryIds(categoryIds)
                .categoryNames(categoryNames)
                .createdAt(nest.getCreatedAt())
                .isDeleted(nest.getDeletedAt() != null)
                .build();
    }

    public List<AdminCommentResponseDto> getNestCommentsForAdmin(Long nestId) {
        // 0. 둥지 존재 여부 확인 (소프트 삭제된 경우 N001 에러)
        if (!nestRepository.existsById(nestId)) {
            throw new BusinessException(ErrorCode.NEST_NOT_FOUND);
        }

        // 1. 해당 둥지의 모든 댓글 조회 (삭제된 댓글 포함 - 관리자 API는 필터 자동 비활성화)
        List<NestComment> allComments = nestCommentRepository.findAllByNestId(nestId);

        if (allComments.isEmpty()) return List.of();

        // 2. 유저 정보 일괄 조회 (Native Query 결과이므로 LAZY 로딩 방지)
        List<Long> userIds = allComments.stream().map(c -> c.getUser().getId()).distinct().toList();
        Map<Long, String> nicknameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, User::getNickname));

        // 3. 각 댓글별 대기(PENDING) 신고 수 일괄 조회
        List<Long> commentIds = allComments.stream().map(NestComment::getId).toList();
        Map<Long, Long> pendingReportCounts = queryFactory
                .select(report.targetId, report.count())
                .from(report)
                .where(
                        report.reportType.eq(ReportType.COMMENT),
                        report.targetId.in(commentIds),
                        report.status.eq(ReportStatus.PENDING)
                )
                .groupBy(report.targetId)
                .fetch()
                .stream()
                .collect(Collectors.toMap(t -> t.get(report.targetId), t -> t.get(report.count())));

        // 4. 부모 ID를 기준으로 그룹화 (트리 구조 생성용)
        Map<Long, List<NestComment>> childrenMap = allComments.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        // 5. 최상위 댓글 추출 및 정렬 (최신순)
        List<NestComment> topComments = allComments.stream()
                .filter(c -> c.getParent() == null)
                .sorted(Comparator.comparing(NestComment::getCreatedAt).reversed())
                .collect(Collectors.toList());

        // 6. 트리 구조 변환
        return topComments.stream()
                .map(c -> convertToAdminCommentResponseDto(c, childrenMap, nicknameMap, pendingReportCounts))
                .collect(Collectors.toList());
    }

    /**
     * 관리자용 댓글 DTO 변환 (재귀를 통한 트리 구조 생성)
     */
    private AdminCommentResponseDto convertToAdminCommentResponseDto(
            NestComment comment,
            Map<Long, List<NestComment>> childrenMap,
            Map<Long, String> nicknameMap,
            Map<Long, Long> pendingReportCounts) {

        List<NestComment> children = childrenMap.getOrDefault(comment.getId(), Collections.emptyList());
        // 대댓글은 생성순으로 정렬
        children.sort(Comparator.comparing(NestComment::getCreatedAt));

        return AdminCommentResponseDto.builder()
                .commentId(comment.getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .authorNickname(nicknameMap.getOrDefault(comment.getUser().getId(), "알 수 없음"))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .isDeleted(comment.getDeletedAt() != null)
                .pendingReportCount(pendingReportCounts.getOrDefault(comment.getId(), 0L))
                .children(children.stream()
                        .map(child -> convertToAdminCommentResponseDto(child, childrenMap, nicknameMap, pendingReportCounts))
                        .collect(Collectors.toList()))
                .build();
    }

    @Transactional
    public void deleteNestForAdmin(Long nestId, AdminNestDeleteRequestDto requestDto) {
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        // 1. FCM 알림 발송 (비동기 권장되나 FcmService 내부에 @Async 적용됨)
        List<String> tokens = userDeviceRepository.findByUserId(nest.getCreator().getId()).stream()
                .map(UserDevice::getFcmToken)
                .collect(Collectors.toList());
        
        if (!tokens.isEmpty()) {
            String reason = (requestDto.getReason() == null || requestDto.getReason().isBlank()) 
                    ? DEFAULT_NEST_DELETE_REASON 
                    : requestDto.getReason();

            fcmService.sendNotification(new NotificationEvent(
                    tokens,
                    TITLE_NEST_DELETED,
                    reason,
                    Map.of(KEY_TYPE, TYPE_NEST_DELETED, KEY_NEST_ID, nestId.toString())
            ));
        }

        // 2. 연관 엽서 원상복구
        postcardRepository.recoverSharedPostcardsByNest(nest);

        // 3. 연관 소셜 데이터 정리 (좋아요, 리액션) - 하드 딜리트
        List<NestComment> allComments = nestCommentRepository.findAllByNestId(nestId);
        if (!allComments.isEmpty()) {
            commentLikeRepository.deleteByCommentIn(allComments);
        }
        nestReactionRepository.deleteByNest(nest);

        // 4. 하위 댓글 일괄 소프트 삭제
        nestCommentRepository.deleteAllByNest(nest);

        // 5. 둥지 소프트 삭제
        nestRepository.delete(nest);
    }

    @Transactional
    public void deleteCommentForAdmin(Long commentId) {
        NestComment comment = nestCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        
        // 1. 해당 댓글의 좋아요 데이터 정리 (하드 딜리트)
        commentLikeRepository.deleteByComment(comment);

        // 2. 하위 대댓글은 유지하고 해당 댓글만 소프트 삭제
        nestCommentRepository.delete(comment);
    }
}
