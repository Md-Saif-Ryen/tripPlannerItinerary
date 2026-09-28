package com.example.tripItinerary.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "missing_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissingLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_name", nullable = false, unique = true, length = 255)
    private String locationName;

    @Builder.Default
    @Column(name = "search_count", nullable = false)
    private Integer searchCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private Boolean resolved = false;

    private LocalDateTime lastSearchedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Filled only after admin resolves the location

    private Long resolvedLocationId;

    private String resolvedCityName;

    private String resolvedStateName;

    private String olaPlaceId;
}