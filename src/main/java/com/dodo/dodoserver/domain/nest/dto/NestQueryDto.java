package com.dodo.dodoserver.domain.nest.dto;

import com.dodo.dodoserver.domain.nest.entity.Nest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NestQueryDto {
    private Nest nest;
    private Long likeCount;
    private Double distance;
}
