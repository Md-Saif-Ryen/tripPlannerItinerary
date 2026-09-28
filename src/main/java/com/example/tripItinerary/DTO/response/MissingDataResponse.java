package com.example.tripItinerary.DTO.response;

import org.springframework.data.domain.Page;

import com.example.tripItinerary.DTO.projection.MissingDataLocationProjection;

public record MissingDataResponse(
        Page<MissingDataLocationProjection> hotels,
        Page<MissingDataLocationProjection> restaurants,
        Page<MissingDataLocationProjection> touristPlaces) {
}