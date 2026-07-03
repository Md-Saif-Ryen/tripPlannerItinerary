package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.ItineraryRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.ItineraryResponse;
import com.example.tripItinerary.Service.ItineraryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
@Validated
public class ItineraryController {

    private final ItineraryService itineraryService;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ItineraryResponse>> generate(
            @Valid @RequestBody ItineraryRequest request) {

        ItineraryResponse response = itineraryService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Itinerary generated successfully.",
                        response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItineraryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ItineraryRequest request) {

        ItineraryResponse response = itineraryService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Itinerary updated successfully.",
                        response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItineraryResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        itineraryService.getById(id)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ItineraryResponse>>> getMyTrips() {

        return ResponseEntity.ok(
                ApiResponse.success(
                        itineraryService.getMyItineraries()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        itineraryService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Itinerary deleted successfully.",
                        null));
    }

}