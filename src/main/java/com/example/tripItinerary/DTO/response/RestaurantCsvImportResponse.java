package com.example.tripItinerary.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantCsvImportResponse {

    private Integer totalRows;

    private Integer inserted;

    private Integer updated;

    private Integer skipped;

    private Integer failed;

    @Builder.Default
    private List<String> errors = new ArrayList<>();
}