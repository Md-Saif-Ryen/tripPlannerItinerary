package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.HotelImageRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.HotelImageResponse;
import com.example.tripItinerary.Service.hotelImageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/hotel-images")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class HotelImageController {

    private final hotelImageService imageService;

    // ============================================================
    // CREATE
    // ============================================================

    @PostMapping("/addHotelImages")
    public ResponseEntity<ApiResponse<HotelImageResponse>> create(
            @Valid @RequestBody HotelImageRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Hotel image added successfully.",
                                imageService.create(request)));
    }

    @PostMapping("/addBulkHotelImages")
    public ResponseEntity<ApiResponse<List<HotelImageResponse>>> createBulk(
            @Valid @RequestBody List<@Valid HotelImageRequest> requests) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Hotel images added successfully.",
                                imageService.createBulk(requests)));
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @GetMapping("/getHotelImagesById/{id}")
    public ResponseEntity<ApiResponse<HotelImageResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hotel image fetched successfully.",
                        imageService.getById(id)));
    }

    // ============================================================
    // GET HOTEL IMAGES
    // ============================================================

    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<ApiResponse<List<HotelImageResponse>>> getByHotel(
            @PathVariable Long hotelId) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hotel images fetched successfully.",
                        imageService.getByHotelId(hotelId)));
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @PutMapping("/updateHotelImagesById/{id}")
    public ResponseEntity<ApiResponse<HotelImageResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody HotelImageRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hotel image updated successfully.",
                        imageService.update(id, request)));
    }

    // ============================================================
    // SET PRIMARY
    // ============================================================

    @PutMapping("/{id}/primary")
    public ResponseEntity<ApiResponse<HotelImageResponse>> setPrimary(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Primary image updated successfully.",
                        imageService.setPrimary(id)));
    }

    // ============================================================
    // DELETE
    // ============================================================

    @DeleteMapping("/deleteHotelImagesById/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        imageService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Hotel image deleted successfully.",
                        null));
    }
}