package com.example.tripItinerary.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.MissingLocation;

public interface MissingLocationRepository
        extends JpaRepository<MissingLocation, Long> {

    Optional<MissingLocation> findByLocationNameIgnoreCase(
            String locationName);

    List<MissingLocation> findByResolvedFalseOrderBySearchCountDesc();
}