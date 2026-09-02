package com.example.tripItinerary.Service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.UserRequest;
import com.example.tripItinerary.DTO.request.userUpdateRequest;
import com.example.tripItinerary.DTO.response.UserResponse;
import com.example.tripItinerary.Entity.DeletedUser;
import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.Mapper.UserMapper;
import com.example.tripItinerary.Repo.DeletedUserRepository;
import com.example.tripItinerary.Repo.UserRepository;
import com.example.tripItinerary.Service.UserService;
import com.example.tripItinerary.enums.Role;
import com.example.tripItinerary.exception.BadRequestException;
import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

        private final UserRepository userRepository;

        private final DeletedUserRepository deletedUserRepository;

        private final UserMapper userMapper;

        private final PasswordEncoder passwordEncoder;

        // ============================================================
        // CREATE
        // ============================================================

        @Override
        public UserResponse create(UserRequest request) {

                String email = request.getEmail()
                                .trim()
                                .toLowerCase();

                if (userRepository.existsByEmail(email)) {
                        throw new BadRequestException(
                                        "Email already exists.");
                }

                User user = userMapper.toEntity(request);

                user.setEmail(email);

                user.setPasswordHash(
                                passwordEncoder.encode(
                                                request.getPassword()));

                user.setRole(Role.USER);
                user.setActive(true);

                // Defaults
                user.setIsEmailVerified(false);
                user.setIsMobileVerified(false);

                return userMapper.toResponse(
                                userRepository.save(user));
        }

        // ============================================================
        // UPDATE
        // ============================================================

        @Override
        public UserResponse update(
                        @NonNull Long id,
                        userUpdateRequest request) {

                User user = userRepository.findById(id)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "User not found with id : " + id));

                String newEmail = request.getEmail()
                                .trim()
                                .toLowerCase();

                if (!user.getEmail().equals(newEmail)
                                && userRepository.existsByEmail(newEmail)) {

                        throw new BadRequestException(
                                        "Email already exists.");
                }

                user.setEmail(newEmail);
                user.setFullName(
                                request.getFullName());
                user.setProfileImage(
                                request.getProfileImage());

                return userMapper.toResponse(
                                userRepository.save(user));
        }

        // ============================================================
        // GET BY ID
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public UserResponse getById(
                        @NonNull Long id) {

                User user = userRepository.findById(id)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "User not found with id : " + id));

                return userMapper.toResponse(user);
        }

        // ============================================================
        // GET ALL
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public List<UserResponse> getAll() {

                return userRepository.findAll()
                                .stream()
                                .map(userMapper::toResponse)
                                .collect(Collectors.toList());
        }

        // ============================================================
        // DELETE USER
        // ============================================================
        //
        // STEP 1:
        // Find user.
        //
        // STEP 2:
        // Copy user data to deleted_users.
        //
        // STEP 3:
        // Save deleted user.
        //
        // STEP 4:
        // Delete original user.
        //
        // Both operations same transaction mein hain.
        // Agar archive save fail hua -> original delete nahi hoga.
        // ============================================================

        @Override
        public void delete(@NonNull Long id) {

                User user = userRepository.findById(id)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "User not found with id : " + id));

                // --------------------------------------------------------
                // 1. Create deleted user snapshot
                // --------------------------------------------------------

                DeletedUser deletedUser = DeletedUser.builder()

                                .originalUserId(
                                                user.getId())

                                .fullName(
                                                user.getFullName())

                                .email(
                                                user.getEmail())

                                .passwordHash(
                                                user.getPasswordHash())

                                .profileImage(
                                                user.getProfileImage())

                                .phoneNumber(
                                                user.getPhoneNumber())

                                .gender(
                                                user.getGender())

                                .dob(
                                                user.getDob())

                                .isEmailVerified(
                                                user.getIsEmailVerified())

                                .isMobileVerified(
                                                user.getIsMobileVerified())

                                .active(
                                                user.getActive())

                                .fcmTokens(
                                                user.getFcmTokens() == null
                                                                ? new ArrayList<>()
                                                                : new ArrayList<>(
                                                                                user.getFcmTokens()))

                                .originalCreatedAt(
                                                user.getCreatedAt())

                                .originalUpdatedAt(
                                                user.getUpdatedAt())

                                .deletedAt(
                                                LocalDateTime.now())

                                .build();

                // --------------------------------------------------------
                // 2. Save into deleted_users
                // --------------------------------------------------------

                deletedUserRepository.save(deletedUser);

                // --------------------------------------------------------
                // 3. Delete original user
                // --------------------------------------------------------

                userRepository.delete(user);
        }
}