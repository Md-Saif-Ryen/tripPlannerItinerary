package com.example.tripItinerary.Repo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.tripItinerary.Entity.TemporaryItinerary;

@Repository
public interface TemporaryItineraryRepository
        extends JpaRepository<TemporaryItinerary, Long> {

    Optional<TemporaryItinerary> findBySelectionIdAndUserId(
            String selectionId,
            Long userId);

    List<TemporaryItinerary> findByExpiresAtBefore(
            LocalDateTime dateTime);

    @Modifying
    @Query("""
                DELETE FROM TemporaryItinerary t
                WHERE t.expiresAt < :now
            """)
    int deleteExpired(
            LocalDateTime now);
}