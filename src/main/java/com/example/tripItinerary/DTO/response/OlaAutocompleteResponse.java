package com.example.tripItinerary.DTO.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class OlaAutocompleteResponse {

    private String status;

    @JsonProperty("error_message")
    private String errorMessage;

    private List<OlaPredictionResponse> predictions;
}