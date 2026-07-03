package com.example.tripItinerary.Service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.ItineraryRequest;
import com.example.tripItinerary.DTO.response.ItineraryResponse;
import com.example.tripItinerary.Entity.Itinerary;
import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.Mapper.ItineraryMapper;
import com.example.tripItinerary.Repo.ItineraryRepository;
import com.example.tripItinerary.Repo.LocationRepository;
import com.example.tripItinerary.Repo.UserRepository;
import com.example.tripItinerary.Service.ItineraryService;
import com.example.tripItinerary.exception.ResourceNotFoundException;
import com.example.tripItinerary.security.util.SecurityUtils;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItineraryServiceImpl implements ItineraryService {

       

        private final ItineraryRepository itineraryRepository;

        private final UserRepository userRepository;

        private final LocationRepository locationRepository;

        private final ItineraryMapper itineraryMapper;

        private final SecurityUtils securityUtils;

        // ==========================================================
        // CREATE ITINERARY
        // ==========================================================

        /**
         * Create a new itinerary.
         *
         * Flow:
         * 1. Validate request
         * 2. Fetch User
         * 3. Fetch Location
         * 4. Convert DTO -> Entity
         * 5. Set Relations
         * 6. Save
         * 7. Return Response DTO
         */
        @Override
        public ItineraryResponse create(ItineraryRequest request) {

                log.info("Creating itinerary...");

                // Validate request data
                validateRequest(request);

                // Fetch required entities
                User user = getUser(request.getUserId());

                Location location = getLocation(request.getLocationId());

                // Convert Request DTO to Entity
                Itinerary itinerary = itineraryMapper.toEntity(request);

                // Copy all editable fields
                updateItinerary(itinerary, request, user, location);

                // Save entity
                @SuppressWarnings("null")
                Itinerary savedItinerary = itineraryRepository.save(itinerary);

                log.info("Itinerary created successfully with id={}",
                                savedItinerary.getId());

                // Convert Entity -> DTO
                return itineraryMapper.toResponse(savedItinerary);
        }

        // ==========================================================
        // VALIDATION
        // ==========================================================

        /**
         * Validate request before saving/updating.
         */
        private void validateRequest(ItineraryRequest request) {

                Objects.requireNonNull(request,
                                "Itinerary Request cannot be null.");

                if (request.getUserId() == null) {
                        throw new IllegalArgumentException(
                                        "User Id is required.");
                }

                if (request.getLocationId() == null) {
                        throw new IllegalArgumentException(
                                        "Location Id is required.");
                }

                if (request.getTitle() == null
                                || request.getTitle().isBlank()) {

                        throw new IllegalArgumentException(
                                        "Title is required.");
                }

                if (request.getTotalDays() == null
                                || request.getTotalDays() <= 0) {

                        throw new IllegalArgumentException(
                                        "Total Days must be greater than zero.");
                }

                if (request.getTotalBudget() != null
                                && request.getTotalBudget().signum() < 0) {

                        throw new IllegalArgumentException(
                                        "Budget cannot be negative.");
                }

                if (request.getEstimatedCost() != null
                                && request.getEstimatedCost().signum() < 0) {

                        throw new IllegalArgumentException(
                                        "Estimated Cost cannot be negative.");
                }
        }

        // ==========================================================
        // BUSINESS METHOD
        // ==========================================================

        /**
         * Copy request values into entity.
         *
         * This method is shared by:
         * - create()
         * - update()
         */
        private void updateItinerary(Itinerary itinerary,
                        ItineraryRequest request,
                        User user,
                        Location location) {

                // Relationships
                itinerary.setUser(user);

                itinerary.setLocation(location);

                // Basic Information
                itinerary.setTitle(request.getTitle());

                itinerary.setDescription(request.getDescription());

                itinerary.setTotalDays(request.getTotalDays());

                itinerary.setTravelType(request.getTravelType());

                itinerary.setTotalBudget(request.getTotalBudget());

                itinerary.setEstimatedCost(request.getEstimatedCost());
        }

        // ==========================================================
        // HELPER METHODS
        // ==========================================================

        /**
         * Fetch User by Id.
         */
        private User getUser(@NonNull Long userId) {

                return userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "User not found with id : "
                                                                + userId));
        }

        /**
         * Fetch Location by Id.
         */
        private Location getLocation(@NonNull Long locationId) {

                return locationRepository.findById(locationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id : "
                                                                + locationId));
        }

        /**
         * Fetch Itinerary by Id.
         */
        private Itinerary getItinerary(@NonNull Long itineraryId) {

                return itineraryRepository.findById(itineraryId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Itinerary not found with id : "
                                                                + itineraryId));
        }

    /**
     * Fetch itinerary of logged-in user only.
     */
    @SuppressWarnings("unused")
private Itinerary getMyItinerary(@NonNull Long itineraryId) {

        Long userId = securityUtils.getCurrentUserId();

        return itineraryRepository
                .findByIdAndUser(itineraryId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Itinerary not found with id : "
                                        + itineraryId));
    }
    // ==========================================================
    // UPDATE ITINERARY
    // ==========================================================

    /**
     * Update existing itinerary.
     *
     * Flow:
     * 1. Validate request
     * 2. Fetch existing itinerary
     * 3. Fetch User
     * 4. Fetch Location
     * 5. Update fields
     * 6. Save
     * 7. Return Response
     */
    @Override
    public ItineraryResponse update(@NonNull Long id, @NonNull ItineraryRequest request) {

            log.info("Updating itinerary with id={}", id);

            // ------------------------------------------------------
            // Validate request
            // ------------------------------------------------------
            validateRequest(request);

            // ------------------------------------------------------
            // Fetch existing itinerary
            // ------------------------------------------------------
            Itinerary itinerary = getItinerary(id);

            // ------------------------------------------------------
            // Fetch related entities
            // ------------------------------------------------------
            User user = getUser(request.getUserId());

            Location location = getLocation(request.getLocationId());

            // ------------------------------------------------------
            // Copy all updated values
            // ------------------------------------------------------
            updateItinerary(
                            itinerary,
                            request,
                            user,
                            location);

            // ------------------------------------------------------
            // Save updated itinerary
            // ------------------------------------------------------
            @SuppressWarnings("null")
            Itinerary updatedItinerary = itineraryRepository.save(itinerary);

            log.info("Itinerary updated successfully with id={}",
                            updatedItinerary.getId());

            // ------------------------------------------------------
            // Convert Entity -> Response DTO
            // ------------------------------------------------------
            return itineraryMapper.toResponse(updatedItinerary);
    }

    // ==========================================================
    // GET ITINERARY BY ID
    // ==========================================================

    /**
     * Fetch single itinerary using Id.
     */
    @Override
    @Transactional(readOnly = true)
    public ItineraryResponse getById(@NonNull Long id) {

            log.info("Fetching itinerary with id={}", id);

            // ------------------------------------------------------
            // Fetch itinerary
            // ------------------------------------------------------
            Itinerary itinerary = getItinerary(id);

            // ------------------------------------------------------
            // Convert Entity -> DTO
            // ------------------------------------------------------
            return itineraryMapper.toResponse(itinerary);
    }

    // ==========================================================
    // CHECK OWNERSHIP (Reusable)
    // ==========================================================

/**
     * Verify that the itinerary belongs to the logged-in user.
     *
     * Note:
     * Use this helper in update() and delete() if your application
     * allows users to modify only their own itineraries.
     */
    private void validateOwnership(Itinerary itinerary) {

        Long currentUserId = securityUtils.getCurrentUserId();

        if (!itinerary.getUser().getId().equals(currentUserId)) {

            throw new ResourceNotFoundException(
                    "You are not authorized to access this itinerary."
            );
        }
    }
    // ==========================================================
    // GET MY ITINERARIES
    // ==========================================================

    /**
     * Fetch all itineraries of currently logged-in user.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ItineraryResponse> getMyItineraries() {

            log.info("Fetching logged-in user itineraries...");

            // Logged-in User Id
            Long userId = securityUtils.getCurrentUserId();

            // Fetch all itineraries
            List<Itinerary> itineraries = itineraryRepository.findByUserId(userId);

            // Convert Entity List -> Response List
            return itineraries.stream()
                            .map(itineraryMapper::toResponse)
                            .toList();
    }

    // ==========================================================
    // DELETE ITINERARY
    // ==========================================================

    /**
     * Delete itinerary.
     *
     * Only owner can delete itinerary.
     */
    @SuppressWarnings("null")
@Override
    public void delete(Long id) {

            log.info("Deleting itinerary id={}", id);

            // Fetch itinerary
            Itinerary itinerary = getItinerary(id);

            // Validate ownership
            validateOwnership(itinerary);

            // Delete
            itineraryRepository.delete(itinerary);

            log.info("Itinerary deleted successfully.");
    }

    // ==========================================================
    // OPTIONAL BUSINESS METHODS
    // ==========================================================

    /**
     * Remaining Budget
     *
     * totalBudget - estimatedCost
     */
    @SuppressWarnings("unused")
private java.math.BigDecimal calculateRemainingBudget(Itinerary itinerary) {

            if (itinerary.getTotalBudget() == null
                            || itinerary.getEstimatedCost() == null) {

                    return java.math.BigDecimal.ZERO;
            }

            return itinerary.getTotalBudget()
                            .subtract(itinerary.getEstimatedCost());
    }

    /**
     * Calculate Estimated Cost.
     *
     * NOTE:
     * Abhi project me itinerary places nahi hain,
     * isliye existing estimatedCost return kar rahe hain.
     *
     * Future me ye Tourist Places ke cost ka sum karega.
     */
    @SuppressWarnings("unused")
private java.math.BigDecimal calculateEstimatedCost(Itinerary itinerary) {

            if (itinerary.getEstimatedCost() == null) {
                    return java.math.BigDecimal.ZERO;
            }

            return itinerary.getEstimatedCost();
    }

    /**
     * Update itinerary status.
     *
     * NOTE:
     * Jab tumhare enum me COMPLETE / PLANNED / CANCELLED
     * properly use honge tab ye method aur useful hoga.
     */
    @SuppressWarnings("unused")
private void updateStatusAutomatically(Itinerary itinerary) {

            if (itinerary.getEstimatedCost() == null
                            || itinerary.getTotalBudget() == null) {
                    return;
            }

            if (itinerary.getEstimatedCost()
                            .compareTo(itinerary.getTotalBudget()) > 0) {

                    log.warn("Estimated cost exceeds budget for itinerary={}",
                                    itinerary.getId());
            }
    }

    /**
     * Placeholder for future.
     *
     * Future implementation:
     * - Create Day-1
     * - Create Day-2
     * - Create Day-3
     * ...
     *
     * Current project me ItineraryDay logic use nahi ho raha.
     */
    @SuppressWarnings("unused")
private void createDefaultDays(Itinerary itinerary) {

            log.debug("Default day generation skipped.");
    }

    /**
     * Placeholder for future.
     *
     * Future:
     * Automatically assign tourist places
     * according to popularity/rating.
     */
    @SuppressWarnings("unused")
private void addTouristPlacesAutomatically(Itinerary itinerary) {

            log.debug("Automatic tourist place assignment skipped.");
    }

}