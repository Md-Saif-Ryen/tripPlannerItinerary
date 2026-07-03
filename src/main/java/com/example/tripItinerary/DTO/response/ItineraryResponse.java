package com.example.tripItinerary.DTO.response;

import java.math.BigDecimal;
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

    private String locationName;
    private Long userId;

    private Long locationId;

    private String title;

    private String description;

    private Integer totalDays;

    private BigDecimal totalBudget;

    private BigDecimal estimatedCost;

    private TravelType travelType;

    private ItineraryStatus itineraryStatus;

    private List<ItineraryDayResponse> itineraryDays;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}