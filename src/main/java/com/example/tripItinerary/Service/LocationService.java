package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.LocationRequest;
import com.example.tripItinerary.DTO.response.LocationNameResponse;
import com.example.tripItinerary.DTO.response.LocationResponse;
import com.example.tripItinerary.DTO.response.MostSearchedLocationResponse;
import com.example.tripItinerary.Entity.MissingLocation;

public interface LocationService {

    LocationResponse create(LocationRequest request);

    LocationResponse update(Long id, LocationRequest request);

    LocationResponse getById(Long id);

    List<LocationResponse> getAll();
    List<LocationNameResponse> getByLocationName();
    
    List<LocationNameResponse> searchLocations(String query);

    void delete(Long id);
    List<MostSearchedLocationResponse> getTopSearchedLocations();
    List<MissingLocation> getPendingLocations();

}