package com.example.tripItinerary.DTO.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryDayRequest {

    @NotNull(message = "Itinerary id is required.")
    private Long itineraryId;

    @NotNull(message = "Day number is required.")
    @Min(1)
    private Integer dayNumber;

    @Size(max = 255)
    private String title;

    @Size(max = 5000)
    private String notes;

}