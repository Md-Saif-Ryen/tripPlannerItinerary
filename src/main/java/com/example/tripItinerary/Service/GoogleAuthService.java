package com.example.tripItinerary.Service;

import org.springframework.stereotype.Service;

import com.example.tripItinerary.DTO.GoogleUserInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

@Service
public class GoogleAuthService {

    public GoogleUserInfo verifyToken(String idTokenString) {

        if (idTokenString == null ||
                idTokenString.isBlank()) {

            throw new RuntimeException(
                    "Google ID token is required.");
        }

        try {

            /*
             * Verify Firebase ID Token
             *
             * Flutter:
             * FirebaseAuth.signInWithCredential(...)
             *        ↓
             * Firebase ID Token
             *        ↓
             * Backend
             *        ↓
             * Firebase Admin SDK
             */
            FirebaseToken decodedToken =
                    FirebaseAuth.getInstance()
                            .verifyIdToken(idTokenString);

            String firebaseUid =
                    decodedToken.getUid();

            String email =
                    decodedToken.getEmail();

            String name =
                    decodedToken.getName();

            String picture =
                    decodedToken.getPicture();

            if (email == null ||
                    email.isBlank()) {

                throw new RuntimeException(
                        "Google account email is missing.");
            }

            return new GoogleUserInfo(
                    firebaseUid,
                    email,
                    name,
                    picture);

        } catch (FirebaseAuthException e) {

            throw new RuntimeException(
                    "Invalid Firebase ID token.",
                    e);
        }
    }
}