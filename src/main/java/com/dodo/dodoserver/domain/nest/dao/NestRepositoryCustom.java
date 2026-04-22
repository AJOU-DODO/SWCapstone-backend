package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.dto.NestPinResponseDto;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NestRepositoryCustom {
    List<NestPinResponseDto> findNearbyPins(Point point, Double radiusMeter);
    Page<Nest> findNearbyNests(Point point, Double radiusMeter, Long categoryId, Pageable pageable);
    Double calculateDistance(Long nestId, Point point);
}
