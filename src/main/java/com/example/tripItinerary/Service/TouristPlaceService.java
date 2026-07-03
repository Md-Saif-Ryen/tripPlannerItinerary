package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.TouristPlaceRequest;
import com.example.tripItinerary.DTO.response.TouristPlaceResponse;

public interface TouristPlaceService {

    TouristPlaceResponse create(TouristPlaceRequest request);

    TouristPlaceResponse update(Long id, TouristPlaceRequest request);

    TouristPlaceResponse getById(Long id);

    List<TouristPlaceResponse> getAll();

    List<TouristPlaceResponse> getByLocation(Long locationId);

    void delete(Long id);

}