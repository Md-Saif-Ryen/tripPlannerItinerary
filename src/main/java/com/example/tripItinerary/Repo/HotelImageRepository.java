package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.HotelImage;

public interface HotelImageRepository
        extends JpaRepository<HotelImage, Long> {

    List<HotelImage> findByHotelId(Long hotelId);

}