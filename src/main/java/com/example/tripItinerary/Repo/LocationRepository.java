package com.example.tripItinerary.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Entity.MissingLocation;

public interface LocationRepository extends JpaRepository<Location, Long> {

        List<Location> findByStateNameIgnoreCase(String stateName);

        List<Location> findByCityNameIgnoreCase(String cityName);

        List<Location> findByStateNameIgnoreCaseAndCityNameIgnoreCase(
                        String stateName,
                        String cityName);

        List<Location> findByCityNameContainingIgnoreCase(String query);

        // ============================================================
        // SEARCH LOCATIONS
        // ============================================================

        @Query("""
                        SELECT l
                        FROM Location l
                        WHERE LOWER(l.cityName) LIKE LOWER(CONCAT('%', :query, '%'))
                           OR LOWER(l.stateName) LIKE LOWER(CONCAT('%', :query, '%'))
                           OR LOWER(l.address) LIKE LOWER(CONCAT('%', :query, '%'))
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
                        WHERE LOWER(m.locationName) = LOWER(:locationName)
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

}