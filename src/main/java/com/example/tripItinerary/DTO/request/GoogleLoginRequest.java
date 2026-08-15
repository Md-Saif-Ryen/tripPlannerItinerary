package com.example.tripItinerary.DTO.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(

        @NotBlank(message = "Google ID token is required") String idToken

) {
}