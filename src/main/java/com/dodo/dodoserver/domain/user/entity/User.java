package com.dodo.dodoserver.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 프로젝트의 사용자 정보를 저장하는 핵심 엔티티입니다 (MySQL).
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // USER 또는 ADMIN 권한

    @Column(nullable = false)
    private String provider; // 인증 제공자 (예: "google")

    @Column(nullable = false)
    private String providerId; // 제공자로부터 받은 고유 ID

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false; // Soft Delete용 플래그

    @Column(name = "is_onboarded", nullable = false)
    private boolean isOnboarded = false; // 상세 정보 회원 가입

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
