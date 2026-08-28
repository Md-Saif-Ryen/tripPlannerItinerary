package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.RestaurantReviewRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.RestaurantReviewResponse;
import com.example.tripItinerary.Service.RestaurantReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/restaurant-reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RestaurantReviewController {

    private final RestaurantReviewService restaurantReviewService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RestaurantReviewResponse>> create(
            @Valid @RequestBody RestaurantReviewRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Restaurant review created successfully.",
                        restaurantReviewService.create(request)));
    }

    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<ApiResponse<List<RestaurantReviewResponse>>> getByRestaurant(
            @PathVariable Long restaurantId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant reviews fetched successfully.",
                        restaurantReviewService.getByRestaurant(
                                restaurantId)));
    }

    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        restaurantReviewService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant review deleted successfully.",
                        null));
    }
}