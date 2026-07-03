package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.TouristPlaceRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.TouristPlaceResponse;
import com.example.tripItinerary.Service.TouristPlaceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tourist-places")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class TouristPlaceController {

        private final TouristPlaceService touristPlaceService;

        @PostMapping
        public ResponseEntity<ApiResponse<TouristPlaceResponse>> create(
                        @Valid @RequestBody TouristPlaceRequest request) {

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(
                                                "Tourist place created successfully.",
                                                touristPlaceService.create(request)));
        }

        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<TouristPlaceResponse>> update(
                        @PathVariable Long id,
                        @Valid @RequestBody TouristPlaceRequest request) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tourist place updated successfully.",
                                                touristPlaceService.update(id, request)));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<TouristPlaceResponse>> getById(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tourist place fetched successfully.",
                                                touristPlaceService.getById(id)));
        }

        @GetMapping
        public ResponseEntity<ApiResponse<List<TouristPlaceResponse>>> getAll() {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tourist places fetched successfully.",
                                                touristPlaceService.getAll()));
        }

        @GetMapping("/location/{locationId}")
        public ResponseEntity<ApiResponse<List<TouristPlaceResponse>>> getByLocation(
                        @PathVariable Long locationId) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tourist places fetched successfully.",
                                                touristPlaceService.getByLocation(locationId)));
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<Void>> delete(
                        @PathVariable Long id) {

                touristPlaceService.delete(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Tourist place deleted successfully.",
                                                null));
        }

}