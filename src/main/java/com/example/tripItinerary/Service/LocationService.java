package com.example.tripItinerary.Service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.example.tripItinerary.DTO.request.LocationRequest;
import com.example.tripItinerary.DTO.request.MissingLocationRequest;
import com.example.tripItinerary.DTO.request.ResolveMissingLocationRequest;
import com.example.tripItinerary.DTO.response.LocationNameResponse;
import com.example.tripItinerary.DTO.response.LocationResponse;
import com.example.tripItinerary.DTO.response.MissingDataResponse;
import com.example.tripItinerary.DTO.response.MostSearchedLocationResponse;
import com.example.tripItinerary.DTO.response.OlaAutocompleteResponse;
import com.example.tripItinerary.Entity.MissingLocation;

public interface LocationService {

    LocationResponse create(LocationRequest request);

    LocationResponse update(Long id, LocationRequest request);

    LocationResponse getById(Long id);

    List<LocationResponse> getAll();

    List<LocationNameResponse> getByLocationName();

    List<LocationNameResponse> searchLocations(String query);

    List<LocationResponse> getChildren(Long parentId);

    List<LocationResponse> getDescendants(Long locationId);

    List<Long> getDescendantLocationIds(Long locationId);

    void delete(Long id);

    List<MostSearchedLocationResponse> getTopSearchedLocations();

    List<MissingLocation> getPendingLocations();

    MissingDataResponse getLocationsMissingData(Pageable pageable);

    MissingLocation requestMissingLocation(
            MissingLocationRequest request);

    OlaAutocompleteResponse getMissingLocationAutocomplete(
            Long missingLocationId);

    MissingLocation resolveMissingLocation(
            Long missingLocationId,
            ResolveMissingLocationRequest request);
}