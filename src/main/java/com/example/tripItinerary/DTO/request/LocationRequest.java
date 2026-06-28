package com.example.tripItinerary.DTO.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationRequest {

    @NotBlank(message = "State name is required.")
    @Size(max = 100)
    private String stateName;

    @NotBlank(message = "City name is required.")
    @Size(max = 100)
    private String cityName;

    @NotBlank(message = "Address is required.")
    @Size(max = 255)
    private String address;

    @NotNull(message = "Latitude is required.")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required.")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private BigDecimal longitude;

}