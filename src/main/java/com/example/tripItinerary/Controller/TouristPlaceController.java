package com.example.tripItinerary.Controller;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.TouristPlaceRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.TouristPlaceResponse;
import com.example.tripItinerary.Service.TouristPlaceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tourist-places")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class TouristPlaceController {

        private final TouristPlaceService touristPlaceService;

        /*
         * ============================================================
         * CREATE
         * ============================================================
         */

        @PostMapping("/create_tourist_place")
        public ResponseEntity<ApiResponse<TouristPlaceResponse>> createTouristPlace(
                        @RequestBody TouristPlaceRequest request) {

                TouristPlaceResponse response = touristPlaceService.create(request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tourist place created successfully.",
                                                response));
        }

        /*
         * ============================================================
         * UPDATE
         * ============================================================
         */

        @PutMapping("/updateById/{id}")
        public ResponseEntity<ApiResponse<TouristPlaceResponse>> updateTouristPlace(
                        @PathVariable Long id,
                        @RequestBody TouristPlaceRequest request) {

                TouristPlaceResponse response = touristPlaceService.update(
                                id,
                                request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tourist place updated successfully.",
                                                response));
        }

        /*
         * ============================================================
         * GET BY ID
         * ============================================================
         */

        @GetMapping("/getById/{id}")
        public ResponseEntity<ApiResponse<TouristPlaceResponse>> getById(
                        @PathVariable Long id) {

                TouristPlaceResponse response = touristPlaceService.getById(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tourist place fetched successfully.",
                                                response));
        }

        /*
         * ============================================================
         * GET ALL - PAGEABLE
         * ============================================================
         *
         * Default:
         *
         * page = 0
         * size = 20
         * sortBy = placeWeight
         * direction = desc
         *
         * Example:
         *
         * /getAll
         *
         * /getAll?page=0&size=20
         *
         * /getAll?page=0&size=20&sortBy=popularityScore&direction=desc
         */

        @GetMapping("/getAll")
        public ResponseEntity<ApiResponse<Page<TouristPlaceResponse>>> getAll(
                        @RequestParam(defaultValue = "0") int page,

                        @RequestParam(defaultValue = "20") int size,

                        @RequestParam(defaultValue = "placeWeight") String sortBy,

                        @RequestParam(defaultValue = "desc") String direction) {

                Page<TouristPlaceResponse> response = touristPlaceService.getAll(
                                page,
                                size,
                                sortBy,
                                direction);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tourist places fetched successfully.",
                                                response));
        }

        /*
         * ============================================================
         * GET BY LOCATION - PAGEABLE
         * ============================================================
         *
         * Example:
         *
         * /location/33
         *
         * /location/33?page=0&size=20
         *
         * /location/33?page=0&size=20
         * &sortBy=popularityScore
         * &direction=desc
         */

        @GetMapping("/location/{locationId}")
        public ResponseEntity<ApiResponse<Page<TouristPlaceResponse>>> getByLocation(
                        @PathVariable Long locationId,

                        @RequestParam(defaultValue = "0") int page,

                        @RequestParam(defaultValue = "20") int size,

                        @RequestParam(defaultValue = "placeWeight") String sortBy,

                        @RequestParam(defaultValue = "desc") String direction) {

                Page<TouristPlaceResponse> response = touristPlaceService.getByLocation(
                                locationId,
                                page,
                                size,
                                sortBy,
                                direction);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tourist places fetched successfully.",
                                                response));
        }

        /*
         * ============================================================
         * DELETE
         * ============================================================
         */

        @DeleteMapping("/deleteById/{id}")
        public ResponseEntity<ApiResponse<Void>> deleteTouristPlace(
                        @PathVariable Long id) {

                touristPlaceService.delete(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tourist place deleted successfully.",
                                                null));
        }

        // ============================================================
        // COUNT - ALL TOURIST PLACES
        // ============================================================

        @GetMapping("/count")
        public ResponseEntity<Map<String, Object>> countTouristPlaces() {

                long count = touristPlaceService.countAll();

                return ResponseEntity.ok(
                                Map.of(
                                                "success", true,
                                                "message", "Tourist places count fetched successfully.",
                                                "data", count));
        }

        // ============================================================
        // COUNT - BY LOCATION
        // ============================================================

        @GetMapping("/count/location/{locationId}")
        public ResponseEntity<Map<String, Object>> countByLocation(
                        @org.springframework.web.bind.annotation.PathVariable Long locationId) {

                long count = touristPlaceService.countByLocation(locationId);

                return ResponseEntity.ok(
                                Map.of(
                                                "success", true,
                                                "message", "Tourist places count fetched successfully.",
                                                "data", count));
        }

        // ============================================================
        // COUNT - ACTIVE
        // ============================================================

        @GetMapping("/count/active")
        public ResponseEntity<Map<String, Object>> countActive() {

                long count = touristPlaceService.countActive();

                return ResponseEntity.ok(
                                Map.of(
                                                "success", true,
                                                "message", "Active tourist places count fetched successfully.",
                                                "data", count));
        }

        // ============================================================
        // COUNT - ACTIVE BY LOCATION
        // ============================================================

        @GetMapping("/count/active/location/{locationId}")
        public ResponseEntity<Map<String, Object>> countActiveByLocation(
                        @org.springframework.web.bind.annotation.PathVariable Long locationId) {

                long count = touristPlaceService.countActiveByLocation(
                                locationId);

                return ResponseEntity.ok(
                                Map.of(
                                                "success", true,
                                                "message", "Active tourist places count fetched successfully.",
                                                "data", count));
        }
}