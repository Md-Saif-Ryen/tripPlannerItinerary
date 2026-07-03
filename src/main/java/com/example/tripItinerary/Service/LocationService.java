package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.LocationRequest;
import com.example.tripItinerary.DTO.response.LocationResponse;

public interface LocationService {

    LocationResponse create(LocationRequest request);

    LocationResponse update(Long id, LocationRequest request);

    LocationResponse getById(Long id);

    List<LocationResponse> getAll();

    void delete(Long id);

}