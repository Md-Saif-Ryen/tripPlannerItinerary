package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.TouristPlaceImageRequest;
import com.example.tripItinerary.DTO.response.TouristPlaceImageResponse;

public interface TouristPlaceImageService {

    // ============================================================
    // CREATE
    // ============================================================

    TouristPlaceImageResponse create(
            TouristPlaceImageRequest request);

    // ============================================================
    // BULK CREATE
    // ============================================================

    List<TouristPlaceImageResponse> createBulk(
            List<TouristPlaceImageRequest> requests);

    // ============================================================
    // GET BY ID
    // ============================================================

    TouristPlaceImageResponse getById(Long id);

    // ============================================================
    // GET BY TOURIST PLACE
    // ============================================================

    List<TouristPlaceImageResponse> getByTouristPlaceId(
            Long touristPlaceId);

    // ============================================================
    // UPDATE
    // ============================================================

    TouristPlaceImageResponse update(
            Long id,
            TouristPlaceImageRequest request);

    // ============================================================
    // SET PRIMARY
    // ============================================================

    TouristPlaceImageResponse setPrimary(Long id);

    // ============================================================
    // DELETE
    // ============================================================

    void delete(Long id);
}