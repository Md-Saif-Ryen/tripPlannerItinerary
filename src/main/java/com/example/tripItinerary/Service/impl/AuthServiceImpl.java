package com.example.tripItinerary.Service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.tripItinerary.DTO.GoogleUserInfo;
import com.example.tripItinerary.DTO.request.GoogleLoginRequest;
import com.example.tripItinerary.DTO.request.LoginRequest;
import com.example.tripItinerary.DTO.request.RegisterRequest;
import com.example.tripItinerary.DTO.response.AuthResponse;
import com.example.tripItinerary.DTO.response.UserResponse;
import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.Mapper.UserMapper;
import com.example.tripItinerary.Repo.UserRepository;
import com.example.tripItinerary.Service.AuthService;
import com.example.tripItinerary.Service.GoogleAuthService;
import com.example.tripItinerary.enums.Role;
import com.example.tripItinerary.security.jwt.JwtService;
import com.example.tripItinerary.security.user.CustomUserDetails;
import com.example.tripItinerary.security.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final GoogleAuthService googleAuthService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;
    private final SecurityUtils securityUtils;

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {

            throw new RuntimeException("Email already exists.");

        }

        User user = userMapper.toEntity(request);

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setRole(Role.USER);

        user.setActive(true);

        user = userRepository.save(user);

        String token = jwtService.generateToken(new CustomUserDetails(user));

        return AuthResponse.builder()
                .success(true)
                .message("Registration successful.")
                .token(token)
                .tokenType("Bearer")
                .user(userMapper.toResponse(user))
                .build();

    }

    @Override
    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())

        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found."));

        String token = jwtService.generateToken(new CustomUserDetails(user));

        return AuthResponse.builder()
                .success(true)
                .message("Login successful.")
                .token(token)
                .tokenType("Bearer")
                .fcmToken("asdfjsdfkjadhsfjkadnsfkjahsfeejwkdsfulidsvj")
                .user(userMapper.toResponse(user))
                .build();

    }

    @Override
    public AuthResponse googleLogin(GoogleLoginRequest request) {

        // ---------------------------------------------------------
        // 1. VERIFY GOOGLE ID TOKEN
        // ---------------------------------------------------------

        GoogleUserInfo googleUser = googleAuthService.verifyToken(
                request.getIdToken());

        // ---------------------------------------------------------
        // 2. FIND EXISTING USER BY EMAIL
        // ---------------------------------------------------------

        User user = userRepository.findByEmail(
                googleUser.getEmail()).orElse(null);

        // ---------------------------------------------------------
        // 3. CREATE USER IF NOT EXISTS
        // ---------------------------------------------------------

        if (user == null) {

            user = new User();

            user.setEmail(
                    googleUser.getEmail());

            user.setFullName(
                    googleUser.getName());

            user.setProfileImage(
                    googleUser.getPicture());

            user.setRole(Role.USER);

            user.setActive(true);

            /*
             * Google users don't authenticate using your
             * normal password.
             *
             * If passwordHash is mandatory in your Entity/DB,
             * we will handle that separately.
             */

            user = userRepository.save(user);
        }

        // ---------------------------------------------------------
        // 4. CHECK ACCOUNT STATUS
        // ---------------------------------------------------------

        if (!user.getActive()) {

            throw new RuntimeException(
                    "User account is inactive.");
        }

        // ---------------------------------------------------------
        // 5. GENERATE YOUR APPLICATION JWT
        // ---------------------------------------------------------

        String token = jwtService.generateToken(
                new CustomUserDetails(user));

        // ---------------------------------------------------------
        // 6. RETURN SAME AUTH RESPONSE AS NORMAL LOGIN
        // ---------------------------------------------------------

        return AuthResponse.builder()
                .success(true)
                .message("Google login successful.")
                .token(token)
                .tokenType("Bearer")
                .fcmToken(
                        "asdfjsdfkjadhsfjkadnsfkjahsfeejwkdsfulidsvj")
                .user(
                        userMapper.toResponse(user))
                .build();
    }

    @Override
    public UserResponse getCurrentUser() {

        User user = securityUtils.getCurrentUser();

        System.out.println(
                "Current User: " + user.getFullName() + ", Role: " + user.getRole() + ", Active: " + user.getActive());
        return userMapper.toResponse(user);

    }

}