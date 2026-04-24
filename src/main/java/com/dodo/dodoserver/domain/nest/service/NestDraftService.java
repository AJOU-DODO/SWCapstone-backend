package com.dodo.dodoserver.domain.nest.service;

import com.dodo.dodoserver.domain.nest.dao.NestDraftRepository;
import com.dodo.dodoserver.domain.nest.dto.*;
import com.dodo.dodoserver.domain.nest.entity.NestDraft;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NestDraftService {

    private final NestDraftRepository nestDraftRepository;
    private final UserRepository userRepository;
    private final NestService nestService;

    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * 임시 저장 생성
     */
    @Transactional
    public NestDraftResponseDto createDraft(Long userId, NestDraftCreateRequestDto requestDto) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Point point = geometryFactory.createPoint(new Coordinate(requestDto.getLongitude(), requestDto.getLatitude()));

        NestDraft draft = NestDraft.builder()
                .creator(creator)
                .point(point)
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .unlockRadius(requestDto.getUnlockRadius() != null ? requestDto.getUnlockRadius() : 100)
                .imageUrls(requestDto.getImageUrls())
                .categoryIds(requestDto.getCategoryIds())
                .build();

        NestDraft savedDraft = nestDraftRepository.save(draft);
        log.info("임시 저장 생성 완료: ID={}, User={}", savedDraft.getId(), userId);
        return NestDraftResponseDto.from(savedDraft);
    }

    /**
     * 내 임시 저장 목록 조회
     */
    @Transactional(readOnly = true)
    public List<NestDraftResponseDto> getMyDrafts(Long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return nestDraftRepository.findAllByCreatorOrderByCreatedAtDesc(creator).stream()
                .map(NestDraftResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 임시 저장 상세 조회
     */
    @Transactional(readOnly = true)
    public NestDraftResponseDto getDraftDetail(Long userId, Long draftId) {
        NestDraft draft = getDraftIfOwner(userId, draftId);
        return NestDraftResponseDto.from(draft);
    }

    /**
     * 임시 저장 수정
     */
    @Transactional
    public NestDraftResponseDto updateDraft(Long userId, Long draftId, NestDraftUpdateRequestDto requestDto) {
        NestDraft draft = getDraftIfOwner(userId, draftId);

        if (requestDto.getTitle() != null) draft.setTitle(requestDto.getTitle());
        if (requestDto.getContent() != null) draft.setContent(requestDto.getContent());
        if (requestDto.getUnlockRadius() != null) draft.setUnlockRadius(requestDto.getUnlockRadius());
        if (requestDto.getImageUrls() != null) draft.setImageUrls(requestDto.getImageUrls());
        if (requestDto.getCategoryIds() != null) draft.setCategoryIds(requestDto.getCategoryIds());

        log.info("임시 저장 수정 완료: ID={}", draftId);
        return NestDraftResponseDto.from(draft);
    }

    /**
     * 임시 저장 삭제
     */
    @Transactional
    public void deleteDraft(Long userId, Long draftId) {
        NestDraft draft = getDraftIfOwner(userId, draftId);
        nestDraftRepository.delete(draft);
        log.info("임시 저장 삭제 완료: ID={}", draftId);
    }

    /**
     * 임시 저장 데이터를 둥지로 정식 발행
     */
    @Transactional
    public NestSummaryResponseDto publishDraft(Long userId, Long draftId) {
        NestDraft draft = getDraftIfOwner(userId, draftId);

        // 발행 시 필수 값 검증 (예: 제목)
        if (draft.getTitle() == null || draft.getTitle().isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE); // 혹은 적절한 에러 코드
        }

        // NestService의 createNest를 활용하기 위해 DTO 변환
        NestCreateRequestDto nestCreateRequestDto = new NestCreateRequestDto(
                draft.getTitle(),
                draft.getContent(),
                draft.getLatitude(),
                draft.getLongitude(),
                draft.getUnlockRadius(),
                draft.getCategoryIds(),
                draft.getImageUrls(),
                false // isAd 기본값
        );

        NestSummaryResponseDto response = nestService.createNest(userId, nestCreateRequestDto);

        // 발행 성공 시 임시 저장 데이터 삭제
        nestDraftRepository.delete(draft);
        log.info("임시 저장 발행 완료: DraftID={}, NestID={}", draftId, response.getId());

        return response;
    }

    private NestDraft getDraftIfOwner(Long userId, Long draftId) {
        NestDraft draft = nestDraftRepository.findById(draftId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DRAFT_NOT_FOUND));

        if (!draft.getCreator().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.HANDLE_ACCESS_DENIED);
        }
        return draft;
    }
}
