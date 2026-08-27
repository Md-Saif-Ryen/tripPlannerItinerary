package com.example.tripItinerary.Service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.ItineraryRequest;
import com.example.tripItinerary.DTO.response.ItineraryResponse;
import com.example.tripItinerary.DTO.response.UserSummaryResponse;
import com.example.tripItinerary.Entity.Hotel;
import com.example.tripItinerary.Entity.Itinerary;
import com.example.tripItinerary.Entity.ItineraryDay;
import com.example.tripItinerary.Entity.ItineraryPlace;
import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Entity.Restaurant;
import com.example.tripItinerary.Entity.TemporaryItinerary;
import com.example.tripItinerary.Entity.TouristPlace;
import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.Mapper.ItineraryMapper;
import com.example.tripItinerary.Mapper.UserSummaryMapper;
import com.example.tripItinerary.Repo.HotelRepository;
import com.example.tripItinerary.Repo.ItineraryDayRepository;
import com.example.tripItinerary.Repo.ItineraryPlaceRepository;
import com.example.tripItinerary.Repo.ItineraryRepository;
import com.example.tripItinerary.Repo.LocationRepository;
import com.example.tripItinerary.Repo.RestaurantRepository;
import com.example.tripItinerary.Repo.TemporaryItineraryRepository;
import com.example.tripItinerary.Repo.TouristPlaceRepository;
import com.example.tripItinerary.Repo.UserRepository;
import com.example.tripItinerary.Service.ItineraryService;
import com.example.tripItinerary.enums.ItineraryStatus;
import com.example.tripItinerary.enums.PlaceType;
import com.example.tripItinerary.exception.ResourceNotFoundException;
import com.example.tripItinerary.exception.UnauthorizedException;
import com.example.tripItinerary.security.util.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ItineraryServiceImpl implements ItineraryService {

        // ==========================================================
        // REPOSITORIES
        // ==========================================================

        private final ItineraryRepository itineraryRepository;
        private final UserRepository userRepository;
        private final LocationRepository locationRepository;

        private final TouristPlaceRepository touristPlaceRepository;
        private final HotelRepository hotelRepository;
        private final RestaurantRepository restaurantRepository;

        private final ItineraryDayRepository itineraryDayRepository;
        private final ItineraryPlaceRepository itineraryPlaceRepository;

        // ==========================================================
        // MAPPERS / SECURITY
        // ==========================================================

        private final ItineraryMapper itineraryMapper;
        private final UserSummaryMapper userSummaryMapper;
        private final SecurityUtils securityUtils;
        private final ObjectMapper objectMapper;
        private final TemporaryItineraryRepository temporaryItineraryRepository;

        // ==========================================================
        // CONSTANTS
        // ==========================================================

        private static final int MAX_DAYS = 30;

        private static final int MAX_PLACES_PER_DAY = 5;

        private static final int DEFAULT_VISIT_MINUTES = 90;

        private static final int DEFAULT_TRAVEL_MINUTES = 30;

        private static final int NUMBER_OF_OPTIONS = 3;

        // ==========================================================
        // BUDGET DISTRIBUTION
        // ==========================================================

        private static final BigDecimal HOTEL_BUDGET_PERCENTAGE = BigDecimal.valueOf(0.30);

        private static final BigDecimal RESTAURANT_BUDGET_PERCENTAGE = BigDecimal.valueOf(0.30);

        private static final BigDecimal PLACES_BUDGET_PERCENTAGE = BigDecimal.valueOf(0.25);

        // ==========================================================
        // TEMPORARY GENERATED OPTIONS
        // ==========================================================
        //
        // create()
        // ↓
        // Generate 3 options
        // ↓
        // Store in memory
        // ↓
        // Return selectionId
        //
        // selectGeneratedItinerary()
        // ↓
        // Find selectionId
        // ↓
        // Reset temporary IDs
        // ↓
        // Save itinerary
        // ↓
        // Save days
        // ↓
        // Save places
        //
        // NOTE:
        // ConcurrentHashMap is okay for current single-server setup.
        // Production multi-instance deployment should use Redis/database.
        //
        // ==========================================================

        // ==========================================================
        // CREATE / GENERATE 3 OPTIONS
        // ==========================================================

        @Override
        public List<ItineraryResponse> create(
                        ItineraryRequest request) {

                log.info(
                                "Generating {} itinerary options for user={}",
                                NUMBER_OF_OPTIONS,
                                request != null
                                                ? request.getUserId()
                                                : null);

                // ======================================================
                // STEP 1: VALIDATE REQUEST
                // ======================================================

                validateRequest(request);

                // ======================================================
                // STEP 2: FETCH USER
                // ======================================================

                User user = getUser(
                                request.getUserId());

                System.out.println(
                                "printing the user object" + request.getUserId());
                // ======================================================
                // STEP 3: FETCH LOCATION
                // ======================================================

                Location location = getLocation(
                                request.getLocationId());

                // ======================================================
                // STEP 4: FETCH ALL SOURCE DATA ONCE
                // ======================================================

                List<TouristPlace> allTouristPlaces = fetchTouristPlaces(location);

                List<Hotel> allHotels = hotelRepository.findByLocationId(
                                location.getId());

                List<Restaurant> allRestaurants = restaurantRepository
                                .findByLocationIdAndActiveTrue(
                                                location.getId());

                log.info(
                                "Source data => places={}, hotels={}, restaurants={}",
                                allTouristPlaces.size(),
                                allHotels.size(),
                                allRestaurants.size());

                // ======================================================
                // STEP 5: GENERATE THREE OPTIONS
                // ======================================================

                List<ItineraryResponse> responses = new ArrayList<>();

                for (int optionNumber = 1; optionNumber <= NUMBER_OF_OPTIONS; optionNumber++) {

                        log.info(
                                        "Generating itinerary option {}",
                                        optionNumber);

                        // ==================================================
                        // GENERATION TIMESTAMP
                        // ==================================================

                        LocalDateTime generatedAt = LocalDateTime.now();

                        // ==================================================
                        // CREATE ENTITY IN MEMORY
                        // ==================================================

                        Itinerary itinerary = itineraryMapper.toEntity(request);

                        buildItinerary(
                                        itinerary,
                                        request,
                                        user,
                                        location);

                        // ==================================================
                        // TIMESTAMPS
                        // ==================================================

                        itinerary.setCreatedAt(
                                        generatedAt);

                        itinerary.setUpdatedAt(
                                        generatedAt);

                        // ==================================================
                        // CREATE DAYS IN MEMORY
                        // ==================================================

                        createDefaultDaysInMemory(
                                        itinerary,
                                        generatedAt,
                                        optionNumber);

                        // ==================================================
                        // TOURIST PLACES
                        // ==================================================

                        List<TouristPlace> filteredPlaces = filterTouristPlaces(
                                        allTouristPlaces,
                                        itinerary);

                        List<TouristPlace> sortedPlaces = sortTouristPlacesForOption(
                                        filteredPlaces,
                                        optionNumber);

                        // ==================================================
                        // ASSIGN TOURIST PLACES
                        // ==================================================

                        assignPlacesToDaysInMemory(
                                        itinerary,
                                        sortedPlaces,
                                        optionNumber);

                        // ==================================================
                        // HOTELS
                        // ==================================================

                        List<Hotel> hotels = assignHotelsForOption(
                                        itinerary,
                                        allHotels,
                                        optionNumber);

                        assignHotelsToItineraryInMemory(
                                        itinerary,
                                        hotels,
                                        optionNumber);

                        // ==================================================
                        // RESTAURANTS
                        // ==================================================

                        List<Restaurant> restaurants = assignRestaurantsForOption(
                                        itinerary,
                                        allRestaurants,
                                        optionNumber);

                        assignRestaurantsToItineraryInMemory(
                                        itinerary,
                                        restaurants,
                                        optionNumber);

                        // ==================================================
                        // CALCULATE ESTIMATED COST
                        // ==================================================

                        BigDecimal estimatedCost = calculateTotalEstimatedCost(
                                        itinerary,
                                        hotels,
                                        restaurants,
                                        sortedPlaces);

                        itinerary.setEstimatedCost(
                                        estimatedCost);

                        // ==================================================
                        // REMAINING BUDGET
                        // ==================================================

                        BigDecimal remainingBudget = calculateRemainingBudget(
                                        itinerary);

                        itinerary.setRemainingBudget(
                                        remainingBudget);

                        // ==================================================
                        // STATUS
                        // ==================================================

                        updateStatusAutomatically(
                                        itinerary);

                        // ==================================================
                        // SELECTION ID
                        // ==================================================

                        String selectionId = UUID.randomUUID().toString();

                        // ==================================================
                        // STORE GENERATED OPTION
                        // ==================================================

                        saveTemporaryItinerary(
                                        user.getId(),
                                        selectionId,
                                        optionNumber,
                                        itinerary);

                        // ==================================================
                        // MAP ENTITY -> RESPONSE
                        // ==================================================

                        ItineraryResponse response = itineraryMapper.toResponse(
                                        itinerary);

                        // ==================================================
                        // SET OPTION INFORMATION
                        // ==================================================

                        response.setSelectionId(
                                        selectionId);

                        response.setOptionNumber(
                                        optionNumber);

                        // Ensure generated timestamps are returned
                        response.setCreatedAt(
                                        generatedAt);

                        response.setUpdatedAt(
                                        generatedAt);

                        // ==================================================
                        // ADD RESPONSE
                        // ==================================================

                        responses.add(
                                        response);

                        log.info(
                                        "Option {} generated. selectionId={}, places={}, hotels={}, restaurants={}, estimatedCost={}",
                                        optionNumber,
                                        selectionId,
                                        sortedPlaces.size(),
                                        hotels.size(),
                                        restaurants.size(),
                                        estimatedCost);
                }

                log.info(
                                "Successfully generated {} itinerary options",
                                responses.size());

                return responses;
        }
        // ==========================================================
        // TEMPEROARY SAVED ITINERARY
        // ==========================================================

        private void saveTemporaryItinerary(
                        Long userId,
                        String selectionId,
                        int optionNumber,
                        Itinerary itinerary) {

                try {

                        LocalDateTime now = LocalDateTime.now();

                        LocalDateTime expiresAt = now.plusHours(2);

                        // ==================================================
                        // IMPORTANT
                        // ==================================================
                        //
                        // Entity ko directly JSON serialize karne par
                        // circular relationship aa sakta hai:
                        //
                        // Itinerary
                        // -> Days
                        // -> Itinerary
                        // -> Days
                        //
                        // Isliye response DTO ko JSON me store karna
                        // safer hai.
                        //
                        // ==================================================

                        ItineraryResponse response = itineraryMapper.toResponse(
                                        itinerary);

                        response.setSelectionId(
                                        selectionId);

                        response.setOptionNumber(
                                        optionNumber);

                        String json = objectMapper.writeValueAsString(
                                        response);

                        TemporaryItinerary temporary = TemporaryItinerary.builder()
                                        .userId(
                                                        userId)
                                        .selectionId(
                                                        selectionId)
                                        .optionNumber(
                                                        optionNumber)
                                        .itineraryData(
                                                        json)
                                        .createdAt(
                                                        now)
                                        .expiresAt(
                                                        expiresAt)
                                        .build();

                        temporaryItineraryRepository.save(
                                        temporary);

                        log.info(
                                        "Temporary itinerary saved. userId={}, selectionId={}, option={}",
                                        userId,
                                        selectionId,
                                        optionNumber);

                } catch (JsonProcessingException e) {

                        log.error(
                                        "Failed to serialize temporary itinerary",
                                        e);

                        throw new IllegalStateException(
                                        "Unable to store generated itinerary.",
                                        e);
                }
        }

        // ==========================================================
        // SELECT GENERATED ITINERARY
        // ==========================================================

        @Override
        @Transactional
        public ItineraryResponse selectGeneratedItinerary(
                        @NonNull Long userId,
                        @NonNull String selectionId) {

                log.info(
                                "Selecting generated itinerary. userId={}, selectionId={}",
                                userId,
                                selectionId);

                // ======================================================
                // STEP 1: USER
                // ======================================================

                User user = getUser(userId);

                // ======================================================
                // STEP 2: FIND TEMPORARY RECORD
                // ======================================================

                TemporaryItinerary temporary = temporaryItineraryRepository
                                .findBySelectionIdAndUserId(
                                                selectionId,
                                                userId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Generated itinerary option not found or expired."));

                // ======================================================
                // STEP 3: EXPIRATION
                // ======================================================

                if (temporary.getExpiresAt()
                                .isBefore(LocalDateTime.now())) {

                        temporaryItineraryRepository.delete(
                                        temporary);

                        throw new ResourceNotFoundException(
                                        "Generated itinerary option has expired.");
                }

                // ======================================================
                // STEP 4: JSON -> RESPONSE
                // ======================================================

                ItineraryResponse generatedResponse;

                try {

                        generatedResponse = objectMapper.readValue(
                                        temporary.getItineraryData(),
                                        ItineraryResponse.class);

                } catch (JsonProcessingException e) {

                        log.error(
                                        "Unable to deserialize temporary itinerary. selectionId={}",
                                        selectionId,
                                        e);

                        throw new IllegalStateException(
                                        "Unable to read generated itinerary.",
                                        e);
                }

                // ======================================================
                // STEP 5: CREATE COMPLETELY FRESH ITINERARY
                // ======================================================

                Itinerary itinerary = buildFreshItineraryForPersistence(
                                generatedResponse,
                                user);

                LocalDateTime now = LocalDateTime.now();

                itinerary.setCreatedAt(now);
                itinerary.setUpdatedAt(now);

                // ======================================================
                // STEP 6:
                // REMOVE CHILDREN BEFORE SAVING PARENT
                // ======================================================
                //
                // This is the important fix.
                //
                // Even if Itinerary has CascadeType.ALL,
                // Hibernate won't save the days here.
                //
                // ======================================================

                List<ItineraryDay> days = itinerary.getItineraryDays();

                itinerary.setItineraryDays(
                                days);

                // ======================================================
                // STEP 7: SAVE ONLY ITINERARY
                // ======================================================

                Itinerary savedItinerary = itineraryRepository.saveAndFlush(
                                itinerary);

                log.info(
                                "Main itinerary saved successfully. id={}",
                                savedItinerary.getId());

                // ======================================================
                // STEP 8: SAVE DAYS
                // ======================================================

                List<ItineraryDay> savedDays = new ArrayList<>();

                if (days != null &&
                                !days.isEmpty()) {

                        List<ItineraryDay> freshDays = new ArrayList<>();

                        for (ItineraryDay oldDay : days) {

                                // ==================================================
                                // IMPORTANT:
                                // Create a NEW entity.
                                // Never reuse old entity.
                                // ==================================================

                                ItineraryDay newDay = ItineraryDay.builder()
                                                .itinerary(
                                                                savedItinerary)
                                                .dayNumber(
                                                                oldDay.getDayNumber())
                                                .travelDate(
                                                                oldDay.getTravelDate())
                                                .title(
                                                                oldDay.getTitle())
                                                .notes(
                                                                oldDay.getNotes())
                                                .itineraryPlaces(
                                                                new ArrayList<>())
                                                .build();

                                freshDays.add(
                                                newDay);
                        }

                        savedDays = itineraryDayRepository.saveAllAndFlush(
                                        freshDays);
                }

                log.info(
                                "{} itinerary days saved.",
                                savedDays.size());

                // ======================================================
                // STEP 9: SAVE PLACES
                // ======================================================

                List<ItineraryPlace> freshPlaces = new ArrayList<>();

                for (int i = 0; i < savedDays.size(); i++) {

                        ItineraryDay savedDay = savedDays.get(i);

                        ItineraryDay oldDay = days.get(i);

                        if (oldDay.getItineraryPlaces() == null ||
                                        oldDay.getItineraryPlaces().isEmpty()) {

                                continue;
                        }

                        for (ItineraryPlace oldPlace : oldDay.getItineraryPlaces()) {

                                ItineraryPlace newPlace = ItineraryPlace.builder()
                                                .itineraryDay(
                                                                savedDay)
                                                .placeType(
                                                                oldPlace.getPlaceType())
                                                .referenceId(
                                                                oldPlace.getReferenceId())
                                                .visitOrder(
                                                                oldPlace.getVisitOrder())
                                                .plannedStartTime(
                                                                oldPlace.getPlannedStartTime())
                                                .plannedEndTime(
                                                                oldPlace.getPlannedEndTime())
                                                .estimatedCost(
                                                                oldPlace.getEstimatedCost())
                                                .travelTimeMinutes(
                                                                oldPlace.getTravelTimeMinutes())
                                                .notes(
                                                                oldPlace.getNotes())
                                                .completed(
                                                                Boolean.TRUE.equals(
                                                                                oldPlace.getCompleted()))
                                                .build();

                                freshPlaces.add(
                                                newPlace);
                        }
                }

                // ======================================================
                // STEP 10: SAVE PLACES
                // ======================================================

                if (!freshPlaces.isEmpty()) {

                        itineraryPlaceRepository.saveAllAndFlush(
                                        freshPlaces);
                }

                log.info(
                                "{} itinerary places saved.",
                                freshPlaces.size());

                // ======================================================
                // STEP 11:
                // RELOAD ACTUAL DB ENTITY
                // ======================================================

                Itinerary finalItinerary = itineraryRepository
                                .findById(
                                                savedItinerary.getId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Saved itinerary could not be found."));

                // ======================================================
                // STEP 12:
                // DELETE TEMPORARY RECORD
                // ======================================================

                temporaryItineraryRepository.delete(
                                temporary);

                temporaryItineraryRepository.flush();

                // ======================================================
                // STEP 13: RETURN REAL DB DATA
                // ======================================================

                log.info(
                                "Generated itinerary selected successfully. " +
                                                "userId={}, selectionId={}, itineraryId={}",
                                userId,
                                selectionId,
                                finalItinerary.getId());

                return itineraryMapper.toResponse(
                                finalItinerary);
        }

        private Itinerary buildFreshItineraryForPersistence(
                        ItineraryResponse response,
                        User user) {

                Location location = getLocation(
                                response.getLocationId());

                Itinerary itinerary = Itinerary.builder()
                                .user(user)
                                .location(location)
                                .title(response.getTitle())
                                .description(response.getDescription())
                                .totalDays(response.getTotalDays())
                                .totalBudget(response.getTotalBudget())
                                .estimatedCost(
                                                response.getEstimatedCost() != null
                                                                ? response.getEstimatedCost()
                                                                : BigDecimal.ZERO)
                                .remainingBudget(
                                                response.getRemainingBudget() != null
                                                                ? response.getRemainingBudget()
                                                                : BigDecimal.ZERO)
                                .travelType(response.getTravelType())
                                .itineraryStatus(response.getItineraryStatus())
                                .startDate(response.getStartDate())
                                .endDate(response.getEndDate())
                                .itineraryDays(new ArrayList<>())
                                .build();

                // ======================================================
                // BUILD DAYS
                // ======================================================

                if (response.getItineraryDays() == null) {
                        return itinerary;
                }

                for (var dayResponse : response.getItineraryDays()) {

                        ItineraryDay day = ItineraryDay.builder()
                                        .itinerary(itinerary)
                                        .dayNumber(
                                                        dayResponse.getDayNumber())
                                        .travelDate(
                                                        dayResponse.getTravelDate())
                                        .title(
                                                        dayResponse.getTitle())
                                        .notes(
                                                        dayResponse.getNotes())
                                        .itineraryPlaces(
                                                        new ArrayList<>())
                                        .build();

                        // ==================================================
                        // BUILD PLACES
                        // ==================================================

                        if (dayResponse.getItineraryPlaces() != null) {

                                for (var placeResponse : dayResponse.getItineraryPlaces()) {

                                        ItineraryPlace place = ItineraryPlace.builder()
                                                        .itineraryDay(day)
                                                        .placeType(
                                                                        placeResponse.getPlaceType())
                                                        .referenceId(
                                                                        placeResponse.getReferenceId())
                                                        .visitOrder(
                                                                        placeResponse.getVisitOrder())
                                                        .plannedStartTime(
                                                                        placeResponse.getPlannedStartTime())
                                                        .plannedEndTime(
                                                                        placeResponse.getPlannedEndTime())
                                                        .estimatedCost(
                                                                        placeResponse.getEstimatedCost())
                                                        .travelTimeMinutes(
                                                                        placeResponse
                                                                                        .getTravelTimeMinutes())
                                                        .notes(
                                                                        placeResponse.getNotes())
                                                        .completed(
                                                                        Boolean.TRUE.equals(
                                                                                        placeResponse.getCompleted()))
                                                        .build();

                                        day.addPlace(place);
                                }
                        }

                        itinerary.getItineraryDays()
                                        .add(day);
                }

                return itinerary;
        }

        // ==========================================================
        // MARK PLACE COMPLETED
        // ==========================================================

        @Override
        public ItineraryResponse markPlaceCompleted(
                        @NonNull Long itineraryPlaceId) {

                log.info(
                                "Marking itinerary place completed: {}",
                                itineraryPlaceId);

                // ======================================================
                // FETCH PLACE
                // ======================================================

                ItineraryPlace itineraryPlace = itineraryPlaceRepository
                                .findById(
                                                itineraryPlaceId)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "Itinerary place not found with id: "
                                                                                + itineraryPlaceId));

                // ======================================================
                // FETCH DAY
                // ======================================================

                ItineraryDay day = itineraryPlace.getItineraryDay();

                if (day == null ||
                                day.getItinerary() == null) {

                        throw new ResourceNotFoundException(
                                        "Itinerary information not found.");
                }

                Itinerary itinerary = day.getItinerary();

                // ======================================================
                // SECURITY
                // ======================================================

                System.out.println("Printing the itinerary object: " + itinerary.getId());
                // validateOwnership(
                // itinerary);

                // ======================================================
                // MARK COMPLETED
                // ======================================================

                itineraryPlace.setCompleted(
                                true);

                itineraryPlaceRepository.save(
                                itineraryPlace);

                // ======================================================
                // UPDATE PROGRESS
                // ======================================================

                updateItineraryProgress(
                                itinerary);

                itinerary.setUpdatedAt(
                                LocalDateTime.now());

                itineraryRepository.save(
                                itinerary);

                return itineraryMapper.toResponse(
                                itinerary);
        }

        // ==========================================================
        // UPDATE ITINERARY PROGRESS
        // ==========================================================

        private void updateItineraryProgress(
                        Itinerary itinerary) {

                List<ItineraryDay> days = itineraryDayRepository
                                .findByItineraryIdOrderByDayNumber(
                                                itinerary.getId());

                if (days == null ||
                                days.isEmpty()) {

                        return;
                }

                List<ItineraryPlace> places = days.stream()
                                .filter(Objects::nonNull)
                                .flatMap(
                                                day -> {

                                                        if (day.getItineraryPlaces() == null) {
                                                                return Stream.empty();
                                                        }

                                                        return day.getItineraryPlaces()
                                                                        .stream();
                                                })
                                .toList();

                if (places.isEmpty()) {
                        return;
                }

                long completedPlaces = places.stream()
                                .filter(
                                                place -> Boolean.TRUE.equals(
                                                                place.getCompleted()))
                                .count();

                // ======================================================
                // ALL COMPLETED
                // ======================================================

                if (completedPlaces == places.size()) {

                        itinerary.setItineraryStatus(
                                        ItineraryStatus.COMPLETED);

                        return;
                }

                // ======================================================
                // SOME COMPLETED
                // ======================================================

                if (completedPlaces > 0) {

                        itinerary.setItineraryStatus(
                                        ItineraryStatus.PLANNED);

                        return;
                }

                // ======================================================
                // NONE COMPLETED
                // ======================================================

                updateStatusAutomatically(
                                itinerary);
        }

        // ==========================================================
        // CREATE DEFAULT DAYS IN MEMORY
        // ==========================================================

        private void createDefaultDaysInMemory(
                        Itinerary itinerary,
                        LocalDateTime generatedAt,
                        int optionNumber) {

                List<ItineraryDay> days = new ArrayList<>();

                LocalDate startDate = itinerary.getStartDate();

                if (startDate == null) {
                        startDate = LocalDate.now();
                }

                for (int dayNumber = 1; dayNumber <= itinerary.getTotalDays(); dayNumber++) {

                        LocalDate travelDate = startDate.plusDays(
                                        dayNumber - 1);

                        ItineraryDay day = ItineraryDay.builder()
                                        .itinerary(
                                                        itinerary)
                                        .dayNumber(
                                                        dayNumber)
                                        .title(
                                                        generateDayTitle(
                                                                        dayNumber))
                                        .notes(
                                                        "Trip activities for Day "
                                                                        + dayNumber)
                                        .travelDate(
                                                        travelDate)
                                        .itineraryPlaces(
                                                        new ArrayList<>())
                                        .build();

                        // --------------------------------------------------
                        // Created timestamp
                        // --------------------------------------------------

                        day.setCreatedAt(
                                        generatedAt);

                        days.add(
                                        day);
                }

                itinerary.setItineraryDays(
                                days);
        }

        // ==========================================================
        // ASSIGN TOURIST PLACES
        // ==========================================================

        private void assignPlacesToDaysInMemory(
                        Itinerary itinerary,
                        List<TouristPlace> touristPlaces,
                        int optionNumber) {

                if (touristPlaces == null ||
                                touristPlaces.isEmpty()) {

                        log.warn(
                                        "No tourist places available for option {}",
                                        optionNumber);

                        return;
                }

                List<ItineraryDay> days = itinerary.getItineraryDays();

                if (days == null ||
                                days.isEmpty()) {

                        return;
                }

                int totalDays = days.size();

                int totalPlaces = touristPlaces.size();

                int placesPerDay = Math.max(
                                1,
                                (int) Math.ceil(
                                                (double) totalPlaces
                                                                / totalDays));

                int currentPlaceIndex = 0;

                for (ItineraryDay day : days) {

                        LocalTime currentTime = getDefaultStartTime();

                        int visitOrder = 1;

                        int placesAssigned = 0;

                        while (currentPlaceIndex < totalPlaces
                                        &&
                                        placesAssigned < placesPerDay
                                        &&
                                        placesAssigned < MAX_PLACES_PER_DAY) {

                                TouristPlace touristPlace = touristPlaces.get(
                                                currentPlaceIndex++);

                                ItineraryPlace itineraryPlace = buildItineraryPlace(
                                                day,
                                                touristPlace,
                                                visitOrder++,
                                                currentTime,
                                                optionNumber,
                                                placesAssigned + 1);

                                day.addPlace(
                                                itineraryPlace);

                                currentTime = itineraryPlace
                                                .getPlannedEndTime()
                                                .plusMinutes(
                                                                DEFAULT_TRAVEL_MINUTES);

                                placesAssigned++;
                        }
                }
        }

        // ==========================================================
        // BUILD TOURIST ITINERARY PLACE
        // ==========================================================

        private ItineraryPlace buildItineraryPlace(
                        ItineraryDay day,
                        TouristPlace place,
                        int visitOrder,
                        LocalTime startTime,
                        int optionNumber,
                        int placeIndex) {

                int visitMinutes = place.getEstimatedVisitTimeMinutes() != null
                                ? place.getEstimatedVisitTimeMinutes()
                                : DEFAULT_VISIT_MINUTES;

                LocalTime endTime = startTime.plusMinutes(
                                visitMinutes);

                ItineraryPlace itineraryPlace = ItineraryPlace.builder()
                                .itineraryDay(
                                                day)
                                .placeType(
                                                PlaceType.TOURIST_PLACE)
                                .referenceId(
                                                place.getId())
                                .visitOrder(
                                                visitOrder)
                                .plannedStartTime(
                                                startTime)
                                .plannedEndTime(
                                                endTime)
                                .estimatedCost(
                                                place.getPrice() != null
                                                                ? place.getPrice()
                                                                : BigDecimal.ZERO)
                                .travelTimeMinutes(
                                                DEFAULT_TRAVEL_MINUTES)
                                .completed(
                                                false)
                                .notes(
                                                place.getPlaceName())
                                .build();

                return itineraryPlace;
        }

        // ==========================================================
        // ASSIGN HOTELS
        // ==========================================================

        private void assignHotelsToItineraryInMemory(
                        Itinerary itinerary,
                        List<Hotel> hotels,
                        int optionNumber) {

                if (hotels == null ||
                                hotels.isEmpty()) {

                        return;
                }

                List<ItineraryDay> days = itinerary.getItineraryDays();

                if (days == null ||
                                days.isEmpty()) {

                        return;
                }

                int hotelIndex = 0;

                for (ItineraryDay day : days) {

                        if (hotelIndex >= hotels.size()) {
                                break;
                        }

                        Hotel hotel = hotels.get(
                                        hotelIndex++);

                        ItineraryPlace hotelPlace = ItineraryPlace.builder()
                                        .itineraryDay(
                                                        day)
                                        .placeType(
                                                        PlaceType.HOTEL)
                                        .referenceId(
                                                        hotel.getId())
                                        .visitOrder(
                                                        99)
                                        .plannedStartTime(
                                                        LocalTime.of(
                                                                        20,
                                                                        0))
                                        .plannedEndTime(
                                                        LocalTime.of(
                                                                        21,
                                                                        0))
                                        .estimatedCost(
                                                        hotel.getPricePerNight() != null
                                                                        ? hotel.getPricePerNight()
                                                                        : BigDecimal.ZERO)
                                        .travelTimeMinutes(
                                                        0)
                                        .completed(
                                                        false)
                                        .notes(
                                                        "Hotel: "
                                                                        + hotel.getHotelName())
                                        .build();

                        day.addPlace(
                                        hotelPlace);
                }
        }

        // ==========================================================
        // ASSIGN RESTAURANTS
        // ==========================================================

        private void assignRestaurantsToItineraryInMemory(
                        Itinerary itinerary,
                        List<Restaurant> restaurants,
                        int optionNumber) {

                if (restaurants == null ||
                                restaurants.isEmpty()) {

                        return;
                }

                List<ItineraryDay> days = itinerary.getItineraryDays();

                if (days == null ||
                                days.isEmpty()) {

                        return;
                }

                int restaurantIndex = 0;

                for (ItineraryDay day : days) {

                        if (restaurantIndex >= restaurants.size()) {
                                break;
                        }

                        Restaurant restaurant = restaurants.get(
                                        restaurantIndex++);

                        ItineraryPlace restaurantPlace = ItineraryPlace.builder()
                                        .itineraryDay(
                                                        day)
                                        .placeType(
                                                        PlaceType.RESTAURANT)
                                        .referenceId(
                                                        restaurant.getId())
                                        .visitOrder(
                                                        98)
                                        .plannedStartTime(
                                                        LocalTime.of(
                                                                        13,
                                                                        0))
                                        .plannedEndTime(
                                                        LocalTime.of(
                                                                        14,
                                                                        0))
                                        .estimatedCost(
                                                        restaurant
                                                                        .getAverageCostPerPerson() != null
                                                                                        ? restaurant
                                                                                                        .getAverageCostPerPerson()
                                                                                        : BigDecimal.ZERO)
                                        .travelTimeMinutes(
                                                        0)
                                        .completed(
                                                        false)
                                        .notes(
                                                        "Restaurant: "
                                                                        + restaurant
                                                                                        .getRestaurantName())
                                        .build();

                        day.addPlace(
                                        restaurantPlace);
                }
        }

        // ==========================================================
        // HOTEL SELECTION
        // ==========================================================

        private List<Hotel> assignHotelsForOption(
                        Itinerary itinerary,
                        List<Hotel> hotels,
                        int optionNumber) {

                if (hotels == null ||
                                hotels.isEmpty()) {

                        return new ArrayList<>();
                }

                BigDecimal totalBudget = itinerary.getTotalBudget();

                BigDecimal hotelBudget = totalBudget.multiply(
                                HOTEL_BUDGET_PERCENTAGE);

                BigDecimal perDayBudget = hotelBudget.divide(
                                BigDecimal.valueOf(
                                                itinerary.getTotalDays()),
                                2,
                                java.math.RoundingMode.HALF_UP);

                // ======================================================
                // STRICT BUDGET FILTER
                // ======================================================

                List<Hotel> filtered = hotels.stream()
                                .filter(
                                                hotel -> hotel.getPricePerNight() != null)
                                .filter(
                                                hotel -> hotel.getPricePerNight()
                                                                .compareTo(
                                                                                perDayBudget) <= 0)
                                .toList();

                // ======================================================
                // FALLBACK
                // ======================================================
                //
                // Low budget ke wajah se zero hotel nahi.
                // Cheapest available hotels use karo.
                //
                // ======================================================

                if (filtered.isEmpty()) {

                        log.warn(
                                        "No hotel within budget {}. Using cheapest hotels.",
                                        perDayBudget);

                        filtered = hotels.stream()
                                        .filter(
                                                        hotel -> hotel.getPricePerNight() != null)
                                        .sorted(
                                                        Comparator.comparing(
                                                                        Hotel::getPricePerNight))
                                        .toList();
                }

                // ======================================================
                // OPTION 1 = BEST RATING
                // ======================================================

                if (optionNumber == 1) {

                        return filtered.stream()
                                        .sorted(
                                                        Comparator.comparing(
                                                                        Hotel::getAverageRating,
                                                                        Comparator.nullsLast(
                                                                                        Comparator.reverseOrder())))
                                        .limit(3)
                                        .toList();
                }

                // ======================================================
                // OPTION 2 = CHEAPEST
                // ======================================================

                if (optionNumber == 2) {

                        return filtered.stream()
                                        .sorted(
                                                        Comparator.comparing(
                                                                        Hotel::getPricePerNight))
                                        .limit(3)
                                        .toList();
                }

                // ======================================================
                // OPTION 3 = PREMIUM
                // ======================================================

                return filtered.stream()
                                .sorted(
                                                Comparator.comparing(
                                                                Hotel::getPricePerNight,
                                                                Comparator.reverseOrder()))
                                .limit(3)
                                .toList();
        }

        // ==========================================================
        // RESTAURANT SELECTION
        // ==========================================================

        private List<Restaurant> assignRestaurantsForOption(
                        Itinerary itinerary,
                        List<Restaurant> restaurants,
                        int optionNumber) {

                if (restaurants == null ||
                                restaurants.isEmpty()) {

                        return new ArrayList<>();
                }

                BigDecimal totalBudget = itinerary.getTotalBudget();

                BigDecimal restaurantBudget = totalBudget.multiply(
                                RESTAURANT_BUDGET_PERCENTAGE);

                BigDecimal perMealBudget = restaurantBudget.divide(
                                BigDecimal.valueOf(
                                                itinerary.getTotalDays() * 2L),
                                2,
                                java.math.RoundingMode.HALF_UP);

                // ======================================================
                // STRICT FILTER
                // ======================================================

                List<Restaurant> filtered = restaurants.stream()
                                .filter(
                                                restaurant -> restaurant
                                                                .getAverageCostPerPerson() != null)
                                .filter(
                                                restaurant -> restaurant
                                                                .getAverageCostPerPerson()
                                                                .compareTo(
                                                                                perMealBudget) <= 0)
                                .toList();

                // ======================================================
                // FALLBACK
                // ======================================================

                if (filtered.isEmpty()) {

                        log.warn(
                                        "No restaurant within budget {}. "
                                                        + "Using cheapest restaurants.",
                                        perMealBudget);

                        filtered = restaurants.stream()
                                        .filter(
                                                        restaurant -> restaurant
                                                                        .getAverageCostPerPerson() != null)
                                        .sorted(
                                                        Comparator.comparing(
                                                                        Restaurant::getAverageCostPerPerson))
                                        .toList();
                }

                // ======================================================
                // OPTION 1 = BEST RATING
                // ======================================================

                if (optionNumber == 1) {

                        return filtered.stream()
                                        .sorted(
                                                        Comparator.comparing(
                                                                        Restaurant::getAverageRating,
                                                                        Comparator.nullsLast(
                                                                                        Comparator.reverseOrder())))
                                        .limit(3)
                                        .toList();
                }

                // ======================================================
                // OPTION 2 = CHEAPEST
                // ======================================================

                if (optionNumber == 2) {

                        return filtered.stream()
                                        .sorted(
                                                        Comparator.comparing(
                                                                        Restaurant::getAverageCostPerPerson))
                                        .limit(3)
                                        .toList();
                }

                // ======================================================
                // OPTION 3 = PREMIUM
                // ======================================================

                return filtered.stream()
                                .sorted(
                                                Comparator.comparing(
                                                                Restaurant::getAverageCostPerPerson,
                                                                Comparator.reverseOrder()))
                                .limit(3)
                                .toList();
        }

        // ==========================================================
        // SORT TOURIST PLACES FOR OPTION
        // ==========================================================

        private List<TouristPlace> sortTouristPlacesForOption(
                        List<TouristPlace> places,
                        int optionNumber) {

                if (places == null ||
                                places.isEmpty()) {

                        return new ArrayList<>();
                }

                // ======================================================
                // OPTION 1 = RATING + POPULARITY
                // ======================================================

                if (optionNumber == 1) {

                        return places.stream()
                                        .sorted(
                                                        Comparator
                                                                        .comparing(
                                                                                        TouristPlace::getAverageRating,
                                                                                        Comparator.nullsLast(
                                                                                                        Comparator.reverseOrder()))
                                                                        .thenComparing(
                                                                                        TouristPlace::getPopularityScore,
                                                                                        Comparator.nullsLast(
                                                                                                        Comparator.reverseOrder())))
                                        .toList();
                }

                // ======================================================
                // OPTION 2 = POPULARITY
                // ======================================================

                if (optionNumber == 2) {

                        return places.stream()
                                        .sorted(
                                                        Comparator
                                                                        .comparing(
                                                                                        TouristPlace::getPopularityScore,
                                                                                        Comparator.nullsLast(
                                                                                                        Comparator.reverseOrder()))
                                                                        .thenComparing(
                                                                                        TouristPlace::getAverageRating,
                                                                                        Comparator.nullsLast(
                                                                                                        Comparator.reverseOrder())))
                                        .toList();
                }

                // ======================================================
                // OPTION 3 = BUDGET FRIENDLY
                // ======================================================

                return places.stream()
                                .sorted(
                                                Comparator
                                                                .comparing(
                                                                                TouristPlace::getPrice,
                                                                                Comparator.nullsLast(
                                                                                                Comparator.naturalOrder()))
                                                                .thenComparing(
                                                                                TouristPlace::getAverageRating,
                                                                                Comparator.nullsLast(
                                                                                                Comparator.reverseOrder())))
                                .toList();
        }

        // ==========================================================
        // FETCH TOURIST PLACES
        // ==========================================================

        private List<TouristPlace> fetchTouristPlaces(
                        Location location) {

                List<TouristPlace> places = touristPlaceRepository
                                .findByLocationIdAndActiveTrue(
                                                location.getId());

                if (places == null ||
                                places.isEmpty()) {

                        log.warn(
                                        "No tourist places found for location={}",
                                        location.getCityName());

                        return new ArrayList<>();
                }

                return places;
        }

        // ==========================================================
        // FILTER TOURIST PLACES
        // ==========================================================

        private List<TouristPlace> filterTouristPlaces(
                        List<TouristPlace> places,
                        Itinerary itinerary) {

                if (places == null ||
                                places.isEmpty()) {

                        return new ArrayList<>();
                }

                BigDecimal totalBudget = itinerary.getTotalBudget();

                BigDecimal placesBudget = totalBudget.multiply(
                                PLACES_BUDGET_PERCENTAGE);

                // ======================================================
                // ACTIVE
                // ======================================================

                List<TouristPlace> activePlaces = places.stream()
                                .filter(
                                                place -> Boolean.TRUE.equals(
                                                                place.getActive()))
                                .toList();

                // ======================================================
                // STRICT BUDGET FILTER
                // ======================================================

                List<TouristPlace> budgetPlaces = activePlaces.stream()
                                .filter(
                                                place -> place.getPrice() == null
                                                                ||
                                                                place.getPrice()
                                                                                .compareTo(
                                                                                                placesBudget) <= 0)
                                .toList();

                // ======================================================
                // TRAVEL TYPE
                // ======================================================

                List<TouristPlace> travelTypePlaces = budgetPlaces.stream()
                                .filter(
                                                place -> filterByTravelType(
                                                                place,
                                                                itinerary))
                                .toList();

                // ======================================================
                // SEASON
                // ======================================================

                List<TouristPlace> seasonPlaces = travelTypePlaces.stream()
                                .filter(
                                                place -> filterByBestSeason(
                                                                place,
                                                                itinerary))
                                .toList();

                // ======================================================
                // IF STRICT FILTER RETURNS RESULTS
                // ======================================================

                if (!seasonPlaces.isEmpty()) {

                        int maxPlaces = MAX_PLACES_PER_DAY
                                        * itinerary.getTotalDays();

                        return seasonPlaces.stream()
                                        .limit(maxPlaces)
                                        .toList();
                }

                // ======================================================
                // FALLBACK
                // ======================================================
                //
                // Budget too low hone par itinerary empty nahi hogi.
                //
                // Budget filter remove kar diya.
                //
                // ======================================================

                log.warn(
                                "No tourist place matched strict budget {}. "
                                                + "Applying fallback filtering.",
                                placesBudget);

                List<TouristPlace> fallback = activePlaces.stream()
                                .filter(
                                                place -> filterByTravelType(
                                                                place,
                                                                itinerary))
                                .filter(
                                                place -> filterByBestSeason(
                                                                place,
                                                                itinerary))
                                .toList();

                // ======================================================
                // SECOND FALLBACK
                // ======================================================
                //
                // Agar season ke wajah se bhi zero hain,
                // active places use karo.
                //
                // ======================================================

                if (fallback.isEmpty()) {

                        fallback = new ArrayList<>(
                                        activePlaces);
                }

                int maxPlaces = MAX_PLACES_PER_DAY
                                * itinerary.getTotalDays();

                return fallback.stream()
                                .limit(maxPlaces)
                                .toList();
        }

        // ==========================================================
        // TRAVEL TYPE FILTER
        // ==========================================================

        private boolean filterByTravelType(
                        TouristPlace place,
                        Itinerary itinerary) {

                // Currently all active tourist places are accepted.
                //
                // Future:
                // if TouristPlace contains travelTypes,
                // compare it with itinerary.getTravelType().

                return true;
        }

        // ==========================================================
        // BEST SEASON FILTER
        // ==========================================================

        private boolean filterByBestSeason(
                        TouristPlace place,
                        Itinerary itinerary) {

                String bestVisitMonths = place.getBestVisitMonths();

                if (bestVisitMonths == null ||
                                bestVisitMonths.trim().isEmpty()) {

                        return true;
                }

                LocalDate startDate = itinerary.getStartDate();

                if (startDate == null) {
                        startDate = LocalDate.now();
                }

                Month tripMonth = startDate.getMonth();

                String tripMonthName = tripMonth.name();

                String value = bestVisitMonths.trim();

                // ======================================================
                // COMMA SEPARATED
                // ======================================================

                if (value.contains(",")) {

                        for (String month : value.split(",")) {

                                if (month.trim()
                                                .equalsIgnoreCase(
                                                                tripMonthName)) {

                                        return true;
                                }
                        }

                        return false;
                }

                // ======================================================
                // MONTH RANGE
                // ======================================================

                if (value.toLowerCase()
                                .contains(" to ")) {

                        String[] parts = value.split(
                                        "(?i)\\s+to\\s+");

                        if (parts.length == 2) {

                                try {

                                        int startMonth = Month.valueOf(
                                                        parts[0]
                                                                        .trim()
                                                                        .toUpperCase())
                                                        .getValue();

                                        int endMonth = Month.valueOf(
                                                        parts[1]
                                                                        .trim()
                                                                        .toUpperCase())
                                                        .getValue();

                                        int tripMonthNumber = tripMonth.getValue();

                                        if (startMonth <= endMonth) {

                                                return tripMonthNumber >= startMonth
                                                                &&
                                                                tripMonthNumber <= endMonth;
                                        }

                                        // Cross-year range
                                        return tripMonthNumber >= startMonth
                                                        ||
                                                        tripMonthNumber <= endMonth;

                                } catch (IllegalArgumentException exception) {

                                        log.warn(
                                                        "Invalid best visit month value: {}",
                                                        value);

                                        return true;
                                }
                        }
                }

                // ======================================================
                // SINGLE MONTH
                // ======================================================

                return value.equalsIgnoreCase(
                                tripMonthName);
        }

        // ==========================================================
        // DEFAULT START TIME
        // ==========================================================

        private LocalTime getDefaultStartTime() {
                return LocalTime.of(
                                9,
                                0);
        }

        // ==========================================================
        // DAY TITLE
        // ==========================================================

        private String generateDayTitle(
                        int dayNumber) {

                return switch (dayNumber) {

                        case 1 ->
                                "Arrival & Local Sightseeing";

                        case 2 ->
                                "Explore Famous Attractions";

                        case 3 ->
                                "Adventure & Outdoor Activities";

                        case 4 ->
                                "Cultural & Heritage Tour";

                        case 5 ->
                                "Shopping & Food Experience";

                        default ->
                                "Day "
                                                + dayNumber
                                                + " - Exploration";
                };
        }

        // ==========================================================
        // BUILD ITINERARY
        // ==========================================================

        private void buildItinerary(
                        Itinerary itinerary,
                        ItineraryRequest request,
                        User user,
                        Location location) {

                itinerary.setUser(
                                user);

                itinerary.setLocation(
                                location);

                itinerary.setTitle(
                                request.getTitle().trim());

                itinerary.setDescription(
                                request.getDescription());

                itinerary.setTravelType(
                                request.getTravelType());

                itinerary.setTotalDays(
                                request.getTotalDays());

                itinerary.setTotalBudget(
                                request.getTotalBudget());

                // IMPORTANT:
                // Request estimatedCost is ignored during generation.
                // Backend calculates actual estimated cost.
                itinerary.setEstimatedCost(
                                BigDecimal.ZERO);

                itinerary.setRemainingBudget(
                                request.getTotalBudget());

                itinerary.setItineraryStatus(
                                ItineraryStatus.GENERATED);

                itinerary.setStartDate(
                                request.getStartDate());

                itinerary.setEndDate(
                                request.getEndDate());

                itinerary.setItineraryDays(
                                new ArrayList<>());
        }

        // ==========================================================
        // COST CALCULATION
        // ==========================================================

        private BigDecimal calculateTotalEstimatedCost(
                        Itinerary itinerary,
                        List<Hotel> hotels,
                        List<Restaurant> restaurants,
                        List<TouristPlace> touristPlaces) {

                BigDecimal totalCost = BigDecimal.ZERO;

                // ======================================================
                // HOTEL
                // ======================================================

                if (hotels != null &&
                                !hotels.isEmpty()) {

                        Hotel hotel = hotels.get(0);

                        if (hotel.getPricePerNight() != null) {

                                BigDecimal hotelCost = hotel.getPricePerNight()
                                                .multiply(
                                                                BigDecimal.valueOf(
                                                                                itinerary
                                                                                                .getTotalDays()));

                                totalCost = totalCost.add(
                                                hotelCost);
                        }
                }

                // ======================================================
                // RESTAURANT
                // ======================================================

                if (restaurants != null &&
                                !restaurants.isEmpty()) {

                        Restaurant restaurant = restaurants.get(0);

                        if (restaurant
                                        .getAverageCostPerPerson() != null) {

                                BigDecimal foodCost = restaurant
                                                .getAverageCostPerPerson()
                                                .multiply(
                                                                BigDecimal.valueOf(
                                                                                itinerary
                                                                                                .getTotalDays()
                                                                                                * 2L));

                                totalCost = totalCost.add(
                                                foodCost);
                        }
                }

                // ======================================================
                // TOURIST PLACES
                // ======================================================

                if (touristPlaces != null &&
                                !touristPlaces.isEmpty()) {

                        BigDecimal placeCost = touristPlaces.stream()
                                        .map(
                                                        place -> place.getPrice() != null
                                                                        ? place.getPrice()
                                                                        : BigDecimal.ZERO)
                                        .reduce(
                                                        BigDecimal.ZERO,
                                                        BigDecimal::add);

                        totalCost = totalCost.add(
                                        placeCost);
                }

                return totalCost;
        }

        // ==========================================================
        // REMAINING BUDGET
        // ==========================================================

        private BigDecimal calculateRemainingBudget(
                        Itinerary itinerary) {

                if (itinerary.getTotalBudget() == null) {

                        return BigDecimal.ZERO;
                }

                BigDecimal estimatedCost = itinerary.getEstimatedCost() != null
                                ? itinerary.getEstimatedCost()
                                : BigDecimal.ZERO;

                return itinerary
                                .getTotalBudget()
                                .subtract(
                                                estimatedCost);
        }

        // ==========================================================
        // AUTOMATIC STATUS
        // ==========================================================

        private void updateStatusAutomatically(
                        Itinerary itinerary) {

                if (itinerary.getTotalBudget() == null ||
                                itinerary.getEstimatedCost() == null) {

                        itinerary.setItineraryStatus(
                                        ItineraryStatus.GENERATED);

                        return;
                }

                if (itinerary.getTotalBudget()
                                .compareTo(BigDecimal.ZERO) <= 0) {

                        itinerary.setItineraryStatus(
                                        ItineraryStatus.GENERATED);

                        return;
                }

                BigDecimal remainingBudget = calculateRemainingBudget(
                                itinerary);

                BigDecimal budgetRatio = remainingBudget.divide(
                                itinerary.getTotalBudget(),
                                4,
                                java.math.RoundingMode.HALF_UP);

                // ======================================================
                // MORE THAN 20% OVER BUDGET
                // ======================================================

                if (budgetRatio.compareTo(
                                BigDecimal.valueOf(-0.20)) < 0) {

                        itinerary.setItineraryStatus(
                                        ItineraryStatus.CONCERNED);

                        return;
                }

                // ======================================================
                // SLIGHTLY OVER BUDGET
                // ======================================================

                if (budgetRatio.compareTo(
                                BigDecimal.ZERO) < 0) {

                        itinerary.setItineraryStatus(
                                        ItineraryStatus.PLANNED);

                        return;
                }

                // ======================================================
                // WITHIN BUDGET
                // ======================================================

                itinerary.setItineraryStatus(
                                ItineraryStatus.GENERATED);
        }

        // ==========================================================
        // GET ITINERARY BY ID
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public ItineraryResponse getById(
                        @NonNull Long id) {

                Itinerary itinerary = getItinerary(id);

                validateOwnership(
                                itinerary);

                return itineraryMapper.toResponse(
                                itinerary);
        }

        // ==========================================================
        // GET MY ITINERARIES
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public List<ItineraryResponse> getMyItineraries() {

                Long userId = securityUtils.getCurrentUserId();

                return itineraryRepository
                                .findByUserIdOrderByCreatedAtDesc(
                                                userId)
                                .stream()
                                .map(
                                                itineraryMapper::toResponse)
                                .toList();
        }

        // ==========================================================
        // GET ITINERARIES BY USER ID
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public List<ItineraryResponse> getItineraryByUserId(
                        Long userId) {

                return itineraryRepository
                                .findByUserIdOrderByCreatedAtDesc(
                                                userId)
                                .stream()
                                .map(
                                                itineraryMapper::toResponse)
                                .toList();
        }

        // ==========================================================
        // USER SUMMARY
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public UserSummaryResponse getUserSummaryByUserId(
                        Long userId) {

                List<ItineraryResponse> itineraries = itineraryRepository
                                .findByUserIdOrderByCreatedAtDesc(
                                                userId)
                                .stream()
                                .map(
                                                itineraryMapper::toResponse)
                                .toList();

                return userSummaryMapper.toResponse(
                                itineraries);
        }

        // ==========================================================
        // DELETE
        // ==========================================================

        @Override
        public void delete(
                        @NonNull Long id) {

                Itinerary itinerary = getItinerary(id);

                validateOwnership(
                                itinerary);

                itineraryPlaceRepository
                                .deleteByItineraryDayItineraryId(
                                                id);

                itineraryDayRepository
                                .deleteByItineraryId(
                                                id);

                itineraryRepository.delete(
                                itinerary);

                log.info(
                                "Itinerary {} deleted successfully",
                                id);
        }

        // ==========================================================
        // VALIDATION
        // ==========================================================

        private void validateRequest(
                        ItineraryRequest request) {

                Objects.requireNonNull(
                                request,
                                "Itinerary request cannot be null.");

                if (request.getUserId() == null) {

                        throw new IllegalArgumentException(
                                        "User Id is required.");
                }

                if (request.getLocationId() == null) {

                        throw new IllegalArgumentException(
                                        "Location Id is required.");
                }

                if (request.getTitle() == null ||
                                request.getTitle().trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                        "Trip title is required.");
                }

                if (request.getTotalDays() == null ||
                                request.getTotalDays() <= 0) {

                        throw new IllegalArgumentException(
                                        "Total days must be greater than zero.");
                }

                if (request.getTotalDays() > MAX_DAYS) {

                        throw new IllegalArgumentException(
                                        "Maximum trip duration is "
                                                        + MAX_DAYS
                                                        + " days.");
                }

                if (request.getTravelType() == null) {

                        throw new IllegalArgumentException(
                                        "Travel type is required.");
                }

                if (request.getTotalBudget() == null) {

                        throw new IllegalArgumentException(
                                        "Total budget is required.");
                }

                if (request.getTotalBudget()
                                .compareTo(BigDecimal.ZERO) <= 0) {

                        throw new IllegalArgumentException(
                                        "Budget must be greater than zero.");
                }

                if (request.getEstimatedCost() != null &&
                                request.getEstimatedCost()
                                                .compareTo(BigDecimal.ZERO) < 0) {

                        throw new IllegalArgumentException(
                                        "Estimated cost cannot be negative.");
                }

                // ======================================================
                // DATE VALIDATION
                // ======================================================

                if (request.getStartDate() != null &&
                                request.getEndDate() != null &&
                                request.getEndDate()
                                                .isBefore(
                                                                request.getStartDate())) {

                        throw new IllegalArgumentException(
                                        "End date cannot be before start date.");
                }
        }

        // ==========================================================
        // OWNERSHIP
        // ==========================================================

        private void validateOwnership(
                        Itinerary itinerary) {

                System.out.println(
                                "Validating ownership for itinerary IDss: "
                                                + itinerary.getId()
                                                + ", User ID: "
                                                + itinerary.getUser().getId() + securityUtils.getCurrentUserId());
                Long currentUserId = securityUtils.getCurrentUserId();

                System.out.println(
                                "Current User ID: "
                                                + currentUserId
                                                + ", Itinerary User ID: "
                                                + itinerary.getUser().getId());
                if (!Objects.equals(
                                itinerary.getUser().getId(),
                                currentUserId)) {

                        throw new UnauthorizedException(
                                        "You are not authorized to access this itinerary.");
                }
        }

        // ==========================================================
        // GET USER
        // ==========================================================

        private User getUser(
                        @NonNull Long userId) {

                return userRepository
                                .findById(userId)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "User not found with id: "
                                                                                + userId));
        }

        // ==========================================================
        // GET LOCATION
        // ==========================================================

        private Location getLocation(
                        @NonNull Long locationId) {

                return locationRepository
                                .findById(locationId)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "Location not found with id: "
                                                                                + locationId));
        }

        // ==========================================================
        // GET ITINERARY
        // ==========================================================

        private Itinerary getItinerary(
                        @NonNull Long itineraryId) {

                return itineraryRepository
                                .findById(itineraryId)
                                .orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "Itinerary not found with id: "
                                                                                + itineraryId));
        }

        // ==========================================================
        // CALCULATE REMAINING BUDGET BY ID
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public BigDecimal calculateRemainingBudget(
                        @NonNull Long itineraryId) {

                Itinerary itinerary = getItinerary(
                                itineraryId);

                validateOwnership(
                                itinerary);

                return calculateRemainingBudget(
                                itinerary);
        }

        // ==========================================================
        // CHECK OWNER
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public boolean isItineraryOwner(
                        @NonNull Long itineraryId) {

                try {

                        Itinerary itinerary = getItinerary(
                                        itineraryId);

                        Long currentUserId = securityUtils.getCurrentUserId();

                        return Objects.equals(
                                        itinerary.getUser().getId(),
                                        currentUserId);

                } catch (Exception exception) {

                        return false;
                }
        }

        // ==========================================================
        // MANUAL STATUS UPDATE DISABLED
        // ==========================================================

        @Override
        public ItineraryResponse updateStatus(
                        @NonNull Long id,
                        ItineraryStatus status) {

                throw new UnsupportedOperationException(
                                "Manual itinerary status update is not allowed. "
                                                + "Status is managed automatically by backend.");
        }

        // ==========================================================
        // GET BY STATUS
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public List<ItineraryResponse> getItinerariesByStatus(
                        ItineraryStatus status) {

                Long userId = securityUtils.getCurrentUserId();

                return itineraryRepository
                                .findByUserIdAndItineraryStatus(
                                                userId,
                                                status)
                                .stream()
                                .map(
                                                itineraryMapper::toResponse)
                                .toList();
        }

        // ==========================================================
        // UPCOMING
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public List<ItineraryResponse> getUpcomingItineraries() {

                Long userId = securityUtils.getCurrentUserId();

                LocalDate today = LocalDate.now();

                return itineraryRepository
                                .findByUserIdAndStartDateAfterOrderByStartDateAsc(
                                                userId,
                                                today)
                                .stream()
                                .map(
                                                itineraryMapper::toResponse)
                                .toList();
        }

        // ==========================================================
        // WITHIN BUDGET
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public List<ItineraryResponse> getItinerariesWithinBudget(
                        BigDecimal minBudget,
                        BigDecimal maxBudget) {

                Long userId = securityUtils.getCurrentUserId();

                return itineraryRepository
                                .findByUserIdAndTotalBudgetBetween(
                                                userId,
                                                minBudget,
                                                maxBudget)
                                .stream()
                                .map(
                                                itineraryMapper::toResponse)
                                .toList();
        }

        // ==========================================================
        // SEARCH
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public List<ItineraryResponse> searchItineraries(
                        String keyword) {

                Long userId = securityUtils.getCurrentUserId();

                return itineraryRepository
                                .findByUserIdAndTitleContainingOrDescriptionContaining(
                                                userId,
                                                keyword,
                                                keyword)
                                .stream()
                                .map(
                                                itineraryMapper::toResponse)
                                .toList();
        }

        // ==========================================================
        // UPDATE DISABLED
        // ==========================================================
        //
        // User-facing itinerary update nahi hoga.
        //
        // Backend automatically updates:
        // - completed
        // - status
        // - updatedAt
        //
        // ==========================================================

        @Override
        public ItineraryResponse update(
                        @NonNull Long id,
                        @NonNull ItineraryRequest request) {

                throw new UnsupportedOperationException(
                                "Manual itinerary update is not allowed. "
                                                + "Itinerary is generated once and progress is "
                                                + "updated automatically by backend.");
        }
}