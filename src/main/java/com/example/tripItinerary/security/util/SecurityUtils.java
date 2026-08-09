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

        System.out.println("SecurityContextHolder: " + SecurityContextHolder.getContext().getAuthentication());
        return SecurityContextHolder
                .getContext()
                .getAuthentication();

    }

    /**
     * Is User Logged In
     */
    public boolean isAuthenticated() {

        Authentication authentication = getAuthentication();
        System.out.println("Authentication: " + authentication);
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

    }

    /**
     * Current Logged User Email
     */
    public String getCurrentUserEmail() {
        System.out.println("Getting current user email...");
        if (!isAuthenticated()) {

            return null;

        }

        System.out.println("Current User Email: " + getAuthentication().getName());
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

        System.out.println("Fetching current user from repository with email: " + email);
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