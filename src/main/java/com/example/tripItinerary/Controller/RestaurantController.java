// package com.example.tripItinerary.Controller;

// import java.util.List;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.validation.annotation.Validated;
// import org.springframework.web.bind.annotation.*;

// import com.example.tripItinerary.DTO.request.RestaurantRequest;
// import com.example.tripItinerary.DTO.response.ApiResponse;
// import com.example.tripItinerary.DTO.response.RestaurantResponse;
// import com.example.tripItinerary.Service.RestaurantService;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api/v1/restaurants")
// @RequiredArgsConstructor
// @Validated
// @CrossOrigin(origins = "*")
// public class RestaurantController {

//     private final RestaurantService restaurantService;

//     @PostMapping("/create_restaurant")
//     public ResponseEntity<ApiResponse<RestaurantResponse>> create(
//             @Valid @RequestBody RestaurantRequest request) {

//         return ResponseEntity.status(HttpStatus.CREATED)
//                 .body(ApiResponse.success(
//                         "Restaurant created successfully.",
//                         restaurantService.create(request)));
//     }

//     @PutMapping("/updateById/{id}")
//     public ResponseEntity<ApiResponse<RestaurantResponse>> update(
//             @PathVariable Long id,
//             @Valid @RequestBody RestaurantRequest request) {

//         return ResponseEntity.ok(
//                 ApiResponse.success(
//                         "Restaurant updated successfully.",
//                         restaurantService.update(id, request)));
//     }

//     @GetMapping("/getById/{id}")
//     public ResponseEntity<ApiResponse<RestaurantResponse>> getById(
//             @PathVariable Long id) {

//         return ResponseEntity.ok(
//                 ApiResponse.success(
//                         "Restaurant fetched successfully.",
//                         restaurantService.getById(id)));
//     }

//     @GetMapping("/getAll")
//     public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAll() {

//         return ResponseEntity.ok(
//                 ApiResponse.success(
//                         "Restaurants fetched successfully.",
//                         restaurantService.getAll()));
//     }

//     @GetMapping("/location/{locationId}")
//     public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getByLocation(
//             @PathVariable Long locationId) {

//         return ResponseEntity.ok(
//                 ApiResponse.success(
//                         "Restaurants fetched successfully.",
//                         restaurantService.getByLocation(locationId)));
//     }

//     @DeleteMapping("/deleteById/{id}")
//     public ResponseEntity<ApiResponse<Void>> delete(
//             @PathVariable Long id) {

//         restaurantService.delete(id);

//         return ResponseEntity.ok(
//                 ApiResponse.success(
//                         "Restaurant deleted successfully.",
//                         null));
//     }

// }

package com.example.tripItinerary.Controller;

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
        // GET ALL - PAGINATION
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
                                                restaurantService.getAll(
                                                                pageable)));
        }

        // ============================================================
        // GET BY LOCATION - PAGINATION
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

        @PostMapping(value = "/import_csv/{locationId}", consumes = "multipart/form-data")
        public ResponseEntity<ApiResponse<RestaurantCsvImportResponse>> importCsv(

                        @PathVariable Long locationId,

                        @RequestParam("file") MultipartFile file) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Restaurant CSV imported successfully.",
                                                restaurantService.importCsv(
                                                                file,
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

                page = Math.max(page, 0);

                /*
                 * Prevent user from requesting:
                 *
                 * size=100000
                 *
                 * which can kill the API.
                 */

                size = Math.min(
                                Math.max(size, 1),
                                100);

                /*
                 * Allow only known sortable columns.
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