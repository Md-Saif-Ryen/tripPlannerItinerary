package com.example.tripItinerary.Repo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    Optional<Itinerary> findByIdAndUser_Id(Long id, Long userId);


    List<Itinerary> findByUserIdOrderByCreatedAtDesc(Long userId);

List<Itinerary> findByUserIdAndStartDateAfterOrderByStartDateAsc(Long userId, LocalDate startDate);

List<Itinerary> findByUserIdAndTotalBudgetBetween(Long userId, BigDecimal minBudget, BigDecimal maxBudget);


@Query("SELECT i FROM Itinerary i WHERE i.user.id = :userId " +
       "AND (LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
       "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
List<Itinerary> findByUserIdAndTitleContainingOrDescriptionContaining(
        @Param("userId") Long userId,
        @Param("keyword") String keyword,
        @Param("keyword") String descriptionKeyword);

List<Itinerary> findByUserIdAndItineraryStatus(Long userId, ItineraryStatus status);

}