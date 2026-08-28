package com.example.tripItinerary.Entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "location_searches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User can be null if search is made by guest
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "search_query", nullable = false, length = 255)
    private String searchQuery;

    @Column(name = "normalized_query", nullable = false, length = 255)
    private String normalizedQuery;

    @CreationTimestamp
    @Column(name = "searched_at", updatable = false)
    private LocalDateTime searchedAt;
}