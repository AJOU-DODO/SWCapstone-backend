package com.dodo.dodoserver.domain.admin.user.dto;

import com.dodo.dodoserver.domain.user.entity.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAdminResponseDto {
    private Long id;
    private String nickname;
    private String email;
    private Role role;
    private LocalDateTime createdAt;
    private Long nestCount;
    private Long commentCount;
    private LocalDateTime sanctionedUntil;
    private Boolean isSanctioned; // 현재 시간 기준으로 산출

    public UserAdminResponseDto(Long id, String nickname, String email, Role role, LocalDateTime createdAt, 
                               Long nestCount, Long commentCount, LocalDateTime sanctionedUntil) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.nestCount = nestCount;
        this.commentCount = commentCount;
        this.sanctionedUntil = sanctionedUntil;
        this.isSanctioned = sanctionedUntil != null && sanctionedUntil.isAfter(LocalDateTime.now());
    }
}
