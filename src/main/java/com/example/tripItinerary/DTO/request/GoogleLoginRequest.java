package com.example.tripItinerary.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GoogleLoginRequest {

        @NotBlank(message = "Google ID token is required")
        private String idToken;


    @Size(max = 500)
    private String fcmToken;
}