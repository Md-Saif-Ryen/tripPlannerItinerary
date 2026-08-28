package com.example.tripItinerary.DTO.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MostSearchedLocationResponse {

    private String searchQuery;

    private Long searchCount;

    private Integer rank;
}