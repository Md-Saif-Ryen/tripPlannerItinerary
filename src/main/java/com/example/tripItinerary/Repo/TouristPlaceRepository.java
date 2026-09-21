package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tripItinerary.Entity.TouristPlace;
import com.example.tripItinerary.enums.PlaceCategory;

public interface TouristPlaceRepository
        extends JpaRepository<TouristPlace, Long> {

    // ============================================================
    // EXISTING METHODS
    // ============================================================

    List<TouristPlace> findByLocationId(Long locationId);

    List<TouristPlace> findByCategory(
            PlaceCategory category);

    List<TouristPlace> findByLocationIdOrderByPopularityScoreDesc(
            Long locationId);

    List<TouristPlace> findTop20ByLocationIdOrderByAverageRatingDescPopularityScoreDesc(
            Long locationId);

    List<TouristPlace> findByLocationIdAndActiveTrueOrderByPopularityScoreDescAverageRatingDescPlaceWeightDesc(
            Long locationId);

    List<TouristPlace> findByLocationIdAndActiveTrue(
            Long locationId);

    List<TouristPlace> findByLocationIdAndCategory(
            Long locationId,
            PlaceCategory category);

    // ============================================================
    // PAGINATED
    // ============================================================

    Page<TouristPlace> findAll(
            Pageable pageable);

    Page<TouristPlace> findByLocationId(
            Long locationId,
            Pageable pageable);

    // ============================================================
    // FAST COUNT - ALL TOURIST PLACES
    // ============================================================

    @Query("""
            SELECT COUNT(tp)
            FROM TouristPlace tp
            """)
    long countAllTouristPlaces();

    // ============================================================
    // FAST COUNT - BY LOCATION
    // ============================================================

    @Query("""
            SELECT COUNT(tp)
            FROM TouristPlace tp
            WHERE tp.location.id = :locationId
            """)
    long countByLocationId(
            @Param("locationId") Long locationId);

    // ============================================================
    // FAST COUNT - ACTIVE TOURIST PLACES
    // ============================================================

    @Query("""
            SELECT COUNT(tp)
            FROM TouristPlace tp
            WHERE tp.active = true
            """)
    long countActiveTouristPlaces();

    // ============================================================
    // FAST COUNT - ACTIVE BY LOCATION
    // ============================================================

    @Query("""
            SELECT COUNT(tp)
            FROM TouristPlace tp
            WHERE tp.location.id = :locationId
            AND tp.active = true
            """)
    long countActiveByLocationId(
            @Param("locationId") Long locationId);
}