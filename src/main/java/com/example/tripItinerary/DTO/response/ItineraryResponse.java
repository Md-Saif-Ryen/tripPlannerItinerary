package com.example.tripItinerary.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.tripItinerary.enums.ItineraryStatus;
import com.example.tripItinerary.enums.TravelType;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryResponse {

    private Long id;

    private Long userId;
    private Long locationId;
    private String locationName;

    private String title;
    private String description;
    private Integer totalDays;
    private BigDecimal totalBudget;
    private BigDecimal estimatedCost;
    private BigDecimal remainingBudget; // Added

    private TravelType travelType;
    private ItineraryStatus itineraryStatus;

    private LocalDate startDate; // Added
    private LocalDate endDate; // Added

    private List<ItineraryDayResponse> itineraryDays;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}