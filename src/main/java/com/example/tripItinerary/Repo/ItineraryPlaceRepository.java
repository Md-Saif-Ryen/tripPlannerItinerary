package com.example.tripItinerary.Repo;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tripItinerary.Entity.ItineraryPlace;

public interface ItineraryPlaceRepository
        extends JpaRepository<ItineraryPlace, Long> {

    List<ItineraryPlace> findByItineraryDayIdOrderByVisitOrder(Long itineraryDayId);
    
    void deleteByItineraryDayItineraryId(Long itineraryId);

      // =========================================================
    // OPTIMIZED VISITED PLACES COUNT
    // =========================================================

    @Query("""
        SELECT COUNT(DISTINCT ip.referenceId)

        FROM ItineraryPlace ip

        JOIN ip.itineraryDay d

        JOIN d.itinerary i

        WHERE i.user.id = :userId

        AND ip.completed = true
    """)
    long countVisitedPlacesByUserId(
            @Param("userId") Long userId
    );
}