package com.example.tripItinerary.DTO.request;

import java.math.BigDecimal;
import java.time.LocalTime;

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
public class RestaurantRequest {

    @NotNull(message = "Location id is required.")
    private Long locationId;

    @NotBlank(message = "Restaurant name is required.")
    @Size(max = 255)
    private String restaurantName;

    @Size(max = 5000)
    private String description;

    @Size(max = 500)
    private String address;

    @Builder.Default
    private BigDecimal averageCostPerPerson = BigDecimal.ZERO;

    @Builder.Default
    @Min(value = 1)
    private Integer restaurantWeight = 1;

    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private BigDecimal latitude;

    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private BigDecimal longitude;

    @Size(max = 100)
    private String cuisineType;

    private LocalTime openingTime;

    private LocalTime closingTime;

    @Builder.Default
    private Boolean veg = false;

    @Builder.Default
    private Boolean active = true;

    @Size(max = 20)
    private String contactNumber;

    @Size(max = 500)
    private String websiteUrl;

}