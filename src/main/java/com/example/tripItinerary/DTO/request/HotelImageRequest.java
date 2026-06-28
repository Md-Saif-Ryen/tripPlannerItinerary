package com.example.tripItinerary.DTO.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelImageRequest {

    @NotNull(message = "Hotel id is required.")
    private Long hotelId;

    @NotBlank(message = "Image URL is required.")
    @Size(max = 1000)
    private String imageUrl;

    @Builder.Default
    private Boolean primary = false;

}