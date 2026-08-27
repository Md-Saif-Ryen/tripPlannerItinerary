package com.example.tripItinerary.DTO.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.example.tripItinerary.enums.ItineraryStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingTripResponse {

    private Long id;

    private String title;

    private String locationName;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer totalDays;

    private BigDecimal totalBudget;

    private BigDecimal estimatedCost;
private ItineraryStatus itineraryStatus;
}