package com.dodo.dodoserver.domain.nest.dao.querydsl;

import com.dodo.dodoserver.domain.nest.dto.NestPinResponseDto;
import com.dodo.dodoserver.domain.nest.dto.NestQueryDto;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NestRepositoryCustom {
    List<NestPinResponseDto> findNearbyPins(Point point, Double radiusMeter);
    Page<NestQueryDto> findNearbyNests(Point point, Double radiusMeter, List<Long> categoryIds, Pageable pageable);
    Double calculateDistance(Long nestId, Point point);
}
