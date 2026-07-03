package com.example.tripItinerary.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "itinerary_days")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItineraryDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= Foreign Key =================
@JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "itinerary_id", nullable = false)
    private Itinerary itinerary;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // ================= Relationships =================
    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "itineraryDay", cascade = CascadeType.ALL,    fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ItineraryPlace> itineraryPlaces = new ArrayList<>();

    public void addPlace(ItineraryPlace place) {
        itineraryPlaces.add(place);
        place.setItineraryDay(this);
    }

    public void removePlace(ItineraryPlace place) {
        itineraryPlaces.remove(place);
        place.setItineraryDay(null);
    }
}