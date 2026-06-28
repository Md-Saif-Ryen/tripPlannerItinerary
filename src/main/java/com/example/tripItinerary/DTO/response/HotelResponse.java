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
public class HotelResponse {

    private Long id;

    private Long locationId;

    private String hotelName;

    private String description;

    private String address;

    private BigDecimal pricePerNight;

    private BigDecimal averageRating;

    private Integer hotelWeight;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private Integer starRating;

    private Integer totalRooms;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private String contactNumber;

    private String websiteUrl;

    private Boolean active;

    private List<AmenityResponse> amenities;

    private List<HotelImageResponse> images;

    private List<HotelReviewResponse> reviews;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}