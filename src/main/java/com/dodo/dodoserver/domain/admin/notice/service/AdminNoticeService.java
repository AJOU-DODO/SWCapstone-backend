package com.dodo.dodoserver.domain.admin.notice.service;

import com.dodo.dodoserver.domain.admin.notice.dao.NoticeAdminRepository;
import com.dodo.dodoserver.domain.admin.notice.dto.NoticeRequestDto;
import com.dodo.dodoserver.domain.notice.dao.NoticeRepository;
import com.dodo.dodoserver.domain.notice.dto.NoticeResponseDto;
import com.dodo.dodoserver.domain.notice.entity.Notice;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminNoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeAdminRepository noticeAdminRepository;
    private final JobLauncher jobLauncher;
    private final Job noticePublishJob;

    /**
     * 공지사항 등록 (초안)
     */
    @Transactional
    public Long createNotice(NoticeRequestDto requestDto) {
        Notice notice = Notice.builder()
                .category(requestDto.getCategory())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .isPublished(false)
                .build();
        return noticeRepository.save(notice).getId();
    }

    /**
     * 공지사항 수정
     */
    @Transactional
    public void updateNotice(Long noticeId, NoticeRequestDto requestDto) {
        Notice notice = noticeAdminRepository.findByIdWithDeleted(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        
        notice.update(requestDto.getCategory(), requestDto.getTitle(), requestDto.getContent());
    }

    /**
     * 공지사항 삭제 (Soft Delete)
     */
    @Transactional
    public void deleteNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
        noticeRepository.delete(notice);
    }

    /**
     * 공지사항 발행 및 FCM 발송 (Batch Job 호출)
     */
    @Transactional
    public void publishNotice(Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));

        if (notice.isPublished()) {
            throw new BusinessException(ErrorCode.ALREADY_PUBLISHED_NOTICE);
        }

        notice.publish();
        
        // Batch Job 실행 (비동기 위임)
        runPublishJobAsync(notice.getTitle(), notice.getCategory().getDescription());
    }

    /**
     * 관리자용 공지사항 목록 조회 (삭제된 항목 포함)
     */
    public Page<NoticeResponseDto> getAllNotices(Pageable pageable) {
        return noticeAdminRepository.findAllNoticesWithDeleted(pageable)
                .map(NoticeResponseDto::from);
    }

    /**
     * 관리자용 공지사항 상세 조회 (삭제된 항목 포함)
     */
    public NoticeResponseDto getNoticeDetail(Long noticeId) {
        return noticeAdminRepository.findByIdWithDeleted(noticeId)
                .map(NoticeResponseDto::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }

    @Async("fcmExecutor")
    public void runPublishJobAsync(String title, String content) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("title", title)
                    .addString("content", content)
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();

            jobLauncher.run(noticePublishJob, jobParameters);
            log.info("Successfully launched Notice Publish Batch Job for: {}", title);
        } catch (Exception e) {
            log.error("Failed to launch Notice Publish Batch Job", e);
        }
    }
}
