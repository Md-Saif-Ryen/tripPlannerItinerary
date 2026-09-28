package com.example.tripItinerary.Service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.tripItinerary.DTO.response.OlaAutocompleteResponse;
import com.example.tripItinerary.Service.OlaMapsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OlaMapsServiceImpl implements OlaMapsService {

    private final RestClient restClient;

    @Value("${ola.maps.api-key}")
    private String apiKey;

    @Override
    public OlaAutocompleteResponse autocomplete(String query) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.olamaps.io")
                        .path("/places/v1/autocomplete")
                        .queryParam("input", query)
                        .queryParam("api_key", apiKey)
                        .build())
                .header("Origin", "http://localhost:8080")
                .header("X-Request-Id", UUID.randomUUID().toString())
                .retrieve()
                .onStatus(
                        status -> status.isError(),
                        (request, response) -> {

                            String body = new String(
                                    response.getBody().readAllBytes(),
                                    java.nio.charset.StandardCharsets.UTF_8);

                            System.out.println(
                                    "OLA STATUS: " + response.getStatusCode());

                            System.out.println(
                                    "OLA RESPONSE: " + body);
                        })
                .body(OlaAutocompleteResponse.class);
    }
}