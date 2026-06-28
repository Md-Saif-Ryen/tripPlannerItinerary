package com.example.tripItinerary.DTO.response;



import java.math.BigDecimal;
import java.time.LocalDateTime;

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
public class LocationResponse {

    private Long id;

    private String stateName;

    private String cityName;

    private String address;

    private BigDecimal latitude;

    private BigDecimal longitude;

    private LocalDateTime createdAt;

}