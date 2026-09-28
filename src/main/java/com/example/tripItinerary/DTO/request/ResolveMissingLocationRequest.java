package com.example.tripItinerary.DTO.request;

import jakarta.validation.constraints.NotBlank;
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
public class ResolveMissingLocationRequest {

    @NotBlank(message = "OLA place ID is required.")
    private String olaPlaceId;
}