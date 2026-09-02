package com.example.tripItinerary.DTO.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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
public class UserRequest {

    // ============================================================
    // REQUIRED
    // ============================================================

    @NotBlank(message = "Full name is required.")
    private String fullName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters.")
    private String password;

    // ============================================================
    // OPTIONAL
    // ============================================================

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must contain exactly 10 digits.")
    private String phoneNumber;

    @Size(max = 1000)
    private String profileImage;

    private String gender;

    private java.time.LocalDate dob;

    // ============================================================
    // VERIFICATION
    //
    // IMPORTANT:
    // These should NOT be trusted from Flutter.
    // Backend should always initialize them.
    // ============================================================
    @Builder.Default
    @Column(nullable = false)
    private Boolean isEmailVerified = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isMobileVerified = false;
}