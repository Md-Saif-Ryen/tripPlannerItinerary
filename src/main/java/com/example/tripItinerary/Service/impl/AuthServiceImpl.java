package com.example.tripItinerary.Service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Transactional
public class AuthServiceImpl implements AuthService {

        private static final int MAX_FCM_TOKENS = 5;

        private final UserRepository userRepository;
        private final UserMapper userMapper;
        private final GoogleAuthService googleAuthService;
        private final PasswordEncoder passwordEncoder;
        private final AuthenticationManager authenticationManager;
        private final JwtService jwtService;
        private final SecurityUtils securityUtils;

        // ============================================================
        // ADD / UPDATE FCM TOKEN
        // ============================================================

        private void addFcmToken(
                        User user,
                        String fcmToken) {

                if (fcmToken == null ||
                                fcmToken.trim().isEmpty()) {
                        return;
                }

                String token = fcmToken.trim();

                if (user.getFcmTokens() == null) {
                        user.setFcmTokens(new ArrayList<>());
                }

                List<String> tokens = user.getFcmTokens();

                // Remove duplicate token.
                tokens.remove(token);

                // New token always goes to top.
                tokens.add(0, token);

                // Keep only latest 5.
                while (tokens.size() > MAX_FCM_TOKENS) {
                        tokens.remove(tokens.size() - 1);
                }
        }

        // ============================================================
        // REGISTER
        // ============================================================

        @Override
        public AuthResponse register(RegisterRequest request) {

                String email = request.getEmail()
                                .trim()
                                .toLowerCase();

                if (userRepository.existsByEmail(email)) {
                        throw new RuntimeException(
                                        "Email already exists.");
                }

                User user = userMapper.toEntity(request);

                // Make sure normalized email is stored.
                user.setEmail(email);

                // Password hash.
                user.setPasswordHash(
                                passwordEncoder.encode(
                                                request.getPassword()));

                user.setRole(Role.USER);
                user.setActive(true);

                // --------------------------------------------------------
                // Verification defaults
                // --------------------------------------------------------

                user.setIsEmailVerified(false);
                user.setIsMobileVerified(false);

                // --------------------------------------------------------
                // Optional fields
                // --------------------------------------------------------

                user.setGender(request.getGender());
                user.setDob(request.getDob());
                user.setPhoneNumber(request.getPhoneNumber());
                user.setProfileImage(request.getProfileImage());

                user = userRepository.save(user);

                String token = jwtService.generateToken(
                                new CustomUserDetails(user));

                return AuthResponse.builder()
                                .success(true)
                                .message("Registration successful.")
                                .token(token)
                                .tokenType("Bearer")
                                .fcmToken(
                                                getLatestFcmToken(user))
                                .user(
                                                userMapper.toResponse(user))
                                .build();
        }

        // ============================================================
        // LOGIN
        // ============================================================

        @Override
        public AuthResponse login(LoginRequest request) {

                String email = request.getEmail()
                                .trim()
                                .toLowerCase();

                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                email,
                                                request.getPassword()));

                User user = userRepository
                                .findByEmail(email)
                                .orElseThrow(
                                                () -> new RuntimeException(
                                                                "User not found."));

                if (!Boolean.TRUE.equals(user.getActive())) {
                        throw new RuntimeException(
                                        "User account is inactive.");
                }

                // --------------------------------------------------------
                // Add latest FCM token
                // --------------------------------------------------------

                addFcmToken(
                                user,
                                request.getFcmToken());

                user = userRepository.save(user);

                String token = jwtService.generateToken(
                                new CustomUserDetails(user));

                return AuthResponse.builder()
                                .success(true)
                                .message("Login successful.")
                                .token(token)
                                .tokenType("Bearer")
                                .fcmToken(
                                                getLatestFcmToken(user))
                                .user(
                                                userMapper.toResponse(user))
                                .build();
        }

        // ============================================================
        // GOOGLE LOGIN
        // ============================================================

        @Override
        public AuthResponse googleLogin(
                        GoogleLoginRequest request) {

                GoogleUserInfo googleUser = googleAuthService.verifyToken(
                                request.getIdToken());

                String email = googleUser.getEmail()
                                .trim()
                                .toLowerCase();

                User user = userRepository
                                .findByEmail(email)
                                .orElse(null);

                // --------------------------------------------------------
                // CREATE NEW GOOGLE USER
                // --------------------------------------------------------

                if (user == null) {

                        user = new User();

                        user.setEmail(email);

                        user.setFullName(
                                        googleUser.getName());

                        user.setProfileImage(
                                        googleUser.getPicture());

                        user.setRole(Role.USER);
                        user.setActive(true);

                        // Google verified email.
                        user.setIsEmailVerified(true);

                        // Mobile not verified.
                        user.setIsMobileVerified(false);

                        addFcmToken(
                                        user,
                                        request.getFcmToken());

                        user = userRepository.save(user);

                } else {

                        if (!Boolean.TRUE.equals(
                                        user.getActive())) {
                                throw new RuntimeException(
                                                "User account is inactive.");
                        }

                        addFcmToken(
                                        user,
                                        request.getFcmToken());

                        user = userRepository.save(user);
                }

                String token = jwtService.generateToken(
                                new CustomUserDetails(user));

                return AuthResponse.builder()
                                .success(true)
                                .message("Google login successful.")
                                .token(token)
                                .tokenType("Bearer")
                                .fcmToken(
                                                getLatestFcmToken(user))
                                .user(
                                                userMapper.toResponse(user))
                                .build();
        }

        // ============================================================
        // CURRENT USER
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public UserResponse getCurrentUser() {

                User user = securityUtils.getCurrentUser();

                System.out.println(
                                "Current User: "
                                                + user.getFullName()
                                                + ", Role: "
                                                + user.getRole()
                                                + ", Active: "
                                                + user.getActive());

                return userMapper.toResponse(user);
        }

        // ============================================================
        // LATEST FCM TOKEN
        // ============================================================

        private String getLatestFcmToken(User user) {

                if (user.getFcmTokens() == null ||
                                user.getFcmTokens().isEmpty()) {
                        return null;
                }

                return user.getFcmTokens().get(0);
        }
}