package com.example.tripItinerary.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

        @Value("${firebase.service-account-base64:}")
        private String firebaseServiceAccountBase64;

        @PostConstruct
        public void initializeFirebase() throws IOException {

                if (!FirebaseApp.getApps().isEmpty()) {
                        return;
                }

                if (firebaseServiceAccountBase64 == null ||
                                firebaseServiceAccountBase64.isBlank()) {

                        throw new IllegalStateException(
                                        "FIREBASE_SERVICE_ACCOUNT_BASE64 environment variable " +
                                                        "is not configured.");
                }

                try {

                        byte[] decodedJson = Base64.getDecoder().decode(
                                        firebaseServiceAccountBase64.trim());

                        try (InputStream serviceAccount = new ByteArrayInputStream(decodedJson)) {

                                FirebaseOptions options = FirebaseOptions.builder()
                                                .setCredentials(
                                                                GoogleCredentials.fromStream(
                                                                                serviceAccount))
                                                .build();

                                FirebaseApp.initializeApp(options);

                                System.out.println(
                                                "Firebase initialized successfully.");
                        }

                } catch (IllegalArgumentException e) {

                        throw new IllegalStateException(
                                        "FIREBASE_SERVICE_ACCOUNT_BASE64 contains invalid Base64.",
                                        e);
                }
        }
}