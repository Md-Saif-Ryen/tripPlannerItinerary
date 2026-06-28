package com.example.tripItinerary.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantImageResponse {

    private Long id;

    private String imageUrl;

    private Boolean primary;

}