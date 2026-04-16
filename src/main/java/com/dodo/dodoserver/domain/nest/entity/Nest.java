package com.dodo.dodoserver.domain.nest.entity;

import com.dodo.dodoserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "nests")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE nests SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class Nest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "unlock_radius")
    private Integer unlockRadius = 0;

    @Builder.Default
    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Builder.Default
    @Column(name = "is_ad")
    private boolean isAd = false;

    @OneToOne(mappedBy = "nest", cascade = CascadeType.ALL, orphanRemoval = true)
    private NestLocation location;

    @Builder.Default
    @OneToMany(mappedBy = "nest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NestImage> images = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 편의 메서드
    public void setLocation(NestLocation location) {
        this.location = location;
        location.setNest(this);
    }

    public void addImage(NestImage image) {
        this.images.add(image);
        image.setNest(this);
    }
}
