package com.example.tripItinerary.DTO.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OlaPredictionResponse {

    private String reference;

    private List<String> types;

    private List<OlaTermResponse> terms;

    private String description;

    @JsonProperty("place_id")
    private String placeId;

    private OlaGeometryResponse geometry;

    private List<String> layer;
}