package com.dodo.dodoserver.domain.ad.entity;

import com.dodo.dodoserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "ad_proposals")
@EntityListeners(AuditingEntityListener.class)
public class AdProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "advertiser_id", nullable = false)
    private User advertiser;

    /**
     * SRID 4326: GPS 좌표계 (WGS84)
     * POINT(longitude, latitude) 순서 저장 주의
     */
    @Column(nullable = false, columnDefinition = "POINT SRID 4326")
    private Point point;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Builder.Default
    @Column(name = "unlock_radius")
    private Integer unlockRadius = 100;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_urls", columnDefinition = "json")
    private List<String> imageUrls;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "category_ids", columnDefinition = "json")
    private List<Long> categoryIds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdProposalStatus status;

    @Column(name = "reject_reason")
    private String rejectReason;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 위도 추출 편의 메서드
    public Double getLatitude() {
        return point != null ? point.getY() : null;
    }

    // 경도 추출 편의 메서드
    public Double getLongitude() {
        return point != null ? point.getX() : null;
    }
}
