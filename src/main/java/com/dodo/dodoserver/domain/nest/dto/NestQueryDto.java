package com.dodo.dodoserver.domain.nest.dto;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NestQueryDto {
    private Nest nest;
    private Long likeCount;
    private Double distance;
    private List<String> categoryNames;

    public NestQueryDto(Nest nest, Long likeCount, Double distance) {
        this.nest = nest;
        this.likeCount = likeCount;
        this.distance = distance;
    }

    public NestQueryDto(Nest nest, Object likeCount, Double distance) {
        this.nest = nest;
        this.likeCount = (likeCount instanceof Long) ? (Long) likeCount : 
                         (likeCount instanceof Number) ? ((Number) likeCount).longValue() : 0L;
        this.distance = distance;
    }
}
