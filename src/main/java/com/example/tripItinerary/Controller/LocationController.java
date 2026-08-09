package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.LocationRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.LocationResponse;
import com.example.tripItinerary.Service.LocationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class LocationController {

    private final LocationService locationService;

    @PostMapping("/create_location")
    public ResponseEntity<ApiResponse<LocationResponse>> create(
            @Valid @RequestBody LocationRequest request) {

        LocationResponse response = locationService.create(request);
        System.out.println("Creating location with state: " + request.getStateName() + ", city: " + request.getCityName() + ", address: " + request.getAddress());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Location created successfully.", response));
    }

    @PutMapping("/updateById/{id}")
    public ResponseEntity<ApiResponse<LocationResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody LocationRequest request) {

        LocationResponse response = locationService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Location updated successfully.", response));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<LocationResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(locationService.getById(id)));
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.success(locationService.getAll()));
    }

    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        locationService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success("Location deleted successfully.", null));
    }

}