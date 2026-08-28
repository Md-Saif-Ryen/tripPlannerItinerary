package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.ItineraryRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.ItineraryResponse;
import com.example.tripItinerary.DTO.response.UserSummaryResponse;
import com.example.tripItinerary.Service.ItineraryService;
// import com.example.tripItinerary.security.util.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/itineraries")
@RequiredArgsConstructor
@Validated
public class ItineraryController {

        private final ItineraryService itineraryService;
        // private final SecurityUtils securityUtils;

        @PostMapping("/generate_iterary")
        public ResponseEntity<ApiResponse<List<ItineraryResponse>>> generate(
                        @Valid @RequestBody ItineraryRequest request) {

                List<ItineraryResponse> response = itineraryService.create(request);

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(
                                                "Itinerary generated successfully.",
                                                response));
        }

        @GetMapping("/getItineraryByUserId/{id}")
        public ResponseEntity<ApiResponse<List<ItineraryResponse>>> getItineraryByUserId(
                        @PathVariable Long id) {

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                itineraryService.getItineraryByUserId(id)));
        }

        @GetMapping("/summary")
        public ResponseEntity<ApiResponse<UserSummaryResponse>> getUserSummary(Long id) {

                try {

                        // Long userId = securityUtils.getCurrentUserId();

                        UserSummaryResponse summary = itineraryService.getUserSummaryByUserId(id);

                        return ResponseEntity.ok(
                                        ApiResponse.<UserSummaryResponse>builder()
                                                        .success(true)
                                                        .message(
                                                                        "User profile summary fetched successfully")
                                                        .data(summary)
                                                        .build());

                } catch (Exception e) {

                        return ResponseEntity
                                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(
                                                        ApiResponse.<UserSummaryResponse>builder()
                                                                        .success(false)
                                                                        .message(
                                                                                        "Failed to fetch user profile summary")
                                                                        .data(null)
                                                                        .build());
                }
        }

        @PutMapping("/updateById/{id}")
        public ResponseEntity<ApiResponse<ItineraryResponse>> update(
                        @PathVariable Long id,
                        @Valid @RequestBody ItineraryRequest request) {

                ItineraryResponse response = itineraryService.update(id, request);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Itinerary updated successfully.",
                                                response));
        }

        @GetMapping("/getById/{id}")
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

        @PostMapping("/select/{selectionId}")
        public ItineraryResponse select(
                        @PathVariable String selectionId,
                        @RequestParam Long userId) {

                return itineraryService.selectGeneratedItinerary(
                                userId,
                                selectionId);
        }

        @PutMapping("/places/{itineraryPlaceId}/complete")
        public ItineraryResponse completePlace(
                        @PathVariable Long itineraryPlaceId) {
                return itineraryService.markPlaceCompleted(
                                itineraryPlaceId);
        }

        @DeleteMapping("/deleteById/{id}")
        public ResponseEntity<ApiResponse<Void>> delete(
                        @PathVariable Long id) {

                itineraryService.delete(id);

                return ResponseEntity.ok(
                                ApiResponse.success(
                                                "Itinerary deleted successfully.",
                                                null));
        }

       

}