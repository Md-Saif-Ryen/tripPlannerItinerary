package com.example.tripItinerary.DTO.request;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.example.tripItinerary.enums.PlaceType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryPlaceRequest {

    @NotNull(message = "Itinerary day id is required.")
    private Long itineraryDayId;

    @NotNull(message = "Place type is required.")
    private PlaceType placeType;

    @NotNull(message = "Reference id is required.")
    private Long referenceId;

    @NotNull(message = "Visit order is required.")
    @Min(1)
    private Integer visitOrder;

    private LocalTime plannedStartTime;

    private LocalTime plannedEndTime;

    @Builder.Default
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    @Builder.Default
    private Integer travelTimeMinutes = 0;

    @Size(max = 5000)
    private String notes;

    @Builder.Default
    private Boolean completed = false;

}