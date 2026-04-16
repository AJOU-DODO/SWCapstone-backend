package com.dodo.dodoserver.domain.nest.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "nest_images")
public class NestImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nest_id", nullable = false)
    private Nest nest;

    @Column(name = "image_url", nullable = false, length = 2083)
    private String imageUrl;

    @Builder.Default
    @Column(name = "sort_order")
    private Integer sortOrder = 1;
}
