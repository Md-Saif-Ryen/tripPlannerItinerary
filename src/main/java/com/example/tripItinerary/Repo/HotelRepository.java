// package com.example.tripItinerary.Repo;

// import java.util.List;
// import java.util.Optional;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import com.example.tripItinerary.Entity.Hotel;

// public interface HotelRepository extends JpaRepository<Hotel, Long> {

//     @Query("""
//                 SELECT DISTINCT h
//                 FROM Hotel h
//                 LEFT JOIN FETCH h.amenities
//                 LEFT JOIN FETCH h.location
//                 WHERE h.id = :id
//             """)
//     Optional<Hotel> findByIdWithDetails(
//             @Param("id") Long id);

//     @Query("""
//                 SELECT DISTINCT h
//                 FROM Hotel h
//                 LEFT JOIN FETCH h.amenities
//                 LEFT JOIN FETCH h.location
//             """)
//     List<Hotel> findAllWithDetails();

//     @Query("""
//                 SELECT DISTINCT h
//                 FROM Hotel h
//                 LEFT JOIN FETCH h.amenities
//                 LEFT JOIN FETCH h.location
//                 WHERE h.location.id = :locationId
//             """)
//     List<Hotel> findAllByLocationIdWithDetails(
//             @Param("locationId") Long locationId);

//     List<Hotel> findByLocationId(Long locationId);
// }



package com.example.tripItinerary.Repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tripItinerary.Entity.Hotel;

public interface HotelRepository
        extends JpaRepository<Hotel, Long> {

    // =========================================================
    // DETAIL
    // =========================================================

    @Query("""
            SELECT DISTINCT h
            FROM Hotel h
            LEFT JOIN FETCH h.amenities
            LEFT JOIN FETCH h.location
            WHERE h.id = :id
            """)
    Optional<Hotel> findDetailById(
            @Param("id") Long id);

    // =========================================================
    // PAGINATED ALL
    // =========================================================

    @Query(value = """
            SELECT h
            FROM Hotel h
            LEFT JOIN FETCH h.location
            WHERE h.active = true
            """, countQuery = """
            SELECT COUNT(h)
            FROM Hotel h
            WHERE h.active = true
            """)
    Page<Hotel> findAllOptimized(
            Pageable pageable);

    // =========================================================
    // PAGINATED BY LOCATION
    // =========================================================

    @Query(value = """
            SELECT h
            FROM Hotel h
            LEFT JOIN FETCH h.location
            WHERE h.location.id = :locationId
            AND h.active = true
            """, countQuery = """
            SELECT COUNT(h)
            FROM Hotel h
            WHERE h.location.id = :locationId
            AND h.active = true
            """)
    Page<Hotel> findByLocationOptimized(
            @Param("locationId") Long locationId,
            Pageable pageable);

    // =========================================================
    // ITINERARY / INTERNAL USE
    // =========================================================

    @Query("""
            SELECT h
            FROM Hotel h
            WHERE h.location.id = :locationId
            AND h.active = true
            """)
    List<Hotel> findActiveByLocationId(
            @Param("locationId") Long locationId);


            // =========================================================
    // EXISTS
    // =========================================================

    boolean existsById(Long id);

    // =========================================================
    // OPTIONAL: FAST COUNT
    // =========================================================

    @Query("""
                SELECT COUNT(h)
                FROM Hotel h
                WHERE h.location.id = :locationId
            """)
    long countByLocationId(
            @Param("locationId") Long locationId);
}