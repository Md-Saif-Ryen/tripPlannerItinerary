package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.ItineraryRequest;
import com.example.tripItinerary.DTO.response.ItineraryResponse;

public interface ItineraryService {

    ItineraryResponse create(ItineraryRequest request);

    ItineraryResponse update(Long id, ItineraryRequest request);

    ItineraryResponse getById(Long id);

    List<ItineraryResponse> getMyItineraries();

    void delete(Long id);

}