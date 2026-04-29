package com.dodo.dodoserver.domain.postcard.entity;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import com.dodo.dodoserver.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
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
@Table(name = "postcards")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE postcards SET deleted_at = NOW() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Postcard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_author_id", nullable = false)
    private User originalAuthor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_owner_id")
    private User currentOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nest_id")
    private Nest nest;

    @Column(nullable = false)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Builder.Default
    @Column(name = "is_shared", nullable = false)
    private boolean isShared = false;

    @Builder.Default
    @Column(name = "is_exchanged", nullable = false)
    private boolean isExchanged = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 엽서 교환 시 상태 변경 (둥지 -> 유저)
     */
    public void exchangeToUser(User newOwner) {
        this.currentOwner = newOwner;
        this.nest = null;
        this.isShared = false;
        this.isExchanged = true;
    }

    /**
     * 엽서 등록 시 상태 변경 (유저 -> 둥지)
     */
    public void shareToNest(Nest nest) {
        this.currentOwner = null;
        this.nest = nest;
        this.isShared = true;
    }
}
