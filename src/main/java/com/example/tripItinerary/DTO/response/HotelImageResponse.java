package com.example.tripItinerary.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelImageResponse {

    private Long id;

    private String imageUrl;

    private Boolean primary;

}