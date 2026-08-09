package com.example.tripItinerary.Controller;

import java.util.List;

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

    private final HotelService hotelService;

    @PostMapping("/create_hotel")
    public ResponseEntity<ApiResponse<HotelResponse>> create(
            @Valid @RequestBody HotelRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Hotel created successfully.",
                        hotelService.create(request)));
    }

    @PutMapping("/updateById/{id}")
    public ResponseEntity<ApiResponse<HotelResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody HotelRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hotel updated successfully.",
                        hotelService.update(id, request)));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<HotelResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hotel fetched successfully.",
                        hotelService.getById(id)));
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hotels fetched successfully.",
                        hotelService.getAll()));
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<ApiResponse<List<HotelResponse>>> getByLocation(
            @PathVariable Long locationId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hotels fetched successfully.",
                        hotelService.getByLocation(locationId)));
    }

    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        hotelService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hotel deleted successfully.",
                        null));
    }

}