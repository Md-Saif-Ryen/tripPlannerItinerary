package com.example.tripItinerary.Controller;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.tripItinerary.DTO.request.RestaurantRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.PageResponse;
import com.example.tripItinerary.DTO.response.RestaurantCsvImportResponse;
import com.example.tripItinerary.DTO.response.RestaurantResponse;

import com.example.tripItinerary.Service.RestaurantService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class RestaurantController {

        private final RestaurantService restaurantService;

        // ============================================================
        // CREATE
        // ============================================================

        @PostMapping("/create_restaurant")
        public ResponseEntity<ApiResponse<RestaurantResponse>> create(
                        @Valid @RequestBody RestaurantRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ApiResponse.success(
                                                                "Restaurant created successfully.",
                                                                restaurantService.create(request)));
        }

        // ============================================================
        // CREATE BULK RESTAURANT
        // ============================================================
        @PostMapping("/bulk_create_restaurant")
        public ResponseEntity<ApiResponse<List<RestaurantResponse>>> createBulk(
                        @Valid @RequestBody List<@Valid RestaurantRequest> requests) {

                List<RestaurantResponse> restaurants = requests.stream()
                                .map(restaurantService::create)
                                .toList();

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ApiResponse.success(
                                                                "Restaurants created successfully.",
                                                                restaurants));
        }

        // ============================================================
        // UPDATE
        // ============================================================

        @PutMapping("/updateById/{id}")
        public ResponseEntity<ApiResponse<RestaurantResponse>> update(
                        @PathVariable Long id,
                        @Valid @RequestBody RestaurantRequest request) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Restaurant updated successfully.",
                                                restaurantService.update(
                                                                id,
                                                                request)));
        }

        // ============================================================
        // GET BY ID
        // ============================================================

        @GetMapping("/getById/{id}")
        public ResponseEntity<ApiResponse<RestaurantResponse>> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Restaurant fetched successfully.",
                                                restaurantService.getById(id)));
        }

        // ============================================================
        // GET ALL - PAGINATED
        // ============================================================

        @GetMapping("/getAll")
        public ResponseEntity<ApiResponse<PageResponse<RestaurantResponse>>> getAll(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(defaultValue = "desc") String direction) {

                Pageable pageable = createPageable(
                                page,
                                size,
                                sortBy,
                                direction);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Restaurants fetched successfully.",
                                                restaurantService.getAll(pageable)));
        }

        // ============================================================
        // GET BY LOCATION - PAGINATED
        // ============================================================

        @GetMapping("/location/{locationId}")
        public ResponseEntity<ApiResponse<PageResponse<RestaurantResponse>>> getByLocation(
                        @PathVariable Long locationId,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "id") String sortBy,
                        @RequestParam(defaultValue = "desc") String direction) {

                Pageable pageable = createPageable(
                                page,
                                size,
                                sortBy,
                                direction);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Restaurants fetched successfully.",
                                                restaurantService.getByLocation(
                                                                locationId,
                                                                pageable)));
        }

        // ============================================================
        // SEARCH
        // ============================================================

        @GetMapping("/search/{locationId}")
        public ResponseEntity<ApiResponse<PageResponse<RestaurantResponse>>> search(
                        @PathVariable Long locationId,
                        @RequestParam String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "averageRating") String sortBy,
                        @RequestParam(defaultValue = "desc") String direction) {

                Pageable pageable = createPageable(
                                page,
                                size,
                                sortBy,
                                direction);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Restaurants fetched successfully.",
                                                restaurantService.search(
                                                                locationId,
                                                                keyword,
                                                                pageable)));
        }

        // ============================================================
        // CSV IMPORT
        // ============================================================

        @PostMapping(value = "/import_csv", consumes = "multipart/form-data")
        public ResponseEntity<ApiResponse<RestaurantCsvImportResponse>> importCsv(
                        @RequestParam("file") MultipartFile file) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Restaurant CSV imported successfully.",
                                                restaurantService.importCsv(file)));
        }

        // ============================================================
        // COUNT - ALL RESTAURANTS
        // ============================================================

        @GetMapping("/count")
        public ResponseEntity<ApiResponse<Long>> countAll() {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Restaurants count fetched successfully.",
                                                restaurantService.countAll()));
        }

        // ============================================================
        // COUNT - BY LOCATION
        // ============================================================

        @GetMapping("/count/location/{locationId}")
        public ResponseEntity<ApiResponse<Long>> countByLocation(
                        @PathVariable Long locationId) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Restaurants count fetched successfully.",
                                                restaurantService.countByLocation(
                                                                locationId)));
        }

        // ============================================================
        // COUNT - ACTIVE
        // ============================================================

        @GetMapping("/count/active")
        public ResponseEntity<ApiResponse<Long>> countActive() {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Active restaurants count fetched successfully.",
                                                restaurantService.countActive()));
        }

        // ============================================================
        // COUNT - ACTIVE BY LOCATION
        // ============================================================

        @GetMapping("/count/active/location/{locationId}")
        public ResponseEntity<ApiResponse<Long>> countActiveByLocation(
                        @PathVariable Long locationId) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Active restaurants count fetched successfully.",
                                                restaurantService.countActiveByLocation(
                                                                locationId)));
        }

        // ============================================================
        // DELETE
        // ============================================================

        @DeleteMapping("/deleteById/{id}")
        public ResponseEntity<ApiResponse<Void>> delete(
                        @PathVariable Long id) {

                restaurantService.delete(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Restaurant deleted successfully.",
                                                null));
        }

        // ============================================================
        // PAGEABLE
        // ============================================================

        private Pageable createPageable(
                        int page,
                        int size,
                        String sortBy,
                        String direction) {

                /*
                 * Page cannot be negative.
                 */
                page = Math.max(page, 0);

                /*
                 * Keep page size between 1 and 100.
                 */
                size = Math.min(
                                Math.max(size, 1),
                                100);

                /*
                 * Only allow known database fields.
                 */
                Set<String> allowedSortFields = Set.of(
                                "id",
                                "restaurantName",
                                "averageRating",
                                "averageCostPerPerson",
                                "restaurantWeight",
                                "createdAt");

                if (!allowedSortFields.contains(sortBy)) {
                        sortBy = "id";
                }

                Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                return PageRequest.of(
                                page,
                                size,
                                Sort.by(
                                                sortDirection,
                                                sortBy));
        }
}