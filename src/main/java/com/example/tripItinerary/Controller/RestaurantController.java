package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.RestaurantRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
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

    @PostMapping("/create_restaurant")
    public ResponseEntity<ApiResponse<RestaurantResponse>> create(
            @Valid @RequestBody RestaurantRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Restaurant created successfully.",
                        restaurantService.create(request)));
    }

    @PutMapping("/updateById/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant updated successfully.",
                        restaurantService.update(id, request)));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant fetched successfully.",
                        restaurantService.getById(id)));
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurants fetched successfully.",
                        restaurantService.getAll()));
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getByLocation(
            @PathVariable Long locationId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurants fetched successfully.",
                        restaurantService.getByLocation(locationId)));
    }

    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        restaurantService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant deleted successfully.",
                        null));
    }

}