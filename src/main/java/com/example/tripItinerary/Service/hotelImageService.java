package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.HotelImageRequest;
import com.example.tripItinerary.DTO.response.HotelImageResponse;

public interface hotelImageService {

    HotelImageResponse create(
            HotelImageRequest request);
            
    List<HotelImageResponse> createBulk(
                    List<HotelImageRequest> requests);

    HotelImageResponse getById(
            Long id);

    List<HotelImageResponse> getByHotelId(
            Long hotelId);

    HotelImageResponse update(
            Long id,
            HotelImageRequest request);

    HotelImageResponse setPrimary(
            Long id);

    void delete(
            Long id);
}