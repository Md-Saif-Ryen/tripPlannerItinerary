package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.RestaurantImage;

public interface RestaurantImageRepository
        extends JpaRepository<RestaurantImage, Long> {

    List<RestaurantImage> findByRestaurantId(Long restaurantId);

}