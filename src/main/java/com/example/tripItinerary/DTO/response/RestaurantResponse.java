package com.example.tripItinerary.DTO.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponse {

    private Long id;

    private Long locationId;

    private String restaurantName;

    private String description;

    private String address;

    private BigDecimal averageCostPerPerson;

    private BigDecimal averageRating;

    private Integer restaurantWeight;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private String cuisineType;

    private LocalTime openingTime;

    private LocalTime closingTime;

    private Boolean veg;

    private Boolean active;

    private String contactNumber;

    private String websiteUrl;

    private List<RestaurantImageResponse> images;

    private List<RestaurantReviewResponse> reviews;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}