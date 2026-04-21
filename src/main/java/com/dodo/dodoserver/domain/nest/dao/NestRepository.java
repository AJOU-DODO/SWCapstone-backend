package com.dodo.dodoserver.domain.nest.dao;

import com.dodo.dodoserver.domain.nest.dto.NestPinProjection;
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
     * Native Query 사용: ST_Latitude, ST_Longitude, ST_Distance_Sphere 직접 호출
     * Soft Delete(deleted_at IS NULL) 반영
     */
    @Query(value = "SELECT n.id AS id, ST_Latitude(nl.point) AS latitude, ST_Longitude(nl.point) AS longitude " +
                   "FROM nests n " +
                   "JOIN nest_locations nl ON n.id = nl.nest_id " +
                   "WHERE ST_Distance_Sphere(nl.point, :point) <= :radiusMeter " +
                   "AND n.deleted_at IS NULL",
           nativeQuery = true)
    List<NestPinProjection> findNearbyPins(
            @Param("point") Point point,
            @Param("radiusMeter") Double radiusMeter);

    /**
     * 반경 내 및 특정 카테고리 필터링 적용 둥지 목록 조회 (Native Query)
     * 페이징 지원을 위해 countQuery 별도 작성
     */
    @Query(value = "SELECT DISTINCT n.* " +
                   "FROM nests n " +
                   "JOIN nest_locations nl ON n.id = nl.nest_id " +
                   "LEFT JOIN nest_categories nc ON n.id = nc.nest_id " +
                   "WHERE ST_Distance_Sphere(nl.point, :point) <= :radiusMeter " +
                   "AND (:categoryId IS NULL OR nc.category_id = :categoryId) " +
                   "AND n.deleted_at IS NULL",
           countQuery = "SELECT COUNT(DISTINCT n.id) " +
                        "FROM nests n " +
                        "JOIN nest_locations nl ON n.id = nl.nest_id " +
                        "LEFT JOIN nest_categories nc ON n.id = nc.nest_id " +
                        "WHERE ST_Distance_Sphere(nl.point, :point) <= :radiusMeter " +
                        "AND (:categoryId IS NULL OR nc.category_id = :categoryId) " +
                        "AND n.deleted_at IS NULL",
           nativeQuery = true)
    Page<Nest> findNearbyNests(
            @Param("point") Point point,
            @Param("radiusMeter") Double radiusMeter,
            @Param("categoryId") Long categoryId,
            Pageable pageable);

    /**
     * 특정 둥지와 주어진 좌표 사이 거리 계산 (Native Query)
     */
    @Query(value = "SELECT ST_Distance_Sphere(nl.point, :point) " +
                   "FROM nest_locations nl " +
                   "WHERE nl.nest_id = :nestId",
           nativeQuery = true)
    Double calculateDistance(@Param("nestId") Long nestId, @Param("point") Point point);
}
