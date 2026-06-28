package com.example.tripItinerary.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.tripItinerary.enums.PlaceCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tourist_places")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TouristPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= Foreign Key =================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    // ================= Basic Details =================

    @Column(name = "place_name", nullable = false)
    private String placeName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "place_weight")
    private Integer placeWeight = 1;

    @Builder.Default
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Builder.Default
    @Column(name = "popularity_score")
    private Integer popularityScore = 0;

    @Column(name = "best_visit_months")
    private String bestVisitMonths;

    @Column(name = "google_place_id")
    private String googlePlaceId;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "website_url", length = 500)
    private String websiteUrl;

    @Column(name = "estimated_visit_time_minutes")
    private Integer estimatedVisitTimeMinutes;

    @Enumerated(EnumType.STRING)
    private PlaceCategory category;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ================= Relationships =================
@JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "touristPlace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TouristPlaceImage> images = new ArrayList<>();
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "touristPlace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TouristPlaceReview> reviews = new ArrayList<>();

}