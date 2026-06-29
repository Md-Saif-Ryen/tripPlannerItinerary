package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.Hotel;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByLocationId(Long locationId);

    List<Hotel> findByLocationIdAndActiveTrue(Long locationId);

    List<Hotel> findByStarRating(Integer starRating);

}