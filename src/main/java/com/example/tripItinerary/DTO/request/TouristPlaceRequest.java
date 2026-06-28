package com.example.tripItinerary.DTO.request;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.example.tripItinerary.enums.PlaceCategory;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TouristPlaceRequest {

    @NotNull(message = "Location id is required.")
    private Long locationId;

    @NotBlank(message = "Place name is required.")
    @Size(max = 255)
    private String placeName;

    @Size(max = 5000)
    private String description;

    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @Builder.Default
    @Min(1)
    private Integer placeWeight = 1;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private BigDecimal latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private BigDecimal longitude;

    @Builder.Default
    @Min(0)
    private Integer popularityScore = 0;

    @Size(max = 100)
    private String bestVisitMonths;

    @Size(max = 255)
    private String googlePlaceId;

    @Size(max = 500)
    private String address;

    @Size(max = 20)
    private String contactNumber;

    @Size(max = 500)
    private String websiteUrl;

    @Min(1)
    private Integer estimatedVisitTimeMinutes;

    @NotNull(message = "Category is required.")
    private PlaceCategory category;

    private LocalTime openingTime;

    private LocalTime closingTime;

    @Builder.Default
    private Boolean active = true;

}