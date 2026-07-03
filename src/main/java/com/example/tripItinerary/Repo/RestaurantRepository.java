package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    List<Restaurant> findByLocationId(Long locationId);

    List<Restaurant> findByLocationIdAndActiveTrue(Long locationId);

    List<Restaurant> findByVegTrue();
    
    List<Restaurant> findByLocationIdAndActiveTrueOrderByAverageRatingDesc(
            Long locationId);

}