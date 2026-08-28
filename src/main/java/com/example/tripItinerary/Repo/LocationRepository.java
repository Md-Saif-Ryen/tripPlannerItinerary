package com.example.tripItinerary.Repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tripItinerary.Entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByStateNameIgnoreCase(String stateName);

    List<Location> findByCityNameIgnoreCase(String cityName);

  
    List<Location> findByStateNameIgnoreCaseAndCityNameIgnoreCase(
            String stateName,
            String cityName);

            List<Location> findByCityNameContainingIgnoreCase(String query);

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

}

// Optional<Location> findByNameIgnoreCase(String name);

// boolean existsByNameIgnoreCase(String name);