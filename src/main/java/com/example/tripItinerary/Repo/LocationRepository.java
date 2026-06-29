package com.example.tripItinerary.Repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.tripItinerary.Entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {

    List<Location> findByStateNameIgnoreCase(String stateName);

    List<Location> findByCityNameIgnoreCase(String cityName);

    List<Location> findByStateNameIgnoreCaseAndCityNameIgnoreCase(
            String stateName,
            String cityName);

}