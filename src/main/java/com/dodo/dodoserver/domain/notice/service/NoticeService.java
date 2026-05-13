package com.dodo.dodoserver.domain.notice.service;

import com.dodo.dodoserver.domain.notice.dao.NoticeRepository;
import com.dodo.dodoserver.domain.notice.dto.NoticeResponseDto;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     * 사용자용 공지사항 목록 조회 (발행된 항목만)
     */
    public Page<NoticeResponseDto> getPublishedNotices(Pageable pageable) {
        return noticeRepository.findAllByIsPublishedTrue(pageable)
                .map(NoticeResponseDto::from);
    }

    /**
     * 사용자용 공지사항 상세 조회 (발행된 항목만)
     */
    public NoticeResponseDto getNoticeDetail(Long noticeId) {
        return noticeRepository.findByIdAndIsPublishedTrue(noticeId)
                .map(NoticeResponseDto::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }
}
