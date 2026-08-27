package com.example.tripItinerary.Entity;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "temporary_itineraries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemporaryItinerary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ======================================================
    // USER
    // ======================================================

    @Column(nullable = false)
    private Long userId;

    // ======================================================
    // FRONTEND SELECTION ID
    // ======================================================

    @Column(name = "selection_id", nullable = false, unique = true, length = 100)
    private String selectionId;

    // ======================================================
    // OPTION NUMBER
    // ======================================================

    @Column(nullable = false)
    private Integer optionNumber;

    // ======================================================
    // COMPLETE GENERATED ITINERARY JSON
    // ======================================================

    @Lob
    @Column(name = "itinerary_data", nullable = false, columnDefinition = "LONGTEXT")
    private String itineraryData;

    // ======================================================
    // CREATED
    // ======================================================

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ======================================================
    // EXPIRATION
    // ======================================================

    @Column(nullable = false)
    private LocalDateTime expiresAt;
}
