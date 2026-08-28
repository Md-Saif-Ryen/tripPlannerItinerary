package com.example.tripItinerary.DTO.response;

import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationSearchResponse {

    private String query;

    private boolean found;

    private List<LocationResponse> locations;

    private String message;
}