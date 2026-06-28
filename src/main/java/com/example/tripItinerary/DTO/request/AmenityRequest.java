package com.example.tripItinerary.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmenityRequest {

    @NotBlank(message = "Amenity name is required.")
    @Size(max = 100, message = "Amenity name cannot exceed 100 characters.")
    private String name;

}