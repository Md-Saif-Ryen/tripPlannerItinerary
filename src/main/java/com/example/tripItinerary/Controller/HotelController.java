// package com.example.tripItinerary.Controller;

// import java.util.List;

// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.validation.annotation.Validated;
// import org.springframework.web.bind.annotation.*;

// import com.example.tripItinerary.DTO.request.HotelRequest;
// import com.example.tripItinerary.DTO.response.ApiResponse;
// import com.example.tripItinerary.DTO.response.HotelResponse;
// import com.example.tripItinerary.Service.HotelService;

// import jakarta.validation.Valid;
// import lombok.RequiredArgsConstructor;

// @RestController
// @RequestMapping("/api/v1/hotels")
// @RequiredArgsConstructor
// @Validated
// @CrossOrigin(origins = "*")
// public class HotelController {

//         private final HotelService hotelService;

//         @PostMapping("/create_hotel")
//         public ResponseEntity<ApiResponse<HotelResponse>> create(
//                         @Valid @RequestBody HotelRequest request) {

//                 return ResponseEntity.status(HttpStatus.CREATED)
//                                 .body(ApiResponse.success(
//                                                 "Hotel created successfully.",
//                                                 hotelService.create(request)));
//         }


//         @PostMapping("/bulk_create_hotel")
//         public ResponseEntity<ApiResponse<HotelResponse>> bulkCreate(
//                         @Valid @RequestBody List<HotelRequest> requests) {

//                 return ResponseEntity.status(HttpStatus.CREATED)
//                                 .body(ApiResponse.success(
//                                                 "Hotels created successfully.",
//                                                 hotelService.bulkCreate(requests)));
//         }


//         @PutMapping("/updateById/{id}")
//         public ResponseEntity<ApiResponse<HotelResponse>> update(
//                         @PathVariable Long id,
//                         @Valid @RequestBody HotelRequest request) {

//                 return ResponseEntity.ok(
//                                 ApiResponse.success(
//                                                 "Hotel updated successfully.",
//                                                 hotelService.update(id, request)));
//         }

//         @GetMapping("/getById/{id}")
//         public ResponseEntity<ApiResponse<HotelResponse>> getById(
//                         @PathVariable Long id) {

//                 return ResponseEntity.ok(
//                                 ApiResponse.success(
//                                                 "Hotel fetched successfully.",
//                                                 hotelService.getById(id)));
//         }

//         @GetMapping("/getAll")
//         public ResponseEntity<ApiResponse<List<HotelResponse>>> getAll() {

//                 return ResponseEntity.ok(
//                                 ApiResponse.success(
//                                                 "Hotels fetched successfully.",
//                                                 hotelService.getAll()));
//         }

//         @GetMapping("/location/{locationId}")
//         public ResponseEntity<ApiResponse<List<HotelResponse>>> getByLocation(
//                         @PathVariable Long locationId) {

//                 return ResponseEntity.ok(
//                                 ApiResponse.success(
//                                                 "Hotels fetched successfully.",
//                                                 hotelService.getByLocation(locationId)));
//         }

//         @DeleteMapping("/deleteById/{id}")
//         public ResponseEntity<ApiResponse<Void>> delete(
//                         @PathVariable Long id) {

//                 hotelService.delete(id);

//                 return ResponseEntity.ok(
//                                 ApiResponse.success(
//                                                 "Hotel deleted successfully.",
//                                                 null));
//         }

// }




package com.example.tripItinerary.Controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.HotelRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.HotelResponse;
import com.example.tripItinerary.Service.HotelService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/hotels")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class HotelController {

        private static final int DEFAULT_PAGE_SIZE = 20;
        private static final int MAX_PAGE_SIZE = 100;

        private final HotelService hotelService;

        // =========================================================
        // CREATE
        // =========================================================

        @PostMapping("/create_hotel")
        public ResponseEntity<ApiResponse<HotelResponse>> create(
                        @Valid @RequestBody HotelRequest request) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ApiResponse.success(
                                                                "Hotel created successfully.",
                                                                hotelService.create(request)));
        }

        // =========================================================
        // BULK CREATE
        // =========================================================

        @PostMapping("/bulk_create_hotel")
        public ResponseEntity<ApiResponse<List<HotelResponse>>> bulkCreate(
                        @Valid @RequestBody List<@Valid HotelRequest> requests) {

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(
                                                ApiResponse.success(
                                                                "Hotels created successfully.",
                                                                hotelService.bulkCreate(requests)));
        }

        // =========================================================
        // UPDATE
        // =========================================================

        @PutMapping("/updateById/{id}")
        public ResponseEntity<ApiResponse<HotelResponse>> update(
                        @PathVariable Long id,
                        @Valid @RequestBody HotelRequest request) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Hotel updated successfully.",
                                                hotelService.update(id, request)));
        }

        // =========================================================
        // GET BY ID
        // =========================================================

        @GetMapping("/getById/{id}")
        public ResponseEntity<ApiResponse<HotelResponse>> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Hotel fetched successfully.",
                                                hotelService.getById(id)));
        }

        // =========================================================
        // GET ALL - PAGEABLE
        // =========================================================

        @GetMapping("/getAll")
        public ResponseEntity<ApiResponse<Page<HotelResponse>>> getAll(

                        @RequestParam(defaultValue = "0") int page,

                        @RequestParam(defaultValue = "20") int size,

                        @RequestParam(defaultValue = "hotelWeight") String sortBy,

                        @RequestParam(defaultValue = "desc") String direction) {

                Pageable pageable = createPageable(
                                page,
                                size,
                                sortBy,
                                direction);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Hotels fetched successfully.",
                                                hotelService.getAll(pageable)));
        }

        // =========================================================
        // GET BY LOCATION - PAGEABLE
        // =========================================================

        @GetMapping("/location/{locationId}")
        public ResponseEntity<ApiResponse<Page<HotelResponse>>> getByLocation(

                        @PathVariable Long locationId,

                        @RequestParam(defaultValue = "0") int page,

                        @RequestParam(defaultValue = "20") int size,

                        @RequestParam(defaultValue = "hotelWeight") String sortBy,

                        @RequestParam(defaultValue = "desc") String direction) {

                Pageable pageable = createPageable(
                                page,
                                size,
                                sortBy,
                                direction);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Hotels fetched successfully.",
                                                hotelService.getByLocation(
                                                                locationId,
                                                                pageable)));
        }

        // =========================================================
        // DELETE
        // =========================================================

        @DeleteMapping("/deleteById/{id}")
        public ResponseEntity<ApiResponse<Void>> delete(
                        @PathVariable Long id) {

                hotelService.delete(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Hotel deleted successfully.",
                                                null));
        }

        // =========================================================
        // PAGEABLE HELPER
        // =========================================================

        private Pageable createPageable(
                        int page,
                        int size,
                        String sortBy,
                        String direction) {

                if (page < 0) {
                        page = 0;
                }

                if (size <= 0) {
                        size = DEFAULT_PAGE_SIZE;
                }

                if (size > MAX_PAGE_SIZE) {
                        size = MAX_PAGE_SIZE;
                }

                /*
                 * Prevent arbitrary DB column injection through sortBy.
                 */

                Set<String> allowedSortFields = Set.of(
                                "id",
                                "hotelName",
                                "pricePerNight",
                                "averageRating",
                                "hotelWeight",
                                "starRating",
                                "totalRooms",
                                "createdAt",
                                "updatedAt");

                if (!allowedSortFields.contains(sortBy)) {
                        sortBy = "hotelWeight";
                }

                Sort.Direction sortDirection = "asc".equalsIgnoreCase(direction)
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                return PageRequest.of(
                                page,
                                size,
                                Sort.by(sortDirection, sortBy));
        }



        // ============================================================
        // COUNT - ALL TOURIST PLACES
        // ============================================================

        @GetMapping("/count")
        public ResponseEntity<Map<String, Object>> countTouristPlaces() {

                long count = hotelService.countAll();

                return ResponseEntity.ok(
                                Map.of(
                                                "success", true,
                                                "message", "Hotels count fetched successfully.",
                                                "data", count));
        }

        // ============================================================
        // COUNT - BY LOCATION
        // ============================================================

        @GetMapping("/count/location/{locationId}")
        public ResponseEntity<Map<String, Object>> countByLocation(
                        @org.springframework.web.bind.annotation.PathVariable Long locationId) {

                long count = hotelService.countByLocation(locationId);

                return ResponseEntity.ok(
                                Map.of(
                                                "success", true,
                                                "message", "hotels count fetched successfully.",
                                                "data", count));
        }
}


