package com.example.tripItinerary.Service;

import java.math.BigDecimal;
import java.util.List;

import com.example.tripItinerary.DTO.request.ItineraryRequest;
import com.example.tripItinerary.DTO.response.ItineraryResponse;
import com.example.tripItinerary.DTO.response.UserSummaryResponse;
import com.example.tripItinerary.enums.ItineraryStatus;

public interface ItineraryService {

    // Core CRUD operations
    List<ItineraryResponse> create(ItineraryRequest request);

    ItineraryResponse update(Long id, ItineraryRequest request);

    ItineraryResponse getById(Long id);

    List<ItineraryResponse> getMyItineraries();

    void delete(Long id);

    // Additional Business Methods
    BigDecimal calculateRemainingBudget(Long itineraryId);

    boolean isItineraryOwner(Long itineraryId);

    ItineraryResponse updateStatus(Long id, ItineraryStatus status);

    List<ItineraryResponse> getItinerariesByStatus(ItineraryStatus status);

    List<ItineraryResponse> getUpcomingItineraries();

    List<ItineraryResponse> getItinerariesWithinBudget(BigDecimal minBudget, BigDecimal maxBudget);

    List<ItineraryResponse> searchItineraries(String keyword);
    List<ItineraryResponse> getItineraryByUserId(Long userId );
     UserSummaryResponse getUserSummaryByUserId(Long userId );

     ItineraryResponse selectGeneratedItinerary(
             Long userId,
             String selectionId);

     ItineraryResponse markPlaceCompleted(
             Long itineraryPlaceId);
    
}