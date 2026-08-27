package com.example.tripItinerary.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.tripItinerary.DTO.GoogleUserInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Service
public class GoogleAuthService {

    private final String googleClientId;

    public GoogleAuthService(
            @Value("${google.client-id}") String googleClientId) {
        this.googleClientId = googleClientId;
    }

    public GoogleUserInfo verifyToken(
            String idTokenString) {

        if (idTokenString == null ||
                idTokenString.isBlank()) {

            throw new RuntimeException(
                    "Google ID token is required.");
        }

        try {

            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport
                            .newTrustedTransport(),

                    GsonFactory
                            .getDefaultInstance())
                    .setAudience(
                            Collections.singletonList(
                                    googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(
                    idTokenString);

            if (idToken == null) {

                throw new RuntimeException(
                        "Invalid Google ID token.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String googleId = payload.getSubject();

            String email = payload.getEmail();

            Boolean emailVerified = payload.getEmailVerified();

            String name = (String) payload.get("name");

            String picture = (String) payload.get("picture");

            if (email == null ||
                    email.isBlank()) {

                throw new RuntimeException(
                        "Google account email is missing.");
            }

            if (!Boolean.TRUE.equals(
                    emailVerified)) {

                throw new RuntimeException(
                        "Google email is not verified.");
            }

            return new GoogleUserInfo(
                    googleId,
                    email,
                    name,
                    picture);

        } catch (
                GeneralSecurityException | IOException e) {

            throw new RuntimeException(
                    "Unable to verify Google ID token.",
                    e);
        }
    }
}