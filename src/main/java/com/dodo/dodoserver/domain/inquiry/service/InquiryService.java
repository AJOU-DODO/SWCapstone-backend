package com.dodo.dodoserver.domain.inquiry.service;

import com.dodo.dodoserver.domain.inquiry.dao.InquiryRepository;
import com.dodo.dodoserver.domain.inquiry.dto.InquiryRequestDto;
import com.dodo.dodoserver.domain.inquiry.dto.InquiryResponseDto;
import com.dodo.dodoserver.domain.inquiry.entity.Inquiry;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import com.dodo.dodoserver.domain.user.entity.User;
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
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    @Transactional
    public InquiryResponseDto createInquiry(Long userId, InquiryRequestDto requestDto) {
        User user = findUserById(userId);
        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .type(requestDto.getType())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .build();

        return InquiryResponseDto.from(inquiryRepository.save(inquiry));
    }

    public Page<InquiryResponseDto> getMyInquiries(Long userId, Pageable pageable) {
        User user = findUserById(userId);
        return inquiryRepository.findAllByUser(user, pageable)
                .map(InquiryResponseDto::from);
    }

    @Transactional
    public InquiryResponseDto updateInquiry(Long userId, Long inquiryId, InquiryRequestDto requestDto) {
        User user = findUserById(userId);
        Inquiry inquiry = findById(inquiryId);
        inquiry.validateOwner(user);
        inquiry.update(requestDto.getType(), requestDto.getTitle(), requestDto.getContent());

        return InquiryResponseDto.from(inquiry);
    }

    @Transactional
    public void deleteInquiry(Long userId, Long inquiryId) {
        User user = findUserById(userId);
        Inquiry inquiry = findById(inquiryId);
        inquiry.validateOwner(user);
        inquiry.validateForDelete();

        inquiryRepository.delete(inquiry);
    }

    private Inquiry findById(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
