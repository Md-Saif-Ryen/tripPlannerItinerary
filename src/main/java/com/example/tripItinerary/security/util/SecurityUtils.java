package com.example.tripItinerary.security.util;

import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.Repo.UserRepository;
import com.example.tripItinerary.security.user.CustomUserDetails;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Current Authentication
     */
    public Authentication getAuthentication() {

        return SecurityContextHolder
                .getContext()
                .getAuthentication();

    }

    /**
     * Is User Logged In
     */
    public boolean isAuthenticated() {

        Authentication authentication = getAuthentication();

        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

    }

    /**
     * Current Logged User Email
     */
    public String getCurrentUserEmail() {

        if (!isAuthenticated()) {

            return null;

        }

        return getAuthentication().getName();

    }

    /**
     * Current UserDetails
     */
    public CustomUserDetails getCurrentUserDetails() {

        if (!isAuthenticated()) {

            return null;

        }

        return (CustomUserDetails) getAuthentication().getPrincipal();

    }

    /**
     * Current User Entity
     */
    public User getCurrentUser() {

        String email = getCurrentUserEmail();

        if (email == null) {

            return null;

        }

        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found."));

    }

    /**
     * Current User Id
     */
    public Long getCurrentUserId() {

        return getCurrentUser().getId();

    }

    /**
     * Is Admin
     */
    public boolean isAdmin() {

        User user = getCurrentUser();

        return user != null
                && user.getRole().name().equals("ADMIN");

    }

}