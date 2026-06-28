package com.example.tripItinerary.DTO.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryDayResponse {

    private Long id;

    private Integer dayNumber;

    private String title;

    private String notes;

    private List<ItineraryPlaceResponse> itineraryPlaces;

    private LocalDateTime createdAt;

}