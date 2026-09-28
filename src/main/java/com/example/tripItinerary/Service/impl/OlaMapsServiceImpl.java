package com.example.tripItinerary.Service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.tripItinerary.DTO.response.OlaAutocompleteResponse;
import com.example.tripItinerary.Service.OlaMapsService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OlaMapsServiceImpl implements OlaMapsService {

        private final RestClient restClient;
        private final HttpServletRequest request;

        @Value("${ola.maps.api-key}")
        private String apiKey;

        private static final List<String> ALLOWED_ORIGINS = List.of(
                        "http://localhost:8080",
                        "https://qodenexus.com",
                        "https://tripplanneritinerary.onrender.com"
                       );

        @Override
        public OlaAutocompleteResponse autocomplete(String query) {

                String incomingOrigin = request.getHeader("Origin");

                String allowedOrigin = getAllowedOrigin(incomingOrigin);

                return restClient.get()
                                .uri(uriBuilder -> uriBuilder
                                                .scheme("https")
                                                .host("api.olamaps.io")
                                                .path("/places/v1/autocomplete")
                                                .queryParam("input", query)
                                                .queryParam("api_key", apiKey)
                                                .build())
                                .header("Origin", allowedOrigin)
                                .header("X-Request-Id", UUID.randomUUID().toString())
                                .retrieve()
                                .onStatus(
                                                status -> status.isError(),
                                                (req, response) -> {
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

        private String getAllowedOrigin(String origin) {

                if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
                        return origin;
                }

                return "http://localhost:8080";
        }
}