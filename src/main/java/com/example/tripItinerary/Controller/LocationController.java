package com.example.tripItinerary.Controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.LocationRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.LocationNameResponse;
import com.example.tripItinerary.DTO.response.LocationResponse;
import com.example.tripItinerary.DTO.response.MostSearchedLocationResponse;
import com.example.tripItinerary.Entity.MissingLocation;
import com.example.tripItinerary.Service.LocationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class LocationController {

        private final LocationService locationService;
        private final JdbcTemplate jdbcTemplate;

        // ============================================================
        // CREATE LOCATION
        // ============================================================

        @PostMapping("/create_location")
        public ResponseEntity<ApiResponse<LocationResponse>> create(
                        @Valid @RequestBody LocationRequest request) {

                LocationResponse response = locationService.create(request);

                System.out.println(
                                "Creating location with state: "
                                                + request.getStateName()
                                                + ", city: "
                                                + request.getCityName()
                                                + ", address: "
                                                + request.getAddress());

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ApiResponse.success(
                                                                "Location created successfully.",
                                                                response));
        }

        // ============================================================
        // UPDATE LOCATION
        // ============================================================

        @PutMapping("/updateById/{id}")
        public ResponseEntity<ApiResponse<LocationResponse>> update(
                        @PathVariable Long id,
                        @Valid @RequestBody LocationRequest request) {

                LocationResponse response = locationService.update(id, request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Location updated successfully.",
                                                response));
        }

        // ============================================================
        // GET LOCATION BY ID
        // ============================================================

        @GetMapping("/getById/{id}")
        public ResponseEntity<ApiResponse<LocationResponse>> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                locationService.getById(id)));
        }

        // ============================================================
        // GET ALL LOCATIONS
        // ============================================================

        @GetMapping("/getAll")
        public ResponseEntity<ApiResponse<List<LocationResponse>>> getAll() {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                locationService.getAll()));
        }

        // ============================================================
        // DELETE LOCATION
        // ============================================================

        @DeleteMapping("/deleteById/{id}")
        public ResponseEntity<ApiResponse<Void>> delete(
                        @PathVariable Long id) {

                locationService.delete(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Location deleted successfully.",
                                                null));
        }

        // ============================================================
        // FETCH LOCATION NAMES
        // ============================================================

        @GetMapping("/fetchByLocationName")
        public ResponseEntity<ApiResponse<List<LocationNameResponse>>> getByLocationName() {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                locationService.getByLocationName()));
        }

        // ============================================================
        // SEARCH LOCATION
        // Existing API - keep unchanged
        // ============================================================

        @GetMapping("/search")
        public ResponseEntity<ApiResponse<List<LocationNameResponse>>> searchLocations(
                        @RequestParam String query) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                locationService.searchLocations(query)));
        }

        // ============================================================
        // TOP 5 MOST SEARCHED LOCATIONS
        // ============================================================

        @GetMapping("/search/top")
        public ResponseEntity<ApiResponse<List<MostSearchedLocationResponse>>> getTopSearchedLocations() {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Top searched locations fetched successfully.",
                                                locationService
                                                                .getTopSearchedLocations()));
        }

        // ============================================================
        // GET MISSING LOCATIONS FOR ADMIN
        // ============================================================

        @GetMapping("/missingLocationa")
        public ResponseEntity<ApiResponse<List<MissingLocation>>> getMissingLocations() {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Missing locations fetched successfully.",
                                                locationService.getPendingLocations()));
        }

        @GetMapping("/cronjob")
        public String systemAwake() {
                return "System is awake";
        }

        @GetMapping("/debug/db")
        public Map<String, Object> debugDatabase() {

                Map<String, Object> result = new LinkedHashMap<>();

                result.put("database",
                                jdbcTemplate.queryForObject("SELECT DATABASE()", String.class));

                result.put("locationCount",
                                jdbcTemplate.queryForObject(
                                                "SELECT COUNT(*) FROM locations",
                                                Long.class));

                return result;
        }
}