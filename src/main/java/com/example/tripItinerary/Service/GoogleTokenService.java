package com.example.tripItinerary.Service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GoogleTokenService {

        private final String googleClientId;

        public GoogleTokenService(
                        @Value("${google.client-id}") String googleClientId) {
                this.googleClientId = googleClientId;
        }

        public GoogleUserInfo verify(String idTokenString) {

                try {

                        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                                        GoogleNetHttpTransport.newTrustedTransport(),
                                        GsonFactory.getDefaultInstance())
                                        .setAudience(
                                                        Collections.singletonList(
                                                                        googleClientId))
                                        .build();

                        GoogleIdToken idToken = verifier.verify(idTokenString);

                        if (idToken == null) {
                                throw new IllegalArgumentException(
                                                "Invalid Google ID token.");
                        }

                        Payload payload = idToken.getPayload();

                        String googleId = payload.getSubject();
                        String email = payload.getEmail();

                        Boolean emailVerified = payload.getEmailVerified();

                        String name = (String) payload.get("name");

                        String picture = (String) payload.get("picture");

                        if (googleId == null ||
                                        email == null ||
                                        !Boolean.TRUE.equals(emailVerified)) {

                                throw new IllegalArgumentException(
                                                "Google account verification failed.");
                        }

                        return new GoogleUserInfo(
                                        googleId,
                                        email,
                                        name,
                                        picture);

                } catch (Exception e) {

                        log.error(
                                        "Google token verification failed",
                                        e);

                        throw new IllegalArgumentException(
                                        "Invalid Google authentication.");
                }
        }

        public record GoogleUserInfo(
                        String googleId,
                        String email,
                        String name,
                        String picture) {
        }
}