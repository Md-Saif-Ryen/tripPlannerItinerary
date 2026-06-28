package com.example.tripItinerary.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    // Flutter friendly fields
    private String placeName;

    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String imageUrl;

    private Integer visitOrder;

    private LocalTime plannedStartTime;

    private LocalTime plannedEndTime;

    private BigDecimal estimatedCost;

    private Integer travelTimeMinutes;

    private String notes;

    private Boolean completed;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}