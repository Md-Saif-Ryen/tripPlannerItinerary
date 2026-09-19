package com.example.tripItinerary.DTO.projection;

import java.math.BigDecimal;

public interface UserSummaryProjection {

    Long getTotalTrips();

    Long getCompletedTrips();

    BigDecimal getTotalBudget();

    BigDecimal getUsedBudget();
}