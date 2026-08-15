package com.example.tripItinerary.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private boolean success;

    private String message;

    private String token;

    private String tokenType;
    private String fcmToken;

    private UserResponse user;

}