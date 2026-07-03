package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.ItineraryDay;

public interface ItineraryDayRepository
        extends JpaRepository<ItineraryDay, Long> {

    List<ItineraryDay> findByItineraryIdOrderByDayNumber(Long itineraryId);
    
    void deleteByItineraryId(Long itineraryId);
    

}