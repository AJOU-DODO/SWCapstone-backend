package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.dto.NestPinResponseDto;
import com.dodo.dodoserver.domain.nest.entity.Nest;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NestRepository extends JpaRepository<Nest, Long> {

    /**
     * 특정 좌표(Point) 기준 반경(radiusMeter) 내 모든 둥지 핀 정보 조회
     * ST_Distance_Sphere 미터(m) 단위 거리 계산
     */
    @Query(value = "SELECT new com.dodo.dodoserver.domain.nest.dto.NestPinResponseDto(nl.nest.id, ST_Y(nl.point), ST_X(nl.point)) " +
                   "FROM NestLocation nl " +
                   "WHERE ST_Distance_Sphere(nl.point, :point) <= :radiusMeter")
    List<NestPinResponseDto> findNearbyPins(
            @Param("point") Point point,
            @Param("radiusMeter") Double radiusMeter);

    /**
     * 반경 내 및 특정 카테고리 필터링 적용 둥지 목록 조회
     */
    @Query(value = "SELECT n " +
                   "FROM Nest n " +
                   "JOIN n.location nl " +
                   "LEFT JOIN NestCategory nc ON nc.nest = n " +
                   "WHERE ST_Distance_Sphere(nl.point, :point) <= :radiusMeter " +
                   "AND (:categoryId IS NULL OR nc.category.id = :categoryId) " +
                   "GROUP BY n.id")
    Page<Nest> findNearbyNests(
            @Param("point") Point point,
            @Param("radiusMeter") Double radiusMeter,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    /**
     * 특정 둥지와 주어진 좌표 사이 거리 계산
     */
    @Query(value = "SELECT ST_Distance_Sphere(nl.point, :point) " +
                   "FROM NestLocation nl " +
                   "WHERE nl.nest.id = :nestId")
    Double calculateDistance(@Param("nestId") Long nestId, @Param("point") Point point);
}
