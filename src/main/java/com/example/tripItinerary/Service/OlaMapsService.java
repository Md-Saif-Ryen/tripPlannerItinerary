package com.example.tripItinerary.Service;

import com.example.tripItinerary.DTO.response.OlaAutocompleteResponse;

public interface OlaMapsService {

    OlaAutocompleteResponse autocomplete(String query);
}