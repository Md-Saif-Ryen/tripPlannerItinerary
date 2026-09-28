package com.example.tripItinerary.Controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.tripItinerary.DTO.request.LocationRequest;
import com.example.tripItinerary.DTO.request.MissingLocationRequest;
import com.example.tripItinerary.DTO.request.ResolveMissingLocationRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.LocationNameResponse;
import com.example.tripItinerary.DTO.response.LocationResponse;
import com.example.tripItinerary.DTO.response.MissingDataResponse;
import com.example.tripItinerary.DTO.response.MostSearchedLocationResponse;
import com.example.tripItinerary.DTO.response.OlaAutocompleteResponse;
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
        // CREATE
        // ============================================================

        @PostMapping("/create_location")
        public ResponseEntity<ApiResponse<LocationResponse>> create(
                        @Valid @RequestBody LocationRequest request) {

                LocationResponse response = locationService.create(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ApiResponse.success(
                                                                "Location created successfully.",
                                                                response));
        }

        // ============================================================
        // UPDATE
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
        // GET BY ID
        // ============================================================

        @GetMapping("/getById/{id}")
        public ResponseEntity<ApiResponse<LocationResponse>> getById(
                        @PathVariable Long id) {

                LocationResponse response = locationService.getById(id);

                return ResponseEntity.ok(
                                ApiResponse.success(response));
        }

        // ============================================================
        // GET ALL
        // ============================================================

        @GetMapping("/getAll")
        public ResponseEntity<ApiResponse<List<LocationResponse>>> getAll() {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                locationService.getAll()));
        }

        // ============================================================
        // GET CHILDREN
        // ============================================================

        @GetMapping("/{id}/children")
        public ResponseEntity<ApiResponse<List<LocationResponse>>> getChildren(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                locationService.getChildren(id)));
        }

        // ============================================================
        // GET ALL DESCENDANTS
        // ============================================================

        @GetMapping("/{id}/descendants")
        public ResponseEntity<ApiResponse<List<LocationResponse>>> getDescendants(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                locationService.getDescendants(id)));
        }

        // ============================================================
        // DELETE
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
        // LOCATION NAMES
        // ============================================================

        @GetMapping("/fetchByLocationName")
        public ResponseEntity<ApiResponse<List<LocationNameResponse>>> getByLocationName() {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                locationService.getByLocationName()));
        }

        // ============================================================
        // SEARCH
        // ============================================================

        @GetMapping("/search")
        public ResponseEntity<ApiResponse<List<LocationNameResponse>>> searchLocations(
                        @RequestParam String query) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                locationService.searchLocations(query)));
        }

        // ============================================================
        // TOP SEARCHED
        // ============================================================

        @GetMapping("/search/top")
        public ResponseEntity<ApiResponse<List<MostSearchedLocationResponse>>> getTopSearchedLocations() {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Top searched locations fetched successfully.",
                                                locationService.getTopSearchedLocations()));
        }

        // ============================================================
        // MISSING LOCATIONS
        // ============================================================

        @GetMapping("/missingLocations")
        public ResponseEntity<ApiResponse<List<MissingLocation>>> getMissingLocations() {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Missing locations fetched successfully.",
                                                locationService.getPendingLocations()));
        }

        // ============================================================
        // USER REQUESTS MISSING LOCATION
        // ============================================================

        @PostMapping("/missingLocations/request")
        public ResponseEntity<ApiResponse<MissingLocation>> requestMissingLocation(
                        @Valid @RequestBody MissingLocationRequest request) {

                MissingLocation response = locationService.requestMissingLocation(request);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ApiResponse.success(
                                                                "Location request submitted successfully.",
                                                                response));
        }

        // ============================================================
        // OLA AUTOCOMPLETE
        // ============================================================

        @GetMapping("/missingLocations/{id}/autocomplete")
        public ResponseEntity<ApiResponse<OlaAutocompleteResponse>> getMissingLocationAutocomplete(
                        @PathVariable Long id) {

                OlaAutocompleteResponse response = locationService
                                .getMissingLocationAutocomplete(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Location suggestions fetched successfully.",
                                                response));
        }

        // ============================================================
        // RESOLVE MISSING LOCATION
        // ============================================================

        @PostMapping("/missingLocations/{id}/resolve")
        public ResponseEntity<ApiResponse<MissingLocation>> resolveMissingLocation(
                        @PathVariable Long id,
                        @Valid @RequestBody ResolveMissingLocationRequest request) {

                MissingLocation response = locationService.resolveMissingLocation(
                                id,
                                request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Location resolved and added successfully.",
                                                response));
        }

        // ============================================================
        // MISSING DATA
        // ============================================================

        @GetMapping("/missing-data")
        public ResponseEntity<MissingDataResponse> getLocationsMissingData(
                        @PageableDefault(size = 20) Pageable pageable) {

                return ResponseEntity.ok(
                                locationService
                                                .getLocationsMissingData(pageable));
        }

        // ============================================================
        // CRONJOB / HEALTH CHECK
        // ============================================================

        @GetMapping("/cronjob")
        public ResponseEntity<String> systemAwake() {

                return ResponseEntity.ok(
                                "System is awake");
        }

        // ============================================================
        // DEBUG DATABASE
        // ============================================================

        @GetMapping("/debug/db")
        public ResponseEntity<Map<String, Object>> debugDatabase() {

                Map<String, Object> result = new LinkedHashMap<>();

                result.put(
                                "database",
                                jdbcTemplate.queryForObject(
                                                "SELECT DATABASE()",
                                                String.class));

                result.put(
                                "locationCount",
                                jdbcTemplate.queryForObject(
                                                "SELECT COUNT(*) FROM locations",
                                                Long.class));

                return ResponseEntity.ok(result);
        }
}
