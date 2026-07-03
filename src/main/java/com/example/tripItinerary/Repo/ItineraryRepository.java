package com.example.tripItinerary.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.tripItinerary.Entity.Itinerary;
import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.enums.ItineraryStatus;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

    @EntityGraph(attributePaths = {
            "location",
            "itineraryDays",
            "itineraryDays.itineraryPlaces"
    })
    Optional<Itinerary> findWithDetailsById(Long id);

    List<Itinerary> findByUserOrderByCreatedAtDesc(User user);

    List<Itinerary> findByUserAndItineraryStatusOrderByCreatedAtDesc(
            User user,
            ItineraryStatus status);

    boolean existsByIdAndUser(Long id, User user);

    List<Itinerary> findByUserId(
            Long userId);
    Optional<Itinerary> findByIdAndUser(Long id, Long userId);

}