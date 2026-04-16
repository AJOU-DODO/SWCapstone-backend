package com.dodo.dodoserver.domain.nest.entity;

import com.dodo.dodoserver.domain.user.entity.User;
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
@Table(
    name = "unlock_histories",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "nest_id"})
    }
)
@EntityListeners(AuditingEntityListener.class)
public class UnlockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nest_id", nullable = false)
    private Nest nest;

    @Column(nullable = false, columnDefinition = "POINT SRID 4326")
    private Point verifiedPoint;

    @CreatedDate
    @Column(name = "unlocked_at", updatable = false)
    private LocalDateTime unlockedAt;
}
