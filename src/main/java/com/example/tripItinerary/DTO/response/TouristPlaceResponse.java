package com.example.tripItinerary.DTO.response;
        

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.example.tripItinerary.enums.PlaceCategory;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TouristPlaceResponse {

    private Long id;

    private Long locationId;

    private String placeName;

    private String description;

    private BigDecimal price;

    private Integer placeWeight;

    private BigDecimal averageRating;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer popularityScore;

    private String bestVisitMonths;

    private String googlePlaceId;

    private String address;

    private String contactNumber;

    private String websiteUrl;

    private Integer estimatedVisitTimeMinutes;

    private PlaceCategory category;

    private LocalTime openingTime;

    private LocalTime closingTime;

    private Boolean active;

    private List<TouristPlaceImageResponse> images;

    private List<TouristPlaceReviewResponse> reviews;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}