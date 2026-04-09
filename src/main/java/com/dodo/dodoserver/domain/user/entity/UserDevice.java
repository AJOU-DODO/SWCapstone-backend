package com.dodo.dodoserver.domain.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 유저의 기기 정보와 FCM 토큰을 관리하는 엔티티입니다.
 * 한 유저가 여러 기기를 가질 수 있으므로 User와 N:1 관계를 가집니다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "user_devices",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "fcm_token"})
    }
)
@EntityListeners(AuditingEntityListener.class)
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token", nullable = false, length = 512)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    private DeviceType deviceType; 

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;
}
