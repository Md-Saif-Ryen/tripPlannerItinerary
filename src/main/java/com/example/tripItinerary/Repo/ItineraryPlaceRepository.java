package com.example.tripItinerary.Repo;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.ItineraryPlace;

public interface ItineraryPlaceRepository
        extends JpaRepository<ItineraryPlace, Long> {

    List<ItineraryPlace> findByItineraryDayIdOrderByVisitOrder(Long itineraryDayId);
    
    void deleteByItineraryDayItineraryId(Long itineraryId);

}