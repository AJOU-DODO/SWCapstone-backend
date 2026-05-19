package com.dodo.dodoserver.domain.admin.nest.service;

import com.dodo.dodoserver.domain.admin.nest.dto.AdminCommentResponseDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestDeleteRequestDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestDetailResponseDto;
import com.dodo.dodoserver.domain.admin.nest.dto.AdminNestResponseDto;
import com.dodo.dodoserver.domain.nest.dao.NestCategoryRepository;
import com.dodo.dodoserver.domain.nest.dao.NestCommentRepository;
import com.dodo.dodoserver.domain.nest.dao.NestRepository;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.nest.entity.NestCategory;
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
    private final PostcardRepository postcardRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final FcmService fcmService;
    private final JPAQueryFactory queryFactory;

    public Page<AdminNestResponseDto> getNests(
            Pageable pageable, 
            LocalDate startDate, 
            LocalDate endDate, 
            String sort, 
            String includeDeleted) {
        return nestRepository.findNestsForAdmin(pageable, startDate, endDate, sort, includeDeleted);
    }

    public AdminNestDetailResponseDto getNestDetail(Long nestId) {
        // @SQLRestriction을 우회하여 삭제된 둥지도 상세 조회가 가능해야 함
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        List<Long> categoryIds = nestCategoryRepository.findAllByNest(nest).stream()
                .map(nc -> nc.getCategory().getId())
                .collect(Collectors.toList());

        return AdminNestDetailResponseDto.builder()
                .nestId(nest.getId())
                .title(nest.getTitle())
                .content(nest.getContent())
                .authorNickname(nest.getCreator().getNickname())
                .latitude(nest.getLocation().getPoint().getY())
                .longitude(nest.getLocation().getPoint().getX())
                .imageUrls(nest.getImages().stream().map(NestImage::getImageUrl).collect(Collectors.toList()))
                .categoryIds(categoryIds)
                .createdAt(nest.getCreatedAt())
                .build();
    }

    public List<AdminCommentResponseDto> getNestComments(Long nestId) {
        // 1. 해당 둥지의 모든 댓글 조회 (삭제된 댓글 포함)
        List<NestComment> allComments = nestCommentRepository.findAllByNestIdIncludingDeletedNative(nestId);
        
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

        // 4. DTO 변환
        return allComments.stream().map(c -> AdminCommentResponseDto.builder()
                .commentId(c.getId())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .authorNickname(nicknameMap.getOrDefault(c.getUser().getId(), "알 수 없음"))
                .content(c.getContent())
                .createdAt(c.getCreatedAt())
                .isDeleted(c.getDeletedAt() != null)
                .pendingReportCount(pendingReportCounts.getOrDefault(c.getId(), 0L))
                .build()).collect(Collectors.toList());
    }

    @Transactional
    public void deleteNest(Long nestId, AdminNestDeleteRequestDto requestDto) {
        Nest nest = nestRepository.findById(nestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NEST_NOT_FOUND));

        // 1. FCM 알림 발송 (비동기 권장되나 FcmService 내부에 @Async 적용됨)
        List<String> tokens = userDeviceRepository.findByUserId(nest.getCreator().getId()).stream()
                .map(UserDevice::getFcmToken)
                .collect(Collectors.toList());
        
        if (!tokens.isEmpty()) {
            fcmService.sendNotification(new NotificationEvent(
                    tokens,
                    TITLE_NEST_DELETED,
                    requestDto.getReason(),
                    Map.of(KEY_TYPE, TYPE_NEST_DELETED, KEY_NEST_ID, nestId.toString())
            ));
        }

        // 2. 연관 엽서 원상복구
        postcardRepository.recoverSharedPostcardsByNest(nest);

        // 3. 하위 댓글 일괄 소프트 삭제
        nestCommentRepository.deleteAllByNest(nest);

        // 4. 둥지 소프트 삭제
        nestRepository.delete(nest);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        NestComment comment = nestCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        
        // 하위 대댓글은 유지하고 해당 댓글만 소프트 삭제
        nestCommentRepository.delete(comment);
    }
}
