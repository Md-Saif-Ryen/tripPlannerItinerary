package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.RestaurantImageRequest;
import com.example.tripItinerary.DTO.response.RestaurantImageResponse;

public interface RestaurantImageService {

    RestaurantImageResponse create(RestaurantImageRequest request);

    List<RestaurantImageResponse> createBulk(
            List<RestaurantImageRequest> requests);

    RestaurantImageResponse getById(Long id);

    List<RestaurantImageResponse> getByRestaurantId(Long restaurantId);

    RestaurantImageResponse update(
            Long id,
            RestaurantImageRequest request);

    RestaurantImageResponse setPrimary(Long id);

    void delete(Long id);
}