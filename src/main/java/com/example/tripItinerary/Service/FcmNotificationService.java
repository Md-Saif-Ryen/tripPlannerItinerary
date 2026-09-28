package com.example.tripItinerary.Service;

public interface FcmNotificationService {

    void sendLocationAddedNotification(
            String fcmToken,
            String locationName);
}