package com.dodo.dodoserver.domain.ad.entity;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "nest_ad_info")
@EntityListeners(AuditingEntityListener.class)
public class NestAdInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nest_id", nullable = false, unique = true)
    private Nest nest;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Builder.Default
    @Column(name = "priority_score", nullable = false)
    private Integer priorityScore = 0;

    @Builder.Default
    @Column(nullable = false)
    private Long impressions = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Long clicks = 0L;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
