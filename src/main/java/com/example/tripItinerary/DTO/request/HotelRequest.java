package com.example.tripItinerary.DTO.request;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelRequest {

    @NotNull(message = "Location id is required.")
    private Long locationId;

    @NotBlank(message = "Hotel name is required.")
    @Size(max = 255, message = "Hotel name cannot exceed 255 characters.")
    private String hotelName;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters.")
    private String description;

    @Size(max = 500, message = "Address cannot exceed 500 characters.")
    private String address;

    @Builder.Default
    private BigDecimal pricePerNight = BigDecimal.ZERO;

    @Builder.Default
    @Min(value = 1, message = "Hotel weight must be at least 1.")
    private Integer hotelWeight = 1;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.")
    private BigDecimal longitude;

    @Min(value = 1, message = "Star rating must be at least 1.")
    private Integer starRating;

    @Min(value = 1, message = "Total rooms must be at least 1.")
    private Integer totalRooms;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    @Pattern(regexp = "^[0-9+\\-() ]{7,20}$", message = "Invalid contact number.")
    private String contactNumber;

    @Pattern(regexp = "^https?://.*$", message = "Website URL must start with http:// or https://")
    @Size(max = 500)
    private String websiteUrl;

    @Builder.Default
    private Boolean active = true;

    /**
     * Existing amenity IDs.
     */
    private List<Long> amenityIds;

    /**
     * Image URLs.
     */
    private List<String> images;
}