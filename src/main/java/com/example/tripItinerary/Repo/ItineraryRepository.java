package com.example.tripItinerary.Repo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.tripItinerary.DTO.projection.UserSummaryProjection;
import com.example.tripItinerary.Entity.Itinerary;
import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.enums.ItineraryStatus;

@Repository
public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {

        // =========================================================
        // EXISTING METHODS
        // =========================================================

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

        boolean existsByIdAndUser(
                        Long id,
                        User user);

        List<Itinerary> findByUserId(
                        Long userId);

        Optional<Itinerary> findByIdAndUser_Id(
                        Long id,
                        Long userId);

        List<Itinerary> findByUserIdOrderByCreatedAtDesc(
                        Long userId);

        List<Itinerary> findByUserIdAndStartDateAfterOrderByStartDateAsc(
                        Long userId,
                        LocalDate startDate);

        List<Itinerary> findByUserIdAndTotalBudgetBetween(
                        Long userId,
                        BigDecimal minBudget,
                        BigDecimal maxBudget);

        List<Itinerary> findByUserIdAndItineraryStatus(
                        Long userId,
                        ItineraryStatus status);

        // =========================================================
        // OPTIMIZED USER SUMMARY
        // =========================================================

        @Query("""
                            SELECT
                                COUNT(i.id) AS totalTrips,

                                COALESCE(
                                    SUM(
                                        CASE
                                            WHEN i.itineraryStatus = :completedStatus
                                            THEN 1
                                            ELSE 0
                                        END
                                    ),
                                    0
                                ) AS completedTrips,

                                COALESCE(
                                    SUM(i.totalBudget),
                                    0
                                ) AS totalBudget,

                                COALESCE(
                                    SUM(i.estimatedCost),
                                    0
                                ) AS usedBudget

                            FROM Itinerary i

                            WHERE i.user.id = :userId
                        """)
        UserSummaryProjection getUserSummary(
                        @Param("userId") Long userId,
                        @Param("completedStatus") ItineraryStatus completedStatus);

        // =========================================================
        // UPCOMING TRIP
        // =========================================================

        @Query("""
                            SELECT i
                            FROM Itinerary i
                            JOIN FETCH i.location l

                            WHERE i.user.id = :userId

                            AND i.startDate >= :today

                            AND i.itineraryStatus NOT IN :excludedStatuses

                            ORDER BY i.startDate ASC
                        """)
        List<Itinerary> findUpcomingTrips(
                        @Param("userId") Long userId,
                        @Param("today") LocalDate today,
                        @Param("excludedStatuses") List<ItineraryStatus> excludedStatuses);
}