package com.example.tripItinerary.Mapper;

import org.springframework.stereotype.Component;

import com.example.tripItinerary.DTO.response.BudgetSummaryResponse;
import com.example.tripItinerary.DTO.response.ItineraryResponse;
import com.example.tripItinerary.DTO.response.UpcomingTripResponse;
import com.example.tripItinerary.DTO.response.UserSummaryResponse;
import com.example.tripItinerary.enums.ItineraryStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
public class UserSummaryMapper {

    // =========================================================
    // MAIN SUMMARY
    // =========================================================

    public UserSummaryResponse toResponse(
            List<ItineraryResponse> itineraries) {

        if (itineraries == null || itineraries.isEmpty()) {
            return emptyResponse();
        }

        // -----------------------------------------------------
        // TOTAL TRIPS
        // -----------------------------------------------------

        int totalTrips = itineraries.size();

        // -----------------------------------------------------
        // COMPLETED TRIPS
        // -----------------------------------------------------

        int completedTrips = (int) itineraries.stream()
                .filter(this::isCompletedTrip)
                .count();

        // -----------------------------------------------------
        // PLACES VISITED
        // -----------------------------------------------------

        int placesVisited = calculatePlacesVisited(itineraries);

        // -----------------------------------------------------
        // COUNTRIES VISITED
        // -----------------------------------------------------

        int countriesVisited = calculateCountriesVisited(itineraries);

        // -----------------------------------------------------
        // TOTAL BUDGET
        // -----------------------------------------------------

        BigDecimal totalBudget = itineraries.stream()
                .map(ItineraryResponse::getTotalBudget)
                .filter(Objects::nonNull)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        // -----------------------------------------------------
        // USED BUDGET
        //
        // Currently estimatedCost is used as used budget.
        // Later this should come from actual expenses/payments.
        // -----------------------------------------------------

        BigDecimal usedBudget = itineraries.stream()
                .map(ItineraryResponse::getEstimatedCost)
                .filter(Objects::nonNull)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add);

        // -----------------------------------------------------
        // REMAINING BUDGET
        // -----------------------------------------------------

        BigDecimal remainingBudget = totalBudget.subtract(usedBudget);

        if (remainingBudget.compareTo(
                BigDecimal.ZERO) < 0) {

            remainingBudget = BigDecimal.ZERO;
        }

        // -----------------------------------------------------
        // BUDGET PERCENTAGE
        // -----------------------------------------------------

        BigDecimal percentageUsed = calculatePercentage(
                usedBudget,
                totalBudget);

        // -----------------------------------------------------
        // UPCOMING TRIP
        // -----------------------------------------------------

        UpcomingTripResponse upcomingTrip = findUpcomingTrip(itineraries);

        // -----------------------------------------------------
        // FINAL RESPONSE
        // -----------------------------------------------------

        return UserSummaryResponse.builder()

                .totalTrips(totalTrips)

                .completedTrips(
                        completedTrips)

                .placesVisited(
                        placesVisited)

                .countriesVisited(
                        countriesVisited)

                .budget(
                        BudgetSummaryResponse.builder()

                                .total(totalBudget)

                                .used(usedBudget)

                                .remaining(
                                        remainingBudget)

                                .percentageUsed(
                                        percentageUsed)

                                .build())

                .upcomingTrip(
                        upcomingTrip)

                .travelPreferences(
                        List.of())

                .build();
    }

    // =========================================================
    // EMPTY RESPONSE
    // =========================================================

    private UserSummaryResponse emptyResponse() {

        return UserSummaryResponse.builder()

                .totalTrips(0)

                .completedTrips(0)

                .placesVisited(0)

                .countriesVisited(0)

                .budget(
                        BudgetSummaryResponse.builder()

                                .total(
                                        BigDecimal.ZERO)

                                .used(
                                        BigDecimal.ZERO)

                                .remaining(
                                        BigDecimal.ZERO)

                                .percentageUsed(
                                        BigDecimal.ZERO)

                                .build())

                .upcomingTrip(null)

                .travelPreferences(
                        List.of())

                .build();
    }

    // =========================================================
    // COMPLETED TRIPS
    // =========================================================

    private boolean isCompletedTrip(
            ItineraryResponse itinerary) {

        if (itinerary == null) {
            return false;
        }

        return itinerary.getItineraryStatus() == ItineraryStatus.COMPLETED;
    }

    // =========================================================
    // PLACES VISITED
    // =========================================================

    private int calculatePlacesVisited(
            List<ItineraryResponse> itineraries) {

        return (int) itineraries.stream()

                .filter(Objects::nonNull)

                .filter(itinerary -> itinerary.getItineraryDays() != null)

                .flatMap(itinerary -> itinerary
                        .getItineraryDays()
                        .stream())

                .filter(day -> day.getItineraryPlaces() != null)

                .flatMap(day -> day.getItineraryPlaces()
                        .stream())

                // Only completed/visited places
                .filter(place -> Boolean.TRUE.equals(
                        place.getCompleted()))

                // Use referenceId to avoid duplicates
                .map(place -> place.getReferenceId())

                .filter(Objects::nonNull)

                .distinct()

                .count();
    }

    // =========================================================
    // COUNTRIES VISITED
    // =========================================================

    // private int calculateCountriesVisited(
    // List<ItineraryResponse> itineraries) {

    // return (int) itineraries.stream()

    // .filter(Objects::nonNull)

    // .filter(Objects::nonNull)

    // .filter(country -> !country.isBlank())

    // .map(String::trim)

    // .map(String::toLowerCase)

    // .distinct()

    // .count();
    // }

    private int calculateCountriesVisited(
            List<ItineraryResponse> itineraries) {

        return 0;
    }

    // =========================================================
    // UPCOMING TRIP
    // =========================================================

    private UpcomingTripResponse findUpcomingTrip(
            List<ItineraryResponse> itineraries) {

        LocalDate today = LocalDate.now();

        return itineraries.stream()

                .filter(Objects::nonNull)

                .filter(itinerary -> itinerary.getStartDate() != null)

                // Today or future
                .filter(itinerary -> !itinerary
                        .getStartDate()
                        .isBefore(today))

                // Don't show completed trips
                .filter(itinerary -> itinerary.getItineraryStatus() != ItineraryStatus.COMPLETED)

                // Don't show concerned trips
                .filter(itinerary -> itinerary.getItineraryStatus() != ItineraryStatus.CONCERNED)

                // Don't show drafts as upcoming
                .filter(itinerary -> itinerary.getItineraryStatus() != ItineraryStatus.DRAFT)

                // Earliest upcoming trip
                .min(
                        Comparator.comparing(
                                ItineraryResponse::getStartDate))

                .map(
                        this::toUpcomingTrip)

                .orElse(null);
    }

    // =========================================================
    // UPCOMING TRIP MAPPER
    // =========================================================

    private UpcomingTripResponse toUpcomingTrip(
            ItineraryResponse itinerary) {

        return UpcomingTripResponse.builder()

                .id(
                        itinerary.getId())

                .title(
                        itinerary.getTitle())

                .locationName(
                        itinerary.getLocationName())

                .startDate(
                        itinerary.getStartDate())

                .endDate(
                        itinerary.getEndDate())

                .totalDays(
                        itinerary.getTotalDays())

                .totalBudget(
                        itinerary.getTotalBudget())

                .estimatedCost(
                        itinerary.getEstimatedCost())

                .itineraryStatus(
                        itinerary.getItineraryStatus())

                .build();
    }

    // =========================================================
    // BUDGET PERCENTAGE
    // =========================================================

    private BigDecimal calculatePercentage(
            BigDecimal used,
            BigDecimal total) {

        if (used == null ||
                total == null ||
                total.compareTo(
                        BigDecimal.ZERO) <= 0) {

            return BigDecimal.ZERO;
        }

        BigDecimal percentage = used
                .multiply(
                        BigDecimal.valueOf(100))
                .divide(
                        total,
                        2,
                        RoundingMode.HALF_UP);

        // Never allow > 100
        if (percentage.compareTo(
                BigDecimal.valueOf(100)) > 0) {

            return BigDecimal.valueOf(100);
        }

        return percentage;
    }
}