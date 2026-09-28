// package com.example.tripItinerary.Service.impl;

// import org.springframework.stereotype.Service;

// import com.example.tripItinerary.Service.FcmNotificationService;
// import com.google.firebase.messaging.FirebaseMessaging;
// import com.google.firebase.messaging.Message;
// import com.google.firebase.messaging.Notification;

// import lombok.extern.slf4j.Slf4j;

// @Service
// @Slf4j
// public class FcmNotificationServiceImpl
//         implements FcmNotificationService {

//     @Override
//     public void sendLocationAddedNotification(
//             String fcmToken,
//             String locationName) {

//         if (fcmToken == null
//                 || fcmToken.isBlank()) {
//             return;
//         }

//         try {

//             Message message = Message.builder()
//                     .setToken(fcmToken)
//                     .setNotification(
//                             Notification.builder()
//                                     .setTitle(
//                                             "Location Added")
//                                     .setBody(
//                                             locationName
//                                                     + " has been added to Travilo.")
//                                     .build())
//                     .putData(
//                             "type",
//                             "LOCATION_ADDED")
//                     .putData(
//                             "locationName",
//                             locationName)
//                     .build();

//             FirebaseMessaging
//                     .getInstance()
//                     .send(message);

//         } catch (Exception e) {

//             log.error(
//                     "Failed to send FCM notification",
//                     e);
//         }
//     }
// }