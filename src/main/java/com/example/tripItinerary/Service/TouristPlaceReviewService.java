package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.TouristPlaceReviewRequest;
import com.example.tripItinerary.DTO.response.TouristPlaceReviewResponse;

public interface TouristPlaceReviewService {

    // ============================================================
    // CREATE
    // ============================================================

    TouristPlaceReviewResponse create(
            TouristPlaceReviewRequest request);

    // ============================================================
    // GET BY ID
    // ============================================================

    TouristPlaceReviewResponse getById(Long id);

    // ============================================================
    // GET BY TOURIST PLACE
    // ============================================================

    List<TouristPlaceReviewResponse> getByTouristPlaceId(
            Long touristPlaceId);

    // ============================================================
    // GET BY USER
    // ============================================================

    List<TouristPlaceReviewResponse> getByUserId(
            Long userId);

    // ============================================================
    // UPDATE
    // ============================================================

    TouristPlaceReviewResponse update(
            Long id,
            TouristPlaceReviewRequest request);

    // ============================================================
    // DELETE
    // ============================================================

    void delete(Long id);
}