package com.dodo.dodoserver.domain.ad.dto;

import com.dodo.dodoserver.domain.ad.entity.AdProposal;
import com.dodo.dodoserver.domain.ad.entity.AdProposalStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AdProposalResponseDto {
    private Long id;
    private String title;
    private String content;
    private Double latitude;
    private Double longitude;
    private Integer unlockRadius;
    private List<String> imageUrls;
    private List<Long> categoryIds;
    private List<String> categoryNames;
    private AdProposalStatus status;
    private String rejectReason;
    private LocalDateTime createdAt;

    public static AdProposalResponseDto from(AdProposal proposal, List<String> categoryNames) {
        return AdProposalResponseDto.builder()
                .id(proposal.getId())
                .title(proposal.getTitle())
                .content(proposal.getContent())
                .latitude(proposal.getLatitude())
                .longitude(proposal.getLongitude())
                .unlockRadius(proposal.getUnlockRadius())
                .imageUrls(proposal.getImageUrls())
                .categoryIds(proposal.getCategoryIds())
                .categoryNames(categoryNames)
                .status(proposal.getStatus())
                .rejectReason(proposal.getRejectReason())
                .createdAt(proposal.getCreatedAt())
                .build();
    }

    public static AdProposalResponseDto from(AdProposal proposal) {
        return from(proposal, null);
    }
}
