package com.example.tripItinerary.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.tripItinerary.enums.PlaceType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "itinerary_places", indexes = {
        @Index(name = "idx_day_order", columnList = "itinerary_day_id, visit_order"),
        @Index(name = "idx_reference", columnList = "place_type, reference_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===================== Foreign Key =====================
@JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_day_id", nullable = false)
    private ItineraryDay itineraryDay;

    // ===================== Reference =====================

    @Enumerated(EnumType.STRING)
    @Column(name = "place_type", nullable = false)
    private PlaceType placeType;

    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    // ===================== Planning =====================

    @Column(name = "visit_order", nullable = false)
    private Integer visitOrder;

    @Column(name = "planned_start_time")
    private LocalTime plannedStartTime;

    @Column(name = "planned_end_time")
    private LocalTime plannedEndTime;

    @Builder.Default
    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "travel_time_minutes")
    private Integer travelTimeMinutes = 0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(name = "is_completed")
    private Boolean completed = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}