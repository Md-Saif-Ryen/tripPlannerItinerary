package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.TouristPlaceImage;

public interface TouristPlaceImageRepository
        extends JpaRepository<TouristPlaceImage, Long> {

    List<TouristPlaceImage> findByTouristPlaceId(Long touristPlaceId);

}