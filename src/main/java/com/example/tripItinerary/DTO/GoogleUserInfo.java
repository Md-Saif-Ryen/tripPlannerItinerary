package com.example.tripItinerary.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleUserInfo {

    private String googleId;

    private String email;

    private String name;

    private String picture;
}