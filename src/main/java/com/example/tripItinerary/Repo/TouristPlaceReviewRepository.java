package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.TouristPlaceReview;

public interface TouristPlaceReviewRepository
        extends JpaRepository<TouristPlaceReview, Long> {

    List<TouristPlaceReview> findByTouristPlaceId(Long touristPlaceId);

    List<TouristPlaceReview> findByUserId(Long userId);

}