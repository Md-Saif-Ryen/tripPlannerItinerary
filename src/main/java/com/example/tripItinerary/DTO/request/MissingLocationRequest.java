package com.example.tripItinerary.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissingLocationRequest {

    @NotBlank(message = "Location query is required.")
    private String query;

    @NotNull(message = "User ID is required.")
    private Long userId;

    @NotBlank(message = "User name is required.")
    private String userName;

    @NotBlank(message = "FCM token is required.")
    private String fcmToken;
}