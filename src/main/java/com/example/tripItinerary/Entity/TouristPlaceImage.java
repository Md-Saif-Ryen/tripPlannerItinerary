package com.example.tripItinerary.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tourist_place_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TouristPlaceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= Foreign Key =================
@JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_place_id", nullable = false)
    private TouristPlace touristPlace;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Builder.Default
    @Column(name = "is_primary")
    private Boolean primary = false;

}