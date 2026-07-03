package com.example.tripItinerary.DTO.request;

import java.math.BigDecimal;

import com.example.tripItinerary.enums.TravelType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryRequest {

    @NotNull(message = "User id is required.")
    private Long userId;

    @NotNull(message = "Location id is required.")
    private Long locationId;

    @Size(max = 255)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotNull(message = "Total days is required.")
    @Min(value = 1)
    private Integer totalDays;

    @DecimalMin(value = "0.0")
    private BigDecimal totalBudget;

    @DecimalMin(value = "0.0")
    private BigDecimal estimatedCost;

    @NotNull(message = "Travel type is required.")
    private TravelType travelType;

}