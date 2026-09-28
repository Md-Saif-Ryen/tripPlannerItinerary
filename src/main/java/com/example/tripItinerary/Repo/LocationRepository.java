package com.example.tripItinerary.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tripItinerary.DTO.projection.MissingDataLocationProjection;
import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Entity.MissingLocation;
import com.example.tripItinerary.enums.LocationType;

public interface LocationRepository
        extends JpaRepository<Location, Long> {

    // ============================================================
    // EXISTING SEARCH
    // ============================================================

    List<Location> findByStateNameIgnoreCase(String stateName);

    List<Location> findByCityNameIgnoreCase(String cityName);

    List<Location> findByStateNameIgnoreCaseAndCityNameIgnoreCase(
            String stateName,
            String cityName);

            Optional<Location> findByCityNameIgnoreCaseAndStateNameIgnoreCase(
            String cityName,
            String stateName);

    List<Location> findByCityNameContainingIgnoreCase(
            String query);

    // ============================================================
    // HIERARCHY
    // ============================================================

    List<Location> findByParentId(Long parentId);

    List<Location> findByParentIdOrderByCityNameAsc(Long parentId);

    List<Location> findByParentIdAndLocationType(
            Long parentId,
            LocationType locationType);

    List<Location> findByLocationType(
            LocationType locationType);

    // ============================================================
    // SEARCH
    // ============================================================

    @Query("""
                SELECT l
                FROM Location l
                WHERE LOWER(l.cityName)
                    LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(l.stateName)
                    LIKE LOWER(CONCAT('%', :query, '%'))
                   OR LOWER(l.address)
                    LIKE LOWER(CONCAT('%', :query, '%'))
                ORDER BY l.cityName ASC
            """)
    List<Location> searchLocations(
            @Param("query") String query);

    // ============================================================
    // MISSING LOCATION
    // ============================================================

    @Query("""
                SELECT m
                FROM MissingLocation m
                WHERE LOWER(m.locationName)
                    = LOWER(:locationName)
            """)
    Optional<MissingLocation> findMissingLocationByName(
            @Param("locationName") String locationName);

    @Query("""
                SELECT m
                FROM MissingLocation m
                WHERE m.resolved = false
                ORDER BY m.searchCount DESC
            """)
    List<MissingLocation> findPendingMissingLocations();

    @Query(value = """
            SELECT
                l.id AS locationId,
                l.city_name AS cityName,
                COUNT(h.id) AS dataCount
            FROM locations l
            LEFT JOIN hotels h
                ON h.location_id = l.id
            GROUP BY l.id, l.city_name
            HAVING COUNT(h.id) = 0
            ORDER BY l.city_name ASC
            """, countQuery = """
            SELECT COUNT(*)
            FROM (
                SELECT l.id
                FROM locations l
                LEFT JOIN hotels h
                    ON h.location_id = l.id
                GROUP BY l.id, l.city_name
                HAVING COUNT(h.id) = 0
            ) x
            """, nativeQuery = true)
    Page<MissingDataLocationProjection> findLocationsWithoutHotels(Pageable pageable);

    @Query(value = """
            SELECT
                l.id AS locationId,
                l.city_name AS cityName,
                COUNT(r.id) AS dataCount
            FROM locations l
            LEFT JOIN restaurants r
                ON r.location_id = l.id
                AND r.is_active = true
            GROUP BY l.id, l.city_name
            HAVING COUNT(r.id) = 0
            ORDER BY l.city_name ASC
            """, countQuery = """
            SELECT COUNT(*)
            FROM (
                SELECT l.id
                FROM locations l
                LEFT JOIN restaurants r
                    ON r.location_id = l.id
                    AND r.is_active = true
                GROUP BY l.id, l.city_name
                HAVING COUNT(r.id) = 0
            ) x
            """, nativeQuery = true)
    Page<MissingDataLocationProjection> findLocationsWithoutRestaurants(Pageable pageable);

    @Query(value = """
            SELECT
                l.id AS locationId,
                l.city_name AS cityName,
                COUNT(tp.id) AS dataCount
            FROM locations l
            LEFT JOIN tourist_places tp
                ON tp.location_id = l.id
                AND tp.is_active = true
                AND tp.is_deleted = false
            GROUP BY l.id, l.city_name
            HAVING COUNT(tp.id) = 0
            ORDER BY l.city_name ASC
            """, countQuery = """
            SELECT COUNT(*)
            FROM (
                SELECT l.id
                FROM locations l
                LEFT JOIN tourist_places tp
                    ON tp.location_id = l.id
                    AND tp.is_active = true
                    AND tp.is_deleted = false
                GROUP BY l.id, l.city_name
                HAVING COUNT(tp.id) = 0
            ) x
            """, nativeQuery = true)
    Page<MissingDataLocationProjection> findLocationsWithoutTouristPlaces(Pageable pageable);
}