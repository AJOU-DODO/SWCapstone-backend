package com.dodo.dodoserver.domain.nest.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "nest_locations")
@EntityListeners(AuditingEntityListener.class)
public class NestLocation {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "nest_id")
    private Nest nest;

    /**
     * SRID 4326: GPS 좌표계 (WGS84)
     * POINT(longitude, latitude) 순서로 저장됨에 주의
     */
    @Column(nullable = false, columnDefinition = "POINT SRID 4326")
    private Point point;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // 편의 메서드: 위도 추출
    public Double getLatitude() {
        return point != null ? point.getY() : null;
    }

    // 편의 메서드: 경도 추출
    public Double getLongitude() {
        return point != null ? point.getX() : null;
    }
}
