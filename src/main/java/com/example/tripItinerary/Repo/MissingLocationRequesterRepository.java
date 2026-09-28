package com.example.tripItinerary.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.MissingLocationRequester;

public interface MissingLocationRequesterRepository
        extends JpaRepository<MissingLocationRequester, Long> {

    Optional<MissingLocationRequester> findByMissingLocationIdAndUserId(
            Long missingLocationId,
            Long userId);

    List<MissingLocationRequester> findByMissingLocationId(Long missingLocationId);

    List<MissingLocationRequester> findByMissingLocationIdAndNotifiedFalse(
            Long missingLocationId);
}