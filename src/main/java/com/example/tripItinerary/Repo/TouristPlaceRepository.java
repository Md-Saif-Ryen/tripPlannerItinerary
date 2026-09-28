// package com.example.tripItinerary.Repo;

// import java.util.Collection;
// import java.util.List;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import com.example.tripItinerary.Entity.TouristPlace;

// public interface TouristPlaceRepository
//         extends JpaRepository<TouristPlace, Long> {

//     // ============================================================
//     // PAGINATED - ALL TOURIST PLACES
//     // ============================================================

//     @Query(
//         value = """
//             SELECT tp
//             FROM TouristPlace tp
//             LEFT JOIN FETCH tp.location
//             """,
//         countQuery = """
//             SELECT COUNT(tp.id)
//             FROM TouristPlace tp
//             """
//     )
//     Page<TouristPlace> findAllOptimized(
//             Pageable pageable);

//     // ============================================================
//     // PAGINATED - BY LOCATION
//     // ============================================================

//     @Query(
//         value = """
//             SELECT tp
//             FROM TouristPlace tp
//             LEFT JOIN FETCH tp.location
//             WHERE tp.location.id = :locationId
//             """,
//         countQuery = """
//             SELECT COUNT(tp.id)
//             FROM TouristPlace tp
//             WHERE tp.location.id = :locationId
//             """
//     )
//     Page<TouristPlace> findByLocationOptimized(
//             @Param("locationId") Long locationId,
//             Pageable pageable);

//     // ============================================================
//     // PAGINATED - ACTIVE
//     // ============================================================

//     @Query(
//         value = """
//             SELECT tp
//             FROM TouristPlace tp
//             LEFT JOIN FETCH tp.location
//             WHERE tp.active = true
//             """,
//         countQuery = """
//             SELECT COUNT(tp.id)
//             FROM TouristPlace tp
//             WHERE tp.active = true
//             """
//     )
//     Page<TouristPlace> findActiveOptimized(
//             Pageable pageable);

//     // ============================================================
//     // PAGINATED - ACTIVE BY LOCATION
//     // ============================================================

//     @Query(
//         value = """
//             SELECT tp
//             FROM TouristPlace tp
//             LEFT JOIN FETCH tp.location
//             WHERE tp.location.id = :locationId
//             AND tp.active = true
//             """,
//         countQuery = """
//             SELECT COUNT(tp.id)
//             FROM TouristPlace tp
//             WHERE tp.location.id = :locationId
//             AND tp.active = true
//             """
//     )
//     Page<TouristPlace> findActiveByLocationOptimized(
//             @Param("locationId") Long locationId,
//             Pageable pageable);

//     // ============================================================
//     // CSV IMPORT
//     // Fetch ONLY possible duplicate records
//     // ============================================================

//     @Query("""
//         SELECT tp
//         FROM TouristPlace tp
//         WHERE tp.location.id IN :locationIds
//         """)
//     List<TouristPlace> findForCsvImport(
//             @Param("locationIds")
//             Collection<Long> locationIds);

//     // ============================================================
//     // COUNTS
//     // ============================================================

//     @Query("""
//         SELECT COUNT(tp.id)
//         FROM TouristPlace tp
//         """)
//     long countAllTouristPlaces();

//     // ============================================================
//     // COUNT BY LOCATION
//     // ============================================================

//     @Query("""
//         SELECT COUNT(tp.id)
//         FROM TouristPlace tp
//         WHERE tp.location.id = :locationId
//         """)
//     long countByLocationId(
//             @Param("locationId")
//             Long locationId);

//     // ============================================================
//     // COUNT ACTIVE
//     // ============================================================

//     @Query("""
//         SELECT COUNT(tp.id)
//         FROM TouristPlace tp
//         WHERE tp.active = true
//         """)
//     long countActiveTouristPlaces();

//     // ============================================================
//     // COUNT ACTIVE BY LOCATION
//     // ============================================================

//     @Query("""
//         SELECT COUNT(tp.id)
//         FROM TouristPlace tp
//         WHERE tp.location.id = :locationId
//         AND tp.active = true
//         """)
//     long countActiveByLocationId(
//             @Param("locationId")
//             Long locationId);
// }

package com.example.tripItinerary.Repo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.tripItinerary.Entity.TouristPlace;

public interface TouristPlaceRepository
                extends JpaRepository<TouristPlace, Long> {

        // ============================================================
        // PAGINATED - ALL TOURIST PLACES
        // ============================================================

        @Query(value = """
                        SELECT tp
                        FROM TouristPlace tp
                        LEFT JOIN FETCH tp.location
                        """, countQuery = """
                        SELECT COUNT(tp.id)
                        FROM TouristPlace tp
                        """)
        Page<TouristPlace> findAllOptimized(
                        Pageable pageable);

        // ============================================================
        // PAGINATED - BY LOCATION
        // ============================================================

        @Query(value = """
                        SELECT tp
                        FROM TouristPlace tp
                        LEFT JOIN FETCH tp.location
                        WHERE tp.location.id = :locationId
                        """, countQuery = """
                        SELECT COUNT(tp.id)
                        FROM TouristPlace tp
                        WHERE tp.location.id = :locationId
                        """)
        Page<TouristPlace> findByLocationOptimized(
                        @Param("locationId") Long locationId,
                        Pageable pageable);

        // ============================================================
        // PAGINATED - ACTIVE
        // ============================================================

        @Query(value = """
                        SELECT tp
                        FROM TouristPlace tp
                        LEFT JOIN FETCH tp.location
                        WHERE tp.active = true
                        """, countQuery = """
                        SELECT COUNT(tp.id)
                        FROM TouristPlace tp
                        WHERE tp.active = true
                        """)
        Page<TouristPlace> findActiveOptimized(
                        Pageable pageable);

        // ============================================================
        // PAGINATED - ACTIVE BY LOCATION
        // ============================================================

        @Query(value = """
                        SELECT tp
                        FROM TouristPlace tp
                        LEFT JOIN FETCH tp.location
                        WHERE tp.location.id = :locationId
                        AND tp.active = true
                        """, countQuery = """
                        SELECT COUNT(tp.id)
                        FROM TouristPlace tp
                        WHERE tp.location.id = :locationId
                        AND tp.active = true
                        """)
        Page<TouristPlace> findActiveByLocationOptimized(
                        @Param("locationId") Long locationId,
                        Pageable pageable);

        // ============================================================
        // CSV IMPORT
        // Fetch ONLY possible duplicate records
        // ============================================================

        @Query("""
                        SELECT tp
                        FROM TouristPlace tp
                        WHERE tp.location.id IN :locationIds
                        """)
        List<TouristPlace> findForCsvImport(
                        @Param("locationIds") Collection<Long> locationIds);

        // ============================================================
        // COUNTS
        // ============================================================

        @Query("""
                        SELECT COUNT(tp.id)
                        FROM TouristPlace tp
                        """)
        long countAllTouristPlaces();

        // ============================================================
        // COUNT BY LOCATION
        // ============================================================

        @Query("""
                        SELECT COUNT(tp.id)
                        FROM TouristPlace tp
                        WHERE tp.location.id = :locationId
                        """)
        long countByLocationId(
                        @Param("locationId") Long locationId);

        // ============================================================
        // COUNT ACTIVE
        // ============================================================

        @Query("""
                        SELECT COUNT(tp.id)
                        FROM TouristPlace tp
                        WHERE tp.active = true
                        """)
        long countActiveTouristPlaces();

        // ============================================================
        // COUNT ACTIVE BY LOCATION
        // ============================================================

        @Query("""
                        SELECT COUNT(tp.id)
                        FROM TouristPlace tp
                        WHERE tp.location.id = :locationId
                        AND tp.active = true
                        """)
        long countActiveByLocationId(
                        @Param("locationId") Long locationId);

        @Query("""
                            SELECT tp
                            FROM TouristPlace tp
                            WHERE tp.location.id = :locationId
                              AND tp.active = true
                        """)
        List<TouristPlace> findActiveByLocationId(
                        @Param("locationId") Long locationId);
}