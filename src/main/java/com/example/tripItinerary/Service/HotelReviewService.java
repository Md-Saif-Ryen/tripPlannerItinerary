package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.HotelReviewRequest;
import com.example.tripItinerary.DTO.response.HotelReviewResponse;

public interface HotelReviewService {

    HotelReviewResponse create(
            HotelReviewRequest request);

    HotelReviewResponse getById(
            Long id);

    List<HotelReviewResponse> getByHotelId(
            Long hotelId);

    HotelReviewResponse update(
            Long id,
            HotelReviewRequest request);

    void delete(
            Long id);
}