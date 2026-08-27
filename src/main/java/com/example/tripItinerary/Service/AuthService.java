package com.example.tripItinerary.Service;

import com.example.tripItinerary.DTO.request.GoogleLoginRequest;
import com.example.tripItinerary.DTO.request.LoginRequest;
import com.example.tripItinerary.DTO.request.RegisterRequest;
import com.example.tripItinerary.DTO.response.AuthResponse;
import com.example.tripItinerary.DTO.response.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser();
    AuthResponse googleLogin(GoogleLoginRequest request);

}