package com.dodo.dodoserver.domain.admin.user.dto;

import com.dodo.dodoserver.domain.admin.user.entity.AdminEmailWhitelist;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminEmailWhitelistResponseDto {
    private Long id;
    private String email;
    private String remark;
    private LocalDateTime createdAt;

    public static AdminEmailWhitelistResponseDto from(AdminEmailWhitelist entity) {
        return AdminEmailWhitelistResponseDto.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .remark(entity.getRemark())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
