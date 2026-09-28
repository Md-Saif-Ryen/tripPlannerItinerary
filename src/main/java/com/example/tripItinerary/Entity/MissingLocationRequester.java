package com.example.tripItinerary.Entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "missing_location_requesters", uniqueConstraints = {
        @UniqueConstraint(name = "uk_missing_location_user", columnNames = {
                "missing_location_id",
                "user_id"
        })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissingLocationRequester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "missing_location_id", nullable = false)
    private MissingLocation missingLocation;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", nullable = false, length = 255)
    private String userName;

    @Column(name = "fcm_token", length = 1000)
    private String fcmToken;

    @Builder.Default
    @Column(nullable = false)
    private Boolean notified = false;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime notifiedAt;
}