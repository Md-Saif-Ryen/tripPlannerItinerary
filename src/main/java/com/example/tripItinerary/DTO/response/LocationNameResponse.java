package com.example.tripItinerary.DTO.response;

import com.example.tripItinerary.enums.LocationType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationNameResponse {

    private Long id;
    private String cityName;
    private String stateName;
    private LocationType locationType;
}