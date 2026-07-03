package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.RestaurantRequest;
import com.example.tripItinerary.DTO.response.RestaurantResponse;

public interface RestaurantService {

    RestaurantResponse create(RestaurantRequest request);

    RestaurantResponse update(Long id, RestaurantRequest request);

    RestaurantResponse getById(Long id);

    List<RestaurantResponse> getAll();

    List<RestaurantResponse> getByLocation(Long locationId);

    void delete(Long id);

}