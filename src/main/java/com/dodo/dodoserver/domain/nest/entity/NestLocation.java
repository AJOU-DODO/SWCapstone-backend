package com.dodo.dodoserver.domain.nest.entity;

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
@Table(name = "nest_locations")
@EntityListeners(AuditingEntityListener.class)
public class NestLocation {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "nest_id")
    private Nest nest;

    @Column(nullable = false, precision = 18, scale = 10)
    private Double latitude;

    @Column(nullable = false, precision = 18, scale = 10)
    private Double longitude;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
