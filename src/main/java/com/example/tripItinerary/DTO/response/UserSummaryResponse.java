package com.example.tripItinerary.DTO.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {

    private Integer totalTrips;

    private Integer completedTrips;

    private Integer placesVisited;

    private Integer countriesVisited;

    private BudgetSummaryResponse budget;

    private UpcomingTripResponse upcomingTrip;

    private List<String> travelPreferences;
}