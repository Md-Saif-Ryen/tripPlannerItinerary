package com.example.tripItinerary.config;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() throws IOException {

        // Prevent duplicate FirebaseApp initialization
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (InputStream serviceAccount =
                     getClass()
                             .getClassLoader()
                             .getResourceAsStream(
                                     "firebase-service-account.json")) {

            if (serviceAccount == null) {
                throw new IllegalStateException(
                        "firebase-service-account.json not found in classpath.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

            System.out.println(
                    "Firebase Admin SDK initialized successfully.");

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to initialize Firebase Admin SDK.",
                    e);
        }
    }
}