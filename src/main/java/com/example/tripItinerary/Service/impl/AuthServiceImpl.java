package com.example.tripItinerary.Service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.tripItinerary.DTO.request.LoginRequest;
import com.example.tripItinerary.DTO.request.RegisterRequest;
import com.example.tripItinerary.DTO.response.AuthResponse;
import com.example.tripItinerary.DTO.response.UserResponse;
import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.Mapper.UserMapper;
import com.example.tripItinerary.Repo.UserRepository;
import com.example.tripItinerary.Service.AuthService;
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
                .user(userMapper.toResponse(user))
                .build();

    }

    @Override
    public UserResponse getCurrentUser() {

        User user = securityUtils.getCurrentUser();

        return userMapper.toResponse(user);

    }

}