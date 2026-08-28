package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    // ============================================================
    // SEND TO SINGLE FCM TOKEN
    // ============================================================

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendNotification(
            @RequestParam String fcmToken,
            @RequestParam String title,
            @RequestParam String body) {

        /*
         * Firebase FCM sending logic will be added here.
         *
         * Example:
         *
         * notificationService.sendToToken(
         * fcmToken,
         * title,
         * body
         * );
         */

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notification request accepted successfully.",
                        null));
    }

    // ============================================================
    // SEND TO MULTIPLE FCM TOKENS
    // ============================================================

    @PostMapping("/sendBulk")
    public ResponseEntity<ApiResponse<Void>> sendBulkNotification(
            @RequestBody List<String> fcmTokens,
            @RequestParam String title,
            @RequestParam String body) {

        /*
         * Firebase bulk notification logic
         * will be added later.
         */

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Bulk notification request accepted successfully.",
                        null));
    }
}