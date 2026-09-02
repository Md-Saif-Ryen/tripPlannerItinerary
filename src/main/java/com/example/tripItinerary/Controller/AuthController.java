package com.example.tripItinerary.Controller;

import com.example.tripItinerary.DTO.request.GoogleLoginRequest;
import com.example.tripItinerary.DTO.request.LoginRequest;
import com.example.tripItinerary.DTO.request.RegisterRequest;
import com.example.tripItinerary.DTO.response.AuthResponse;
import com.example.tripItinerary.DTO.response.UserResponse;
import com.example.tripItinerary.Service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Register User
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    /**
     * Login User
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Current Logged User
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> currentUser() {

        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {

        return ResponseEntity.ok(
                authService.googleLogin(request));
    }

}