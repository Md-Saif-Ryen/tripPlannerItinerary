package com.example.tripItinerary.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.tripItinerary.enums.ItineraryStatus;
import com.example.tripItinerary.enums.TravelType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "itineraries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Itinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= Foreign Keys =================
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    // ================= Details =================

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    @Column(name = "total_budget", precision = 10, scale = 2)
    private BigDecimal totalBudget;

    @Column(name = "estimated_cost", precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "remaining_budget", precision = 10, scale = 2)
    private BigDecimal remainingBudget;

    @Enumerated(EnumType.STRING)
    @Column(name = "travel_type")
    private TravelType travelType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "itinerary_status")
    private ItineraryStatus itineraryStatus = ItineraryStatus.GENERATED;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= Relationships =================
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "itinerary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItineraryDay> itineraryDays = new ArrayList<>();

    public void addDay(ItineraryDay day) {
        itineraryDays.add(day);
        day.setItinerary(this);
    }

    public void removeDay(ItineraryDay day) {
        itineraryDays.remove(day);
        day.setItinerary(null);
    }
}