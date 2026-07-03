package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.TouristPlace;
import com.example.tripItinerary.enums.PlaceCategory;

public interface TouristPlaceRepository extends JpaRepository<TouristPlace, Long> {

    List<TouristPlace> findByLocationId(Long locationId);

    List<TouristPlace> findByCategory(PlaceCategory category);

    List<TouristPlace> findByLocationIdOrderByPopularityScoreDesc(Long locationId);

List<TouristPlace> findTop20ByLocationIdOrderByAverageRatingDescPopularityScoreDesc(Long locationId);

    List<TouristPlace> findByLocationIdAndActiveTrueOrderByPopularityScoreDescAverageRatingDescPlaceWeightDesc(
            Long locationId);

    List<TouristPlace> findByLocationIdAndActiveTrue(Long locationId);

    List<TouristPlace> findByLocationIdAndCategory(
            Long locationId,
            PlaceCategory category);

          

}