package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.RestaurantImageRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.RestaurantImageResponse;
import com.example.tripItinerary.Service.RestaurantImageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/restaurant-images")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class RestaurantImageController {

    private final RestaurantImageService imageService;

    // ============================================================
    // CREATE
    // ============================================================

    @PostMapping("/addRestaurantImages")
    public ResponseEntity<ApiResponse<RestaurantImageResponse>> create(
            @Valid @RequestBody RestaurantImageRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Restaurant image added successfully.",
                                imageService.create(request)));
    }

    // ============================================================
    // BULK CREATE
    // ============================================================

    @PostMapping("/addBulkRestaurantImages")
    public ResponseEntity<ApiResponse<List<RestaurantImageResponse>>> createBulk(
            @Valid @RequestBody List<@Valid RestaurantImageRequest> requests) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Restaurant images added successfully.",
                                imageService.createBulk(requests)));
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @GetMapping("/getRestaurantImagesById/{id}")
    public ResponseEntity<ApiResponse<RestaurantImageResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant image fetched successfully.",
                        imageService.getById(id)));
    }

    // ============================================================
    // GET RESTAURANT IMAGES
    // ============================================================

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<RestaurantImageResponse>>> getByRestaurant(
            @PathVariable Long restaurantId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant images fetched successfully.",
                        imageService.getByRestaurantId(restaurantId)));
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @PutMapping("/updateRestaurantImagesById/{id}")
    public ResponseEntity<ApiResponse<RestaurantImageResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantImageRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant image updated successfully.",
                        imageService.update(id, request)));
    }

    // ============================================================
    // SET PRIMARY
    // ============================================================

    @PutMapping("/{id}/primary")
    public ResponseEntity<ApiResponse<RestaurantImageResponse>> setPrimary(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Primary restaurant image updated successfully.",
                        imageService.setPrimary(id)));
    }

    // ============================================================
    // DELETE
    // ============================================================

    @DeleteMapping("/deleteRestaurantImagesById/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        imageService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant image deleted successfully.",
                        null));
    }
}