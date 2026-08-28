package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.RestaurantReviewRequest;
import com.example.tripItinerary.DTO.response.RestaurantReviewResponse;

public interface RestaurantReviewService {

    RestaurantReviewResponse create(RestaurantReviewRequest request);

    List<RestaurantReviewResponse> getByRestaurant(Long restaurantId);

    void delete(Long id);
}