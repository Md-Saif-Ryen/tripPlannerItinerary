package com.example.tripItinerary.Service;

import java.util.List;

import org.springframework.data.domain.Page;

import com.example.tripItinerary.DTO.request.TouristPlaceRequest;
import com.example.tripItinerary.DTO.response.TouristPlaceResponse;

public interface TouristPlaceService {

    TouristPlaceResponse create(TouristPlaceRequest request);

    TouristPlaceResponse update(Long id, TouristPlaceRequest request);

    TouristPlaceResponse getById(Long id);

    List<TouristPlaceResponse> getAll();

    List<TouristPlaceResponse> getByLocation(Long locationId);

    void delete(Long id);

    Page<TouristPlaceResponse> getAll(
            int page,
            int size,
            String sortBy,
            String direction);

    /**
     * Get tourist places by location with pagination.
     */
    Page<TouristPlaceResponse> getByLocation(
            Long locationId,
            int page,
            int size,
            String sortBy,
            String direction);

    long countAll();

    long countByLocation(Long locationId);

    long countActive();

    long countActiveByLocation(Long locationId);
}