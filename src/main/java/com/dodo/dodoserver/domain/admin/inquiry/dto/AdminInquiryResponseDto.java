package com.dodo.dodoserver.domain.admin.inquiry.dto;

import com.dodo.dodoserver.domain.inquiry.entity.Inquiry;
import com.dodo.dodoserver.domain.inquiry.entity.InquiryStatus;
import com.dodo.dodoserver.domain.inquiry.entity.InquiryType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminInquiryResponseDto {
    private Long id;
    private Long userId;
    private String userNickname;
    private InquiryType type;
    private String typeDescription;
    private String title;
    private InquiryStatus status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime answeredAt;

    public static AdminInquiryResponseDto from(Inquiry inquiry) {
        return AdminInquiryResponseDto.builder()
                .id(inquiry.getId())
                .userId(inquiry.getUser().getId())
                .userNickname(inquiry.getUser().getNickname())
                .type(inquiry.getType())
                .typeDescription(inquiry.getType().getDescription())
                .title(inquiry.getTitle())
                .status(inquiry.getStatus())
                .statusDescription(inquiry.getStatus().getDescription())
                .createdAt(inquiry.getCreatedAt())
                .answeredAt(inquiry.getAnsweredAt())
                .build();
    }
}
