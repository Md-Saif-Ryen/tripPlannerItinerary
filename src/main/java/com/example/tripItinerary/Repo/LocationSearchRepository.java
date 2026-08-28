package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.tripItinerary.Entity.LocationSearch;

public interface LocationSearchRepository
        extends JpaRepository<LocationSearch, Long> {

    // Search history of a particular user
    List<LocationSearch> findByUserIdOrderBySearchedAtDesc(
            Long userId);

    // Top searched locations
    @Query("""
                SELECT ls.normalizedQuery, COUNT(ls.id)
                FROM LocationSearch ls
                GROUP BY ls.normalizedQuery
                ORDER BY COUNT(ls.id) DESC
            """)
    List<Object[]> findMostSearchedLocations();
}