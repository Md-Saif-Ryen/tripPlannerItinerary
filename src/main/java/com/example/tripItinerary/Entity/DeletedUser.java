package com.example.tripItinerary.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "deleted_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeletedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Original user ID
    @Column(name = "original_user_id", nullable = false)
    private Long originalUserId;

    // ============================================================
    // USER DATA SNAPSHOT
    // ============================================================

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "profile_image", length = 1000)
    private String profileImage;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(length = 50)
    private String gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified;

    @Column(name = "is_mobile_verified", nullable = false)
    private Boolean isMobileVerified;

    @Column(nullable = false)
    private Boolean active;

    // ============================================================
    // FCM TOKENS
    // ============================================================

    @ElementCollection
    @CollectionTable(name = "deleted_user_fcm_tokens", joinColumns = @JoinColumn(name = "deleted_user_id"))
    @OrderColumn(name = "token_order")
    @Column(name = "fcm_token", length = 500)
    @Builder.Default
    private List<String> fcmTokens = new ArrayList<>();

    // ============================================================
    // ORIGINAL DATES
    // ============================================================

    @Column(name = "original_created_at")
    private LocalDateTime originalCreatedAt;

    @Column(name = "original_updated_at")
    private LocalDateTime originalUpdatedAt;

    // ============================================================
    // DELETION DATE
    // ============================================================

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;
}