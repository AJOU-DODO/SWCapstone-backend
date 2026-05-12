package com.dodo.dodoserver.domain.admin.user.entity;

import com.dodo.dodoserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "sanction_histories")
@EntityListeners(AuditingEntityListener.class)
public class SanctionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "sanction_type", nullable = false)
    private SanctionType sanctionType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "ended_at")
    private LocalDateTime endedAt; // 제재가 끝나는 시점 (null이면 영구정지 판단 가능 혹은 산출값 저장)

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
