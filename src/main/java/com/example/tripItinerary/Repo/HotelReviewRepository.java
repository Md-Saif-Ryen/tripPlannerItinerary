package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tripItinerary.Entity.HotelReview;

public interface HotelReviewRepository
        extends JpaRepository<HotelReview, Long> {

    List<HotelReview> findByHotelId(Long hotelId);

    List<HotelReview> findByHotelIdOrderByCreatedAtDesc(
            Long hotelId);

    List<HotelReview> findByUserId(Long userId);

    @Query("""
                SELECT AVG(r.rating)
                FROM HotelReview r
                WHERE r.hotel.id = :hotelId
            """)
    Double findAverageRatingByHotelId(
            @Param("hotelId") Long hotelId);
}