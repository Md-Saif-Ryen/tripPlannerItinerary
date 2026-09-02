package com.example.tripItinerary.DTO.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.tripItinerary.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String fullName;

    private String email;

    private String profileImage;

    private String phoneNumber;

    private String gender;

    private LocalDate dob;

    private Role role;

    private Boolean active;

    private Boolean isEmailVerified;

    private Boolean isMobileVerified;

    private List<String> fcmTokens;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}