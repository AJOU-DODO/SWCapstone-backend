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
     * 특정 좌표(Point) 기준 반경(radiusMeter) 내의 모든 둥지 핀 정보를 조회합니다.
     * ST_Distance_Sphere는 미터(m) 단위로 거리를 계산합니다.
     */
    @Query(value = "SELECT new com.dodo.dodoserver.domain.nest.dto.NestPinResponseDto(nl.nest.id, ST_Y(nl.point), ST_X(nl.point)) " +
                   "FROM NestLocation nl " +
                   "WHERE ST_Distance_Sphere(nl.point, :point) <= :radiusMeter")
    List<NestPinResponseDto> findNearbyPins(
            @Param("point") Point point,
            @Param("radiusMeter") Double radiusMeter);

    /**
     * 반경 내 + 특정 카테고리 필터링이 적용된 둥지 목록을 조회합니다.
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
     * 특정 둥지와 주어진 좌표 사이의 거리를 계산하여 반환합니다.
     */
    @Query(value = "SELECT ST_Distance_Sphere(nl.point, :point) " +
                   "FROM NestLocation nl " +
                   "WHERE nl.nest.id = :nestId")
    Double calculateDistance(@Param("nestId") Long nestId, @Param("point") Point point);
}
