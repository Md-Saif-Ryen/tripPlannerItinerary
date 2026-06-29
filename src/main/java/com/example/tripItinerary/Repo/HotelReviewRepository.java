package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.HotelReview;

public interface HotelReviewRepository
        extends JpaRepository<HotelReview, Long> {

    List<HotelReview> findByHotelId(Long hotelId);

    List<HotelReview> findByUserId(Long userId);

}