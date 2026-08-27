package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.HotelReviewRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.HotelReviewResponse;
import com.example.tripItinerary.Service.HotelReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/hotel-reviews")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class HotelReviewController {

    private final HotelReviewService reviewService;

    // ============================================================
    // CREATE
    // ============================================================

    @PostMapping("/addHotelReview")
    public ResponseEntity<ApiResponse<HotelReviewResponse>> create(
            @Valid @RequestBody HotelReviewRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Review added successfully.",
                                reviewService.create(request)));
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @GetMapping("getHotelReviewById/{id}")
    public ResponseEntity<ApiResponse<HotelReviewResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Review fetched successfully.",
                        reviewService.getById(id)));
    }

    // ============================================================
    // GET HOTEL REVIEWS
    // ============================================================

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<ApiResponse<List<HotelReviewResponse>>> getByHotel(
            @PathVariable Long hotelId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hotel reviews fetched successfully.",
                        reviewService.getByHotelId(hotelId)));
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @PutMapping("updateHotelReview/{id}")
    public ResponseEntity<ApiResponse<HotelReviewResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody HotelReviewRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Review updated successfully.",
                        reviewService.update(
                                id,
                                request)));
    }

    // ============================================================
    // DELETE
    // ============================================================

    @DeleteMapping("deleteHotelReview/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        reviewService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Review deleted successfully.",
                        null));
    }
}