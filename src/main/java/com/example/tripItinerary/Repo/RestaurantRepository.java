// package com.example.tripItinerary.Repo;

// import java.util.List;

// import org.springframework.data.jpa.repository.JpaRepository;

// import com.example.tripItinerary.Entity.Restaurant;

// public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

//     List<Restaurant> findByLocationId(Long locationId);

//     List<Restaurant> findByLocationIdAndActiveTrue(Long locationId);

//     List<Restaurant> findByVegTrue();
    
//     List<Restaurant> findByLocationIdAndActiveTrueOrderByAverageRatingDesc(
//             Long locationId);


// }


package com.example.tripItinerary.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tripItinerary.Entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    // ============================================================
    // PAGINATED
    // ============================================================

    Page<Restaurant> findAllByOrderByIdDesc(
            Pageable pageable);

    Page<Restaurant> findByLocationId(
            Long locationId,
            Pageable pageable);

    Page<Restaurant> findByLocationIdAndActiveTrue(
            Long locationId,
            Pageable pageable);

    Page<Restaurant> findByLocationIdAndVegTrueAndActiveTrue(
            Long locationId,
            Pageable pageable);

    // ============================================================
    // ACTIVE RESTAURANTS
    // Used by itinerary generation
    // ============================================================

    List<Restaurant> findByLocationIdAndActiveTrue(
            Long locationId);

    List<Restaurant> findByLocationIdAndActiveTrueOrderByAverageRatingDesc(
            Long locationId);

    // ============================================================
    // VEG
    // ============================================================

    List<Restaurant> findByVegTrue();

    // ============================================================
    // SINGLE RECORD
    // ============================================================

    Optional<Restaurant> findByLocationIdAndRestaurantNameIgnoreCase(
            Long locationId,
            String restaurantName);

    boolean existsByLocationIdAndRestaurantNameIgnoreCase(
            Long locationId,
            String restaurantName);

    // ============================================================
    // SEARCH
    // ============================================================

    @Query("""
            SELECT r
            FROM Restaurant r
            WHERE r.location.id = :locationId
            AND (
                LOWER(r.restaurantName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(r.cuisineType, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(r.address, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    Page<Restaurant> searchByLocation(
            @Param("locationId") Long locationId,
            @Param("keyword") String keyword,
            Pageable pageable);

    // ============================================================
    // CSV DUPLICATE CHECK
    // ============================================================

    @Query("""
            SELECT r.restaurantName
            FROM Restaurant r
            WHERE r.location.id = :locationId
            """)
    List<String> findRestaurantNamesByLocationId(
            @Param("locationId") Long locationId);



            @Query("""
            SELECT r
            FROM Restaurant r
            WHERE r.location.id = :locationId
            """)
    List<Restaurant> findAllForImport(
            @Param("locationId") Long locationId);
}