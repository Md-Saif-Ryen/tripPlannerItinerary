package com.example.tripItinerary.Entity;
import java.time.LocalDateTime;


import jakarta.persistence.*;
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

    @Column(nullable = false)
    private String locationName;

    private Integer searchCount = 1;

    private Boolean resolved = false;

    private LocalDateTime lastSearchedAt;

    // getters & setters
}