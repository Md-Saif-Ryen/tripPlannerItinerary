    package com.example.tripItinerary.DTO.request;

    import java.math.BigDecimal;
    import java.time.LocalDate;

    import com.example.tripItinerary.enums.TravelType;

    import jakarta.validation.constraints.DecimalMin;
    import jakarta.validation.constraints.Min;
    import jakarta.validation.constraints.NotNull;
    import jakarta.validation.constraints.Size;
    import lombok.*;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class ItineraryRequest {

        @NotNull(message = "User id is required.")
        private Long userId;

        @NotNull(message = "Location id is required.")
        private Long locationId;

        @NotNull(message = "Title is required.")
        @Size(max = 255, message = "Title cannot exceed 255 characters.")
        private String title;

        @Size(max = 5000, message = "Description cannot exceed 5000 characters.")
        private String description;

        @NotNull(message = "Total days is required.")
        @Min(value = 1, message = "Total days must be at least 1.")
        private Integer totalDays;

        @NotNull(message = "Total budget is required.")
        @DecimalMin(value = "0.0", message = "Total budget cannot be negative.")
        private BigDecimal totalBudget;

        @DecimalMin(value = "0.0", message = "Estimated cost cannot be negative.")
        private BigDecimal estimatedCost;

        @NotNull(message = "Travel type is required.")
        private TravelType travelType;

        // Add start date and end date
        private LocalDate startDate;
        private LocalDate endDate;
    }