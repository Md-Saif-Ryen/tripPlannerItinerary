package com.example.tripItinerary.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tripItinerary.Entity.HotelImage;

public interface HotelImageRepository
                extends JpaRepository<HotelImage, Long> {

        List<HotelImage> findByHotelId(Long hotelId);

        Optional<HotelImage> findByHotelIdAndPrimaryTrue(
                        Long hotelId);

        @Modifying
        @Query("""
                            update HotelImage h
                            set h.primary = false
                            where h.hotel.id = :hotelId
                        """)
        void clearPrimaryByHotelId(
                        @Param("hotelId") Long hotelId);
}