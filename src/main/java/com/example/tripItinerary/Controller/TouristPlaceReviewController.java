package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.TouristPlaceReviewRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.TouristPlaceReviewResponse;
import com.example.tripItinerary.Service.TouristPlaceReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/tourist-place-reviews")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class TouristPlaceReviewController {

    private final TouristPlaceReviewService reviewService;

    // ============================================================
    // CREATE
    // ============================================================

    @PostMapping("/addTouristPlaceReview")
    public ResponseEntity<ApiResponse<TouristPlaceReviewResponse>> create(
            @Valid @RequestBody TouristPlaceReviewRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Tourist place review added successfully.",
                                reviewService.create(request)));
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @GetMapping("/getTouristPlaceReviewById/{id}")
    public ResponseEntity<ApiResponse<TouristPlaceReviewResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tourist place review fetched successfully.",
                        reviewService.getById(id)));
    }

    // ============================================================
    // GET REVIEWS BY TOURIST PLACE
    // ============================================================

    @GetMapping("/tourist-place/{touristPlaceId}")
    public ResponseEntity<ApiResponse<List<TouristPlaceReviewResponse>>> getByTouristPlace(
            @PathVariable Long touristPlaceId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tourist place reviews fetched successfully.",
                        reviewService.getByTouristPlaceId(
                                touristPlaceId)));
    }

    // ============================================================
    // GET REVIEWS BY USER
    // ============================================================

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<TouristPlaceReviewResponse>>> getByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "User tourist place reviews fetched successfully.",
                        reviewService.getByUserId(userId)));
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @PutMapping("/updateTouristPlaceReviewById/{id}")
    public ResponseEntity<ApiResponse<TouristPlaceReviewResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TouristPlaceReviewRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tourist place review updated successfully.",
                        reviewService.update(id, request)));
    }

    // ============================================================
    // DELETE
    // ============================================================

    @DeleteMapping("/deleteTouristPlaceReviewById/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        reviewService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tourist place review deleted successfully.",
                        null));
    }
}