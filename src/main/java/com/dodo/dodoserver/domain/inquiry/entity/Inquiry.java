package com.dodo.dodoserver.domain.inquiry.entity;

import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.error.ErrorCode;
import com.dodo.dodoserver.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "inquiries")
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE inquiries SET deleted_at = NOW() WHERE id = ?")
@FilterDef(name = "inquiryFilter")
@Filter(name = "inquiryFilter", condition = "deleted_at IS NULL")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private InquiryStatus status = InquiryStatus.PENDING;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;

    /**
     * 문의 수정
     */
    public void update(InquiryType type, String title, String content) {
        validateNotCompleted();
        this.type = type;
        this.title = title;
        this.content = content;
    }

    /**
     * 삭제 시 검증
     */
    public void validateForDelete() {
        validateNotCompleted();
    }

    /**
     * 관리자 답변 등록
     */
    public void addAnswer(String answer) {
        this.answer = answer;
        this.status = InquiryStatus.COMPLETED;
        this.answeredAt = LocalDateTime.now();
    }

    private void validateNotCompleted() {
        if (this.status == InquiryStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.CANNOT_MODIFY_COMPLETED_INQUIRY);
        }
    }

    public void validateOwner(User user) {
        if (!this.user.getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NOT_INQUIRY_OWNER);
        }
    }
}
