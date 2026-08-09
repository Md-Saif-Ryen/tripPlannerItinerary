package com.example.tripItinerary.DTO.response;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.example.tripItinerary.enums.PlaceType;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryPlaceResponse {

    private Long id;
    private PlaceType placeType;
    private Long referenceId;
    private Integer visitOrder;
    private LocalTime plannedStartTime;
    private LocalTime plannedEndTime;
    private BigDecimal estimatedCost;
    private Integer travelTimeMinutes;
    private String notes;
    private Boolean completed;

    // ✅ Display fields for Hotel, Restaurant, TouristPlace
    private String placeName;
    private String placeAddress;
    private BigDecimal placeRating;
    private String placeImage;
    private BigDecimal placePrice;
    private String contactNumber;
    private String websiteUrl;
}