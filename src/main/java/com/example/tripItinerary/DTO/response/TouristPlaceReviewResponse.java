package com.example.tripItinerary.DTO.response;

import java.time.LocalDateTime;

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
public class TouristPlaceReviewResponse {

    private Long id;

    private Long touristPlaceId;

    private Long userId;

    private String userName;

    private Integer rating;

    private String reviewText;

    private LocalDateTime createdAt;

}