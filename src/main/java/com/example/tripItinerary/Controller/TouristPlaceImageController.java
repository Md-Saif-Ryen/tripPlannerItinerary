package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.TouristPlaceImageRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.TouristPlaceImageResponse;
import com.example.tripItinerary.Service.TouristPlaceImageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tourist-place-images")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class TouristPlaceImageController {

    private final TouristPlaceImageService imageService;

    // ============================================================
    // CREATE
    // ============================================================

    @PostMapping("/addTouristPlaceImages")
    public ResponseEntity<ApiResponse<TouristPlaceImageResponse>> create(
            @Valid @RequestBody TouristPlaceImageRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tourist place image added successfully.",
                                imageService.create(request)));
    }

    // ============================================================
    // BULK CREATE
    // ============================================================

    @PostMapping("/addBulkTouristPlaceImages")
    public ResponseEntity<ApiResponse<List<TouristPlaceImageResponse>>> createBulk(
            @Valid @RequestBody List<@Valid TouristPlaceImageRequest> requests) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tourist place images added successfully.",
                                imageService.createBulk(requests)));
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @GetMapping("/getTouristPlaceImagesById/{id}")
    public ResponseEntity<ApiResponse<TouristPlaceImageResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tourist place image fetched successfully.",
                        imageService.getById(id)));
    }

    // ============================================================
    // GET TOURIST PLACE IMAGES
    // ============================================================

    @GetMapping("/tourist-place/{touristPlaceId}")
    public ResponseEntity<ApiResponse<List<TouristPlaceImageResponse>>> getByTouristPlace(
            @PathVariable Long touristPlaceId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tourist place images fetched successfully.",
                        imageService.getByTouristPlaceId(
                                touristPlaceId)));
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @PutMapping("/updateTouristPlaceImagesById/{id}")
    public ResponseEntity<ApiResponse<TouristPlaceImageResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TouristPlaceImageRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tourist place image updated successfully.",
                        imageService.update(id, request)));
    }

    // ============================================================
    // SET PRIMARY
    // ============================================================

    @PutMapping("/{id}/primary")
    public ResponseEntity<ApiResponse<TouristPlaceImageResponse>> setPrimary(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Primary tourist place image updated successfully.",
                        imageService.setPrimary(id)));
    }

    // ============================================================
    // DELETE
    // ============================================================

    @DeleteMapping("/deleteTouristPlaceImagesById/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        imageService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tourist place image deleted successfully.",
                        null));
    }
}