
// package com.example.tripItinerary.Service.impl;

// import java.math.BigDecimal;
// import java.math.RoundingMode;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.LocalTime;
// import java.time.Month;
// import java.util.ArrayList;
// import java.util.Comparator;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Objects;
// import java.util.Set;
// import java.lang.reflect.Method;
// import java.util.UUID;
// import java.util.stream.Stream;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.example.tripItinerary.DTO.projection.UserSummaryProjection;
// import com.example.tripItinerary.DTO.request.ItineraryRequest;
// import com.example.tripItinerary.DTO.response.BudgetSummaryResponse;
// import com.example.tripItinerary.DTO.response.ItineraryResponse;
// import com.example.tripItinerary.DTO.response.UpcomingTripResponse;
// import com.example.tripItinerary.DTO.response.UserSummaryResponse;
// import com.example.tripItinerary.Entity.Hotel;
// import com.example.tripItinerary.Entity.Itinerary;
// import com.example.tripItinerary.Entity.ItineraryDay;
// import com.example.tripItinerary.Entity.ItineraryPlace;
// import com.example.tripItinerary.Entity.Location;
// import com.example.tripItinerary.Entity.Restaurant;
// import com.example.tripItinerary.Entity.TemporaryItinerary;
// import com.example.tripItinerary.Entity.TouristPlace;
// import com.example.tripItinerary.Entity.User;
// import com.example.tripItinerary.Mapper.ItineraryMapper;
// import com.example.tripItinerary.Repo.HotelRepository;
// import com.example.tripItinerary.Repo.ItineraryDayRepository;
// import com.example.tripItinerary.Repo.ItineraryPlaceRepository;
// import com.example.tripItinerary.Repo.ItineraryRepository;
// import com.example.tripItinerary.Repo.LocationRepository;
// import com.example.tripItinerary.Repo.RestaurantRepository;
// import com.example.tripItinerary.Repo.TemporaryItineraryRepository;
// import com.example.tripItinerary.Repo.TouristPlaceRepository;
// import com.example.tripItinerary.Repo.UserRepository;
// import com.example.tripItinerary.Service.ItineraryService;
// import com.example.tripItinerary.enums.ItineraryStatus;
// import com.example.tripItinerary.enums.PlaceType;
// import com.example.tripItinerary.exception.ResourceNotFoundException;
// import com.example.tripItinerary.exception.UnauthorizedException;
// import com.example.tripItinerary.security.util.SecurityUtils;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.fasterxml.jackson.core.JsonProcessingException;

// import lombok.NonNull;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// @Slf4j
// @Service
// @RequiredArgsConstructor
// @Transactional
// public class ItineraryServiceImpl implements ItineraryService {

//         // ==========================================================
//         // REPOSITORIES
//         // ==========================================================

//         private final ItineraryRepository itineraryRepository;
//         private final UserRepository userRepository;
//         private final LocationRepository locationRepository;

//         private final TouristPlaceRepository touristPlaceRepository;
//         private final HotelRepository hotelRepository;
//         private final RestaurantRepository restaurantRepository;

//         private final ItineraryDayRepository itineraryDayRepository;
//         private final ItineraryPlaceRepository itineraryPlaceRepository;

//         // ==========================================================
//         // MAPPERS / SECURITY
//         // ==========================================================

//         private final ItineraryMapper itineraryMapper;
//         private final SecurityUtils securityUtils;
//         private final ObjectMapper objectMapper;
//         private final TemporaryItineraryRepository temporaryItineraryRepository;

//         // ==========================================================
//         // CONSTANTS
//         // ==========================================================

//         private static final int MAX_DAYS = 30;

//         private static final int MAX_PLACES_PER_DAY = 7;
//         private static final int MAX_PRIMARY_TOURIST_PLACES_PER_DAY = 3;
//         private static final int MAX_TOTAL_ACTIVITIES_PER_DAY = 7;
//         private static final int ACTIVITY_BUFFER_MINUTES = 10;
//         private static final int RESTAURANT_DURATION_MINUTES = 60;
//         private static final int HOTEL_DURATION_MINUTES = 60;
//         private static final int DAY_START_HOUR = 9;
//         private static final int DAY_END_HOUR = 23;
//         private static final double RESTAURANT_MAX_DISTANCE_KM = 5.0;
//         private static final double TOURIST_MAX_DISTANCE_KM = 7.0;
//         private static final double HOTEL_MAX_DISTANCE_KM = 7.0;

//         private static final int DEFAULT_VISIT_MINUTES = 90;

//         // private static final int DEFAULT_TRAVEL_MINUTES = 30;
//         private static final int DEFAULT_AVERAGE_SPEED_KM_PER_HOUR = 20;

//         private static final int NUMBER_OF_OPTIONS = 5;

//         // ==========================================================
//         // BUDGET DISTRIBUTION
//         // ==========================================================

//         private static final BigDecimal HOTEL_BUDGET_PERCENTAGE = BigDecimal.valueOf(0.30);

//         private static final BigDecimal RESTAURANT_BUDGET_PERCENTAGE = BigDecimal.valueOf(0.30);

//         private static final BigDecimal PLACES_BUDGET_PERCENTAGE = BigDecimal.valueOf(0.25);

//         // ==========================================================
//         // TEMPORARY GENERATED OPTIONS
//         // ==========================================================
//         //
//         // create()
//         // ↓
//         // Generate 3 options
//         // ↓
//         // Store in memory
//         // ↓
//         // Return selectionId
//         //
//         // selectGeneratedItinerary()
//         // ↓
//         // Find selectionId
//         // ↓
//         // Reset temporary IDs
//         // ↓
//         // Save itinerary
//         // ↓
//         // Save days
//         // ↓
//         // Save places
//         //
//         // NOTE:
//         // ConcurrentHashMap is okay for current single-server setup.
//         // Production multi-instance deployment should use Redis/database.
//         //
//         // ==========================================================

//         // ==========================================================
//         // CREATE / GENERATE 3 OPTIONS
//         // ==========================================================

//         @Override
//         public List<ItineraryResponse> create(
//                         ItineraryRequest request) {

//                 log.info(
//                                 "Generating {} itinerary options for user={}",
//                                 NUMBER_OF_OPTIONS,
//                                 request != null
//                                                 ? request.getUserId()
//                                                 : null);

//                 // ======================================================
//                 // STEP 1: VALIDATE REQUEST
//                 // ======================================================

//                 validateRequest(request);

//                 // ======================================================
//                 // STEP 2: FETCH USER
//                 // ======================================================

//                 @SuppressWarnings("null")
//                 User user = getUser(
//                                 request.getUserId());

//                 System.out.println(
//                                 "printing the user object" + request.getUserId());
//                 // ======================================================
//                 // STEP 3: FETCH LOCATION
//                 // ======================================================

//                 Location location = getLocation(
//                                 request.getLocationId());

//                 // ======================================================
//                 // STEP 4: FETCH ALL SOURCE DATA ONCE
//                 // ======================================================

//                 List<TouristPlace> allTouristPlaces = fetchTouristPlaces(location);

//                 List<Hotel> allHotels = hotelRepository.findActiveByLocationId(
//                                 location.getId());

//                 List<Restaurant> allRestaurants = restaurantRepository
//                                 .findByLocationIdAndActiveTrue(
//                                                 location.getId());

//                 log.info(
//                                 "Source data => places={}, hotels={}, restaurants={}",
//                                 allTouristPlaces.size(),
//                                 allHotels.size(),
//                                 allRestaurants.size());

//                 // ======================================================
//                 // STEP 5: GENERATE THREE OPTIONS
//                 // ======================================================

//                 List<ItineraryResponse> responses = new ArrayList<>();

//                 for (int optionNumber = 1; optionNumber <= NUMBER_OF_OPTIONS; optionNumber++) {

//                         log.info(
//                                         "Generating itinerary option {}",
//                                         optionNumber);

//                         // ==================================================
//                         // GENERATION TIMESTAMP
//                         // ==================================================

//                         LocalDateTime generatedAt = LocalDateTime.now();

//                         // ==================================================
//                         // CREATE ENTITY IN MEMORY
//                         // ==================================================

//                         Itinerary itinerary = itineraryMapper.toEntity(request);

//                         buildItinerary(
//                                         itinerary,
//                                         request,
//                                         user,
//                                         location);

//                         // ==================================================
//                         // TIMESTAMPS
//                         // ==================================================

//                         itinerary.setCreatedAt(
//                                         generatedAt);

//                         itinerary.setUpdatedAt(
//                                         generatedAt);

//                         // ==================================================
//                         // CREATE DAYS IN MEMORY
//                         // ==================================================

//                         createDefaultDaysInMemory(
//                                         itinerary,
//                                         generatedAt,
//                                         optionNumber);

//                         // ==================================================
//                         // TOURIST PLACES
//                         // ==================================================

//                         List<TouristPlace> filteredPlaces = filterTouristPlaces(
//                                         allTouristPlaces,
//                                         itinerary);

//                         List<TouristPlace> sortedPlaces = sortTouristPlacesForOption(
//                                         filteredPlaces,
//                                         optionNumber);

//                         // ==================================================
//                         // SMART DAY-BY-DAY SCHEDULER
//                         // ==================================================
//                         // Tourist places, restaurants and hotels are scheduled
//                         // together so that time, meal windows, distance and
//                         // activity limits are respected.

//                         List<Hotel> hotels = assignHotelsForOption(
//                                         itinerary,
//                                         allHotels,
//                                         optionNumber);

//                         List<Restaurant> restaurants = assignRestaurantsForOption(
//                                         itinerary,
//                                         allRestaurants,
//                                         optionNumber);

//                         generateSmartDaySchedules(
//                                         itinerary,
//                                         sortedPlaces,
//                                         restaurants,
//                                         hotels,
//                                         location,
//                                         optionNumber);

//                         // ==================================================
//                         // CALCULATE ESTIMATED COST
//                         // ==================================================

//                         BigDecimal estimatedCost = calculateTotalEstimatedCost(
//                                         itinerary);

//                         itinerary.setEstimatedCost(
//                                         estimatedCost);

//                         // ==================================================
//                         // REMAINING BUDGET
//                         // ==================================================

//                         BigDecimal remainingBudget = calculateRemainingBudget(
//                                         itinerary);

//                         itinerary.setRemainingBudget(
//                                         remainingBudget);

//                         // ==================================================
//                         // STATUS
//                         // ==================================================

//                         updateStatusAutomatically(
//                                         itinerary);

//                         // ==================================================
//                         // SELECTION ID
//                         // ==================================================

//                         String selectionId = UUID.randomUUID().toString();

//                         // ==================================================
//                         // STORE GENERATED OPTION
//                         // ==================================================

//                         saveTemporaryItinerary(
//                                         user.getId(),
//                                         selectionId,
//                                         optionNumber,
//                                         itinerary);

//                         // ==================================================
//                         // MAP ENTITY -> RESPONSE
//                         // ==================================================

//                         ItineraryResponse response = itineraryMapper.toResponse(
//                                         itinerary);

//                         // ==================================================
//                         // SET OPTION INFORMATION
//                         // ==================================================

//                         response.setSelectionId(
//                                         selectionId);

//                         response.setOptionNumber(
//                                         optionNumber);

//                         // Ensure generated timestamps are returned
//                         response.setCreatedAt(
//                                         generatedAt);

//                         response.setUpdatedAt(
//                                         generatedAt);

//                         // ==================================================
//                         // ADD RESPONSE
//                         // ==================================================

//                         responses.add(
//                                         response);

//                         log.info(
//                                         "Option {} generated. selectionId={}, places={}, hotels={}, restaurants={}, estimatedCost={}",
//                                         optionNumber,
//                                         selectionId,
//                                         sortedPlaces.size(),
//                                         hotels.size(),
//                                         restaurants.size(),
//                                         estimatedCost);
//                 }

//                 log.info(
//                                 "Successfully generated {} itinerary options",
//                                 responses.size());

//                 return responses;
//         }
//         // ==========================================================
//         // TEMPEROARY SAVED ITINERARY
//         // ==========================================================

//         private void saveTemporaryItinerary(
//                         Long userId,
//                         String selectionId,
//                         int optionNumber,
//                         Itinerary itinerary) {

//                 try {

//                         LocalDateTime now = LocalDateTime.now();

//                         LocalDateTime expiresAt = now.plusHours(2);

//                         // ==================================================
//                         // IMPORTANT
//                         // ==================================================
//                         //
//                         // Entity ko directly JSON serialize karne par
//                         // circular relationship aa sakta hai:
//                         //
//                         // Itinerary
//                         // -> Days
//                         // -> Itinerary
//                         // -> Days
//                         //
//                         // Isliye response DTO ko JSON me store karna
//                         // safer hai.
//                         //
//                         // ==================================================

//                         ItineraryResponse response = itineraryMapper.toResponse(
//                                         itinerary);

//                         response.setSelectionId(
//                                         selectionId);

//                         response.setOptionNumber(
//                                         optionNumber);

//                         String json = objectMapper.writeValueAsString(
//                                         response);

//                         TemporaryItinerary temporary = TemporaryItinerary.builder()
//                                         .userId(
//                                                         userId)
//                                         .selectionId(
//                                                         selectionId)
//                                         .optionNumber(
//                                                         optionNumber)
//                                         .itineraryData(
//                                                         json)
//                                         .createdAt(
//                                                         now)
//                                         .expiresAt(
//                                                         expiresAt)
//                                         .build();

//                         temporaryItineraryRepository.save(
//                                         temporary);

//                         log.info(
//                                         "Temporary itinerary saved. userId={}, selectionId={}, option={}",
//                                         userId,
//                                         selectionId,
//                                         optionNumber);

//                 } catch (JsonProcessingException e) {

//                         log.error(
//                                         "Failed to serialize temporary itinerary",
//                                         e);

//                         throw new IllegalStateException(
//                                         "Unable to store generated itinerary.",
//                                         e);
//                 }
//         }

//         // ==========================================================
//         // SELECT GENERATED ITINERARY
//         // ==========================================================

//         @Override
//         @Transactional
//         public ItineraryResponse selectGeneratedItinerary(
//                         @NonNull Long userId,
//                         @NonNull String selectionId) {

//                 log.info(
//                                 "Selecting generated itinerary. userId={}, selectionId={}",
//                                 userId,
//                                 selectionId);

//                 // ======================================================
//                 // STEP 1: USER
//                 // ======================================================

//                 User user = getUser(userId);

//                 // ======================================================
//                 // STEP 2: FIND TEMPORARY RECORD
//                 // ======================================================

//                 TemporaryItinerary temporary = temporaryItineraryRepository
//                                 .findBySelectionIdAndUserId(
//                                                 selectionId,
//                                                 userId)
//                                 .orElseThrow(() -> new ResourceNotFoundException(
//                                                 "Generated itinerary option not found or expired."));

//                 // ======================================================
//                 // STEP 3: EXPIRATION
//                 // ======================================================

//                 if (temporary.getExpiresAt()
//                                 .isBefore(LocalDateTime.now())) {

//                         temporaryItineraryRepository.delete(
//                                         temporary);

//                         throw new ResourceNotFoundException(
//                                         "Generated itinerary option has expired.");
//                 }

//                 // ======================================================
//                 // STEP 4: JSON -> RESPONSE
//                 // ======================================================

//                 ItineraryResponse generatedResponse;

//                 try {

//                         generatedResponse = objectMapper.readValue(
//                                         temporary.getItineraryData(),
//                                         ItineraryResponse.class);

//                 } catch (JsonProcessingException e) {

//                         log.error(
//                                         "Unable to deserialize temporary itinerary. selectionId={}",
//                                         selectionId,
//                                         e);

//                         throw new IllegalStateException(
//                                         "Unable to read generated itinerary.",
//                                         e);
//                 }

//                 // ======================================================
//                 // STEP 5: CREATE COMPLETELY FRESH ITINERARY
//                 // ======================================================

//                 Itinerary itinerary = buildFreshItineraryForPersistence(
//                                 generatedResponse,
//                                 user);

//                 LocalDateTime now = LocalDateTime.now();

//                 itinerary.setCreatedAt(now);
//                 itinerary.setUpdatedAt(now);

//                 // ======================================================
//                 // STEP 6:
//                 // REMOVE CHILDREN BEFORE SAVING PARENT
//                 // ======================================================
//                 //
//                 // This is the important fix.
//                 //
//                 // Even if Itinerary has CascadeType.ALL,
//                 // Hibernate won't save the days here.
//                 //
//                 // ======================================================

//                 List<ItineraryDay> days = itinerary.getItineraryDays();

//                 itinerary.setItineraryDays(
//                                 days);

//                 // ======================================================
//                 // STEP 7: SAVE ONLY ITINERARY
//                 // ======================================================

//                 Itinerary savedItinerary = itineraryRepository.save(
//                                 itinerary);

//                 log.info(
//                                 "Main itinerary saved successfully. id={}",
//                                 savedItinerary.getId());

//                 // ======================================================
//                 // STEP 8: SAVE DAYS
//                 // ======================================================

//                 List<ItineraryDay> savedDays = new ArrayList<>();

//                 if (days != null &&
//                                 !days.isEmpty()) {

//                         List<ItineraryDay> freshDays = new ArrayList<>();

//                         for (ItineraryDay oldDay : days) {

//                                 // ==================================================
//                                 // IMPORTANT:
//                                 // Create a NEW entity.
//                                 // Never reuse old entity.
//                                 // ==================================================

//                                 ItineraryDay newDay = ItineraryDay.builder()
//                                                 .itinerary(
//                                                                 savedItinerary)
//                                                 .dayNumber(
//                                                                 oldDay.getDayNumber())
//                                                 .travelDate(
//                                                                 oldDay.getTravelDate())
//                                                 .title(
//                                                                 oldDay.getTitle())
//                                                 .notes(
//                                                                 oldDay.getNotes())
//                                                 .itineraryPlaces(
//                                                                 new ArrayList<>())
//                                                 .build();

//                                 freshDays.add(
//                                                 newDay);
//                         }

//                         savedDays = itineraryDayRepository.saveAll(
//                                         freshDays);
//                 }

//                 log.info(
//                                 "{} itinerary days saved.",
//                                 savedDays.size());

//                 // ======================================================
//                 // STEP 9: SAVE PLACES
//                 // ======================================================

//                 List<ItineraryPlace> freshPlaces = new ArrayList<>();

//                 for (int i = 0; i < savedDays.size(); i++) {

//                         ItineraryDay savedDay = savedDays.get(i);

//                         @SuppressWarnings("null")
//                         ItineraryDay oldDay = days.get(i);

//                         if (oldDay.getItineraryPlaces() == null ||
//                                         oldDay.getItineraryPlaces().isEmpty()) {

//                                 continue;
//                         }

//                         for (ItineraryPlace oldPlace : oldDay.getItineraryPlaces()) {

//                                 ItineraryPlace newPlace = ItineraryPlace.builder()
//                                                 .itineraryDay(
//                                                                 savedDay)
//                                                 .placeType(
//                                                                 oldPlace.getPlaceType())
//                                                 .referenceId(
//                                                                 oldPlace.getReferenceId())
//                                                 .visitOrder(
//                                                                 oldPlace.getVisitOrder())
//                                                 .plannedStartTime(
//                                                                 oldPlace.getPlannedStartTime())
//                                                 .plannedEndTime(
//                                                                 oldPlace.getPlannedEndTime())
//                                                 .estimatedCost(
//                                                                 oldPlace.getEstimatedCost())
//                                                 .travelTimeMinutes(
//                                                                 oldPlace.getTravelTimeMinutes())
//                                                 .distanceKm(
//                                                                 oldPlace.getDistanceKm())
//                                                 .notes(
//                                                                 oldPlace.getNotes())
//                                                 .completed(
//                                                                 Boolean.TRUE.equals(
//                                                                                 oldPlace.getCompleted()))
//                                                 .build();

//                                 freshPlaces.add(
//                                                 newPlace);
//                         }
//                 }

//                 // ======================================================
//                 // STEP 10: SAVE PLACES
//                 // ======================================================

//                 if (!freshPlaces.isEmpty()) {

//                         itineraryPlaceRepository.saveAll(
//                                         freshPlaces);
//                 }

//                 log.info(
//                                 "{} itinerary places saved.",
//                                 freshPlaces.size());

//                 // ======================================================
//                 // STEP 11:
//                 // RELOAD ACTUAL DB ENTITY
//                 // ======================================================

//                 Itinerary finalItinerary = itineraryRepository
//                                 .findById(
//                                                 savedItinerary.getId())
//                                 .orElseThrow(() -> new ResourceNotFoundException(
//                                                 "Saved itinerary could not be found."));

//                 // ======================================================
//                 // STEP 12:
//                 // DELETE TEMPORARY RECORD
//                 // ======================================================

//                 temporaryItineraryRepository.delete(
//                                 temporary);

//                 temporaryItineraryRepository.flush();

//                 // ======================================================
//                 // STEP 13: RETURN REAL DB DATA
//                 // ======================================================

//                 log.info(
//                                 "Generated itinerary selected successfully. " +
//                                                 "userId={}, selectionId={}, itineraryId={}",
//                                 userId,
//                                 selectionId,
//                                 finalItinerary.getId());

//                 return itineraryMapper.toResponse(
//                                 finalItinerary);
//         }

//         private Itinerary buildFreshItineraryForPersistence(
//                         ItineraryResponse response,
//                         User user) {

//                 Location location = getLocation(
//                                 response.getLocationId());

//                 Itinerary itinerary = Itinerary.builder()
//                                 .user(user)
//                                 .location(location)
//                                 .title(response.getTitle())
//                                 .description(response.getDescription())
//                                 .totalDays(response.getTotalDays())
//                                 .totalBudget(response.getTotalBudget())
//                                 .estimatedCost(
//                                                 response.getEstimatedCost() != null
//                                                                 ? response.getEstimatedCost()
//                                                                 : BigDecimal.ZERO)
//                                 .remainingBudget(
//                                                 response.getRemainingBudget() != null
//                                                                 ? response.getRemainingBudget()
//                                                                 : BigDecimal.ZERO)
//                                 .travelType(response.getTravelType())
//                                 .itineraryStatus(response.getItineraryStatus())
//                                 .startDate(response.getStartDate())
//                                 .endDate(response.getEndDate())
//                                 .restaurantsPerDay(
//                                                 response.getRestaurantsPerDay() != null
//                                                                 ? Math.max(
//                                                                                 1,
//                                                                                 response.getRestaurantsPerDay())
//                                                                 : 1)
//                                 .itineraryDays(new ArrayList<>())
//                                 .build();

//                 // ======================================================
//                 // BUILD DAYS
//                 // ======================================================

//                 if (response.getItineraryDays() == null) {
//                         return itinerary;
//                 }

//                 for (var dayResponse : response.getItineraryDays()) {

//                         ItineraryDay day = ItineraryDay.builder()
//                                         .itinerary(itinerary)
//                                         .dayNumber(
//                                                         dayResponse.getDayNumber())
//                                         .travelDate(
//                                                         dayResponse.getTravelDate())
//                                         .title(
//                                                         dayResponse.getTitle())
//                                         .notes(
//                                                         dayResponse.getNotes())
//                                         .itineraryPlaces(
//                                                         new ArrayList<>())
//                                         .build();

//                         // ==================================================
//                         // BUILD PLACES
//                         // ==================================================

//                         if (dayResponse.getItineraryPlaces() != null) {

//                                 for (var placeResponse : dayResponse.getItineraryPlaces()) {

//                                         ItineraryPlace place = ItineraryPlace.builder()
//                                                         .itineraryDay(day)
//                                                         .placeType(
//                                                                         placeResponse.getPlaceType())
//                                                         .referenceId(
//                                                                         placeResponse.getReferenceId())
//                                                         .visitOrder(
//                                                                         placeResponse.getVisitOrder())
//                                                         .plannedStartTime(
//                                                                         placeResponse.getPlannedStartTime())
//                                                         .plannedEndTime(
//                                                                         placeResponse.getPlannedEndTime())
//                                                         .estimatedCost(
//                                                                         placeResponse.getEstimatedCost())
//                                                         .travelTimeMinutes(
//                                                                         placeResponse
//                                                                                         .getTravelTimeMinutes())
//                                                         .distanceKm(
//                                                                         placeResponse.getDistanceKm())
//                                                         .notes(
//                                                                         placeResponse.getNotes())
//                                                         .completed(
//                                                                         Boolean.TRUE.equals(
//                                                                                         placeResponse.getCompleted()))
//                                                         .build();

//                                         day.addPlace(place);
//                                 }
//                         }

//                         itinerary.getItineraryDays()
//                                         .add(day);
//                 }

//                 return itinerary;
//         }

//         // ==========================================================
//         // MARK PLACE COMPLETED
//         // ==========================================================

//         @Override
//         public ItineraryResponse markPlaceCompleted(
//                         @NonNull Long itineraryPlaceId) {

//                 log.info(
//                                 "Marking itinerary place completed: {}",
//                                 itineraryPlaceId);

//                 // ======================================================
//                 // FETCH PLACE
//                 // ======================================================

//                 ItineraryPlace itineraryPlace = itineraryPlaceRepository
//                                 .findById(
//                                                 itineraryPlaceId)
//                                 .orElseThrow(
//                                                 () -> new ResourceNotFoundException(
//                                                                 "Itinerary place not found with id: "
//                                                                                 + itineraryPlaceId));

//                 // ======================================================
//                 // FETCH DAY
//                 // ======================================================

//                 ItineraryDay day = itineraryPlace.getItineraryDay();

//                 if (day == null ||
//                                 day.getItinerary() == null) {

//                         throw new ResourceNotFoundException(
//                                         "Itinerary information not found.");
//                 }

//                 Itinerary itinerary = day.getItinerary();

//                 // ======================================================
//                 // SECURITY
//                 // ======================================================

//                 System.out.println("Printing the itinerary object: " + itinerary.getId());
//                 // validateOwnership(
//                 // itinerary);

//                 // ======================================================
//                 // MARK COMPLETED
//                 // ======================================================

//                 itineraryPlace.setCompleted(
//                                 true);

//                 itineraryPlaceRepository.save(
//                                 itineraryPlace);

//                 // ======================================================
//                 // UPDATE PROGRESS
//                 // ======================================================

//                 updateItineraryProgress(
//                                 itinerary);

//                 itinerary.setUpdatedAt(
//                                 LocalDateTime.now());

//                 itineraryRepository.save(
//                                 itinerary);

//                 return itineraryMapper.toResponse(
//                                 itinerary);
//         }

//         // ==========================================================
//         // UPDATE ITINERARY PROGRESS
//         // ==========================================================

//         private void updateItineraryProgress(
//                         Itinerary itinerary) {

//                 List<ItineraryDay> days = itineraryDayRepository
//                                 .findByItineraryIdOrderByDayNumber(
//                                                 itinerary.getId());

//                 if (days == null ||
//                                 days.isEmpty()) {

//                         return;
//                 }

//                 List<ItineraryPlace> places = days.stream()
//                                 .filter(Objects::nonNull)
//                                 .flatMap(
//                                                 day -> {

//                                                         if (day.getItineraryPlaces() == null) {
//                                                                 return Stream.empty();
//                                                         }

//                                                         return day.getItineraryPlaces()
//                                                                         .stream();
//                                                 })
//                                 .toList();

//                 if (places.isEmpty()) {
//                         return;
//                 }

//                 long completedPlaces = places.stream()
//                                 .filter(
//                                                 place -> Boolean.TRUE.equals(
//                                                                 place.getCompleted()))
//                                 .count();

//                 // ======================================================
//                 // ALL COMPLETED
//                 // ======================================================

//                 if (completedPlaces == places.size()) {

//                         itinerary.setItineraryStatus(
//                                         ItineraryStatus.COMPLETED);

//                         return;
//                 }

//                 // ======================================================
//                 // SOME COMPLETED
//                 // ======================================================

//                 if (completedPlaces > 0) {

//                         itinerary.setItineraryStatus(
//                                         ItineraryStatus.PLANNED);

//                         return;
//                 }

//                 // ======================================================
//                 // NONE COMPLETED
//                 // ======================================================

//                 updateStatusAutomatically(
//                                 itinerary);
//         }

//         // ==========================================================
//         // CREATE DEFAULT DAYS IN MEMORY
//         // ==========================================================

//         private void createDefaultDaysInMemory(
//                         Itinerary itinerary,
//                         LocalDateTime generatedAt,
//                         int optionNumber) {

//                 List<ItineraryDay> days = new ArrayList<>();

//                 LocalDate startDate = itinerary.getStartDate();

//                 if (startDate == null) {
//                         startDate = LocalDate.now();
//                 }

//                 for (int dayNumber = 1; dayNumber <= itinerary.getTotalDays(); dayNumber++) {

//                         LocalDate travelDate = startDate.plusDays(
//                                         dayNumber - 1);

//                         ItineraryDay day = ItineraryDay.builder()
//                                         .itinerary(
//                                                         itinerary)
//                                         .dayNumber(
//                                                         dayNumber)
//                                         .title(
//                                                         generateDayTitle(
//                                                                         dayNumber))
//                                         .notes(
//                                                         "Trip activities for Day "
//                                                                         + dayNumber)
//                                         .travelDate(
//                                                         travelDate)
//                                         .itineraryPlaces(
//                                                         new ArrayList<>())
//                                         .build();

//                         // --------------------------------------------------
//                         // Created timestamp
//                         // --------------------------------------------------

//                         day.setCreatedAt(
//                                         generatedAt);

//                         days.add(
//                                         day);
//                 }

//                 itinerary.setItineraryDays(
//                                 days);
//         }

//         // ==========================================================
//         // SMART DAY-BY-DAY ITINERARY SCHEDULER
//         // ==========================================================

//         /**
//          * Generates the complete day schedule in one pass.
//          *
//          * Important rules:
//          * 1. Tourist places, restaurants and hotels are NOT scheduled independently.
//          * 2. A meal window has priority. If a restaurant can be scheduled in that
//          * window, no tourist-place activity is inserted in the same window.
//          * 3. A tourist place already started is never interrupted by a meal window.
//          * 4. Distance from the previous activity is considered before selecting the
//          * next activity.
//          * 5. Three tourist places/day is the preferred/primary target, NOT a reason
//          * to stop the day early. If time remains, suitable activities can still
//          * be added until the daily activity cap.
//          * 6. Existing response structure is unchanged: ItineraryDay -> ItineraryPlace
//          * with the same placeType/referenceId/time/cost/notes fields.
//          */
//         private void generateSmartDaySchedules(
//                         Itinerary itinerary,
//                         List<TouristPlace> touristPlaces,
//                         List<Restaurant> restaurants,
//                         List<Hotel> hotels,
//                         Location location,
//                         int optionNumber) {

//                 if (itinerary == null || itinerary.getItineraryDays() == null) {
//                         return;
//                 }

//                 Set<Long> usedTouristPlaceIds = new HashSet<>();
//                 Set<Long> usedRestaurantIds = new HashSet<>();
//                 Set<Long> usedHotelIds = new HashSet<>();

//                 for (ItineraryDay day : itinerary.getItineraryDays()) {
//                         if (day == null) {
//                                 continue;
//                         }

//                         scheduleSingleDay(
//                                         day,
//                                         itinerary,
//                                         touristPlaces,
//                                         restaurants,
//                                         hotels,
//                                         location,
//                                         optionNumber,
//                                         usedTouristPlaceIds,
//                                         usedRestaurantIds,
//                                         usedHotelIds);
//                 }
//         }

//         private void scheduleSingleDay(
//                         ItineraryDay day,
//                         Itinerary itinerary,
//                         List<TouristPlace> touristPlaces,
//                         List<Restaurant> restaurants,
//                         List<Hotel> hotels,
//                         Location location,
//                         int optionNumber,
//                         Set<Long> usedTouristPlaceIds,
//                         Set<Long> usedRestaurantIds,
//                         Set<Long> usedHotelIds) {

//                 LocalTime currentTime = getDefaultStartTime();
//                 LocalTime dayEnd = LocalTime.of(DAY_END_HOUR, 0);
//                 int visitOrder = 1;
//                 int activityCount = 0;
//                 int touristCount = 0;
//                 int restaurantCount = 0;
//                 int restaurantsPerDay = Math.max(0, itinerary.getSafeRestaurantsPerDay());

//                 double previousLat = getCoordinate(location, "latitude");
//                 double previousLng = getCoordinate(location, "longitude");
//                 boolean previousCoordinatesAvailable = hasCoordinates(location);

//                 while (currentTime.isBefore(dayEnd)
//                                 && activityCount < MAX_TOTAL_ACTIVITIES_PER_DAY) {

//                         // --------------------------------------------------
//                         // 1. MEAL WINDOW HAS HARD PRIORITY
//                         // --------------------------------------------------
//                         MealWindow mealWindow = getCurrentMealWindow(currentTime, restaurantsPerDay);

//                         if (mealWindow != null) {
//                                 // Meal window is reserved for restaurant activity only.
//                                 // If today's restaurant quota is already fulfilled, we
//                                 // intentionally wait until the meal window closes.
//                                 int activeWindowIndex = getMealWindowIndex(mealWindow, restaurantsPerDay);
//                                 boolean restaurantSlotStillRequired = activeWindowIndex >= 0
//                                                 && restaurantCount <= activeWindowIndex
//                                                 && restaurantCount < restaurantsPerDay;

//                                 if (restaurantSlotStillRequired) {
//                                         Restaurant restaurant = findBestRestaurantForSlot(
//                                                         restaurants,
//                                                         usedRestaurantIds,
//                                                         mealWindow.start,
//                                                         mealWindow.end,
//                                                         previousLat,
//                                                         previousLng,
//                                                         previousCoordinatesAvailable,
//                                                         optionNumber);

//                                         if (restaurant != null) {
//                                                 double restaurantDistance = distanceKm(
//                                                                 previousCoordinatesAvailable,
//                                                                 previousLat,
//                                                                 previousLng,
//                                                                 getCoordinate(restaurant, "latitude"),
//                                                                 getCoordinate(restaurant, "longitude"));

//                                                 int restaurantTravelMinutes = calculateTravelTimeMinutes(
//                                                                 restaurantDistance);

//                                                 LocalTime restaurantStart = currentTime
//                                                                 .plusMinutes(restaurantTravelMinutes);
//                                                 if (restaurantStart.isBefore(mealWindow.start)) {
//                                                         restaurantStart = mealWindow.start;
//                                                 }

//                                                 LocalTime restaurantOpening = getTimeProperty(
//                                                                 restaurant, "openingTime", "openTime", "openingHour");
//                                                 if (restaurantOpening != null
//                                                                 && restaurantOpening.isAfter(restaurantStart)) {
//                                                         restaurantStart = restaurantOpening;
//                                                 }

//                                                 LocalTime restaurantEnd = restaurantStart
//                                                                 .plusMinutes(RESTAURANT_DURATION_MINUTES);

//                                                 if (!restaurantEnd.isAfter(mealWindow.end)
//                                                                 && !restaurantEnd.isAfter(dayEnd)
//                                                                 && isOpenForSlot(restaurant, restaurantStart,
//                                                                                 restaurantEnd)) {

//                                                         ItineraryPlace restaurantPlace = buildRestaurantItineraryPlace(
//                                                                         day,
//                                                                         restaurant,
//                                                                         visitOrder++,
//                                                                         restaurantStart,
//                                                                         toDistanceBigDecimal(restaurantDistance),
//                                                                         restaurantTravelMinutes);

//                                                         day.addPlace(restaurantPlace);
//                                                         usedRestaurantIds.add(restaurant.getId());
//                                                         restaurantCount++;
//                                                         activityCount++;

//                                                         if (hasCoordinates(restaurant)) {
//                                                                 previousLat = getCoordinate(restaurant, "latitude");
//                                                                 previousLng = getCoordinate(restaurant, "longitude");
//                                                                 previousCoordinatesAvailable = true;
//                                                         }

//                                                         currentTime = restaurantEnd
//                                                                         .plusMinutes(ACTIVITY_BUFFER_MINUTES);
//                                                         continue;
//                                                 }
//                                         }
//                                 }

//                                 // A meal window is intentionally reserved for food.
//                                 // If no restaurant is feasible/open, do not insert a
//                                 // tourist activity or hotel into this window.
//                                 if (currentTime.isBefore(mealWindow.end)) {
//                                         currentTime = mealWindow.end;
//                                         continue;
//                                 }
//                         }

//                         // --------------------------------------------------
//                         // 2. HOTEL IS LAST / END-OF-DAY ACTIVITY
//                         // --------------------------------------------------
//                         if (currentTime.compareTo(LocalTime.of(20, 0)) >= 0) {
//                                 Hotel hotel = findBestHotelForEndOfDay(
//                                                 hotels,
//                                                 usedHotelIds,
//                                                 previousLat,
//                                                 previousLng,
//                                                 previousCoordinatesAvailable,
//                                                 itinerary,
//                                                 optionNumber);

//                                 if (hotel != null) {
//                                         double hotelDistance = distanceKm(
//                                                         previousCoordinatesAvailable,
//                                                         previousLat,
//                                                         previousLng,
//                                                         getCoordinate(hotel, "latitude"),
//                                                         getCoordinate(hotel, "longitude"));

//                                         int hotelTravelMinutes = calculateTravelTimeMinutes(hotelDistance);

//                                         LocalTime hotelStart = currentTime.plusMinutes(hotelTravelMinutes);
//                                         LocalTime hotelEnd = hotelStart.plusMinutes(HOTEL_DURATION_MINUTES);

//                                         // Never put hotel inside an active meal window.
//                                         MealWindow nextMeal = getActiveMealWindow(hotelStart, restaurantsPerDay,
//                                                         restaurantCount);
//                                         if (nextMeal != null) {
//                                                 currentTime = nextMeal.end;
//                                                 continue;
//                                         }

//                                         if (!hotelEnd.isAfter(dayEnd)) {
//                                                 ItineraryPlace hotelPlace = buildHotelItineraryPlace(
//                                                                 day,
//                                                                 hotel,
//                                                                 99,
//                                                                 hotelStart,
//                                                                 toDistanceBigDecimal(hotelDistance),
//                                                                 hotelTravelMinutes);
//                                                 day.addPlace(hotelPlace);
//                                                 usedHotelIds.add(hotel.getId());
//                                                 activityCount++;

//                                                 if (hasCoordinates(hotel)) {
//                                                         previousLat = getCoordinate(hotel, "latitude");
//                                                         previousLng = getCoordinate(hotel, "longitude");
//                                                         previousCoordinatesAvailable = true;
//                                                 }

//                                                 currentTime = hotelEnd;
//                                         }
//                                 }
//                                 break;
//                         }

//                         // --------------------------------------------------
//                         // 3. TOURIST PLACE
//                         // --------------------------------------------------
//                         TouristPlace nextPlace = findBestTouristPlace(
//                                         touristPlaces,
//                                         usedTouristPlaceIds,
//                                         currentTime,
//                                         dayEnd,
//                                         previousLat,
//                                         previousLng,
//                                         previousCoordinatesAvailable,
//                                         optionNumber,
//                                         touristCount < MAX_PRIMARY_TOURIST_PLACES_PER_DAY);

//                         if (nextPlace == null) {
//                                 // If a tourist place cannot fit before the next meal,
//                                 // jump to the next relevant meal window instead of
//                                 // forcing an overlap.
//                                 LocalTime nextMealStart = getNextMealWindowStart(
//                                                 currentTime,
//                                                 restaurantsPerDay,
//                                                 restaurantCount);
//                                 if (nextMealStart != null && nextMealStart.isAfter(currentTime)) {
//                                         currentTime = nextMealStart;
//                                         continue;
//                                 }
//                                 break;
//                         }

//                         int visitMinutes = getVisitMinutes(nextPlace);
//                         LocalTime endTime = currentTime.plusMinutes(visitMinutes);

//                         if (endTime.isAfter(dayEnd)) {
//                                 break;
//                         }

//                         // Do not start a tourist activity if it would cross a meal
//                         // window. The scheduler will start the restaurant first.
//                         MealWindow upcomingMeal = getNextMealWindowContainingOrAfter(
//                                         currentTime,
//                                         restaurantsPerDay,
//                                         restaurantCount);

//                         if (upcomingMeal != null
//                                         && currentTime.isBefore(upcomingMeal.start)
//                                         && endTime.isAfter(upcomingMeal.start)) {
//                                 currentTime = upcomingMeal.start;
//                                 continue;
//                         }

//                         double touristDistance = distanceKm(
//                                         previousCoordinatesAvailable,
//                                         previousLat,
//                                         previousLng,
//                                         getCoordinate(nextPlace, "latitude"),
//                                         getCoordinate(nextPlace, "longitude"));

//                         int touristTravelMinutes = calculateTravelTimeMinutes(touristDistance);

//                         ItineraryPlace itineraryPlace = buildTouristPlaceItineraryPlace(
//                                         day,
//                                         nextPlace,
//                                         visitOrder++,
//                                         currentTime,
//                                         endTime,
//                                         touristTravelMinutes,
//                                         toDistanceBigDecimal(touristDistance));

//                         day.addPlace(itineraryPlace);
//                         usedTouristPlaceIds.add(nextPlace.getId());
//                         touristCount++;
//                         activityCount++;

//                         if (hasCoordinates(nextPlace)) {
//                                 previousLat = getCoordinate(nextPlace, "latitude");
//                                 previousLng = getCoordinate(nextPlace, "longitude");
//                                 previousCoordinatesAvailable = true;
//                         }

//                         currentTime = endTime.plusMinutes(touristTravelMinutes);
//                 }

//                 log.info(
//                                 "Day {} scheduled: touristPlaces={}, restaurants={}, totalActivities={}",
//                                 day.getDayNumber(),
//                                 touristCount,
//                                 restaurantCount,
//                                 activityCount);
//         }

//         // ==========================================================
//         // MEAL WINDOWS
//         // ==========================================================

//         private MealWindow getCurrentMealWindow(LocalTime currentTime, int restaurantsPerDay) {
//                 for (MealWindow window : getMealWindows(restaurantsPerDay)) {
//                         if (!currentTime.isBefore(window.start) && currentTime.isBefore(window.end)) {
//                                 return window;
//                         }
//                 }
//                 return null;
//         }

//         private int getMealWindowIndex(MealWindow target, int restaurantsPerDay) {
//                 List<MealWindow> windows = getMealWindows(restaurantsPerDay);
//                 for (int i = 0; i < windows.size(); i++) {
//                         if (windows.get(i).name.equals(target.name)) {
//                                 return i;
//                         }
//                 }
//                 return -1;
//         }

//         private MealWindow getActiveMealWindow(
//                         LocalTime currentTime,
//                         int restaurantsPerDay,
//                         int restaurantsAlreadyAssigned) {

//                 List<MealWindow> windows = getMealWindows(restaurantsPerDay);

//                 if (restaurantsAlreadyAssigned >= windows.size()) {
//                         return null;
//                 }

//                 // Only the NEXT meal slot can be consumed. This prevents two
//                 // restaurants from being inserted into the same lunch/dinner window.
//                 MealWindow next = windows.get(restaurantsAlreadyAssigned);
//                 if (!currentTime.isBefore(next.start) && currentTime.isBefore(next.end)) {
//                         return next;
//                 }
//                 return null;
//         }

//         private MealWindow getNextMealWindowContainingOrAfter(
//                         LocalTime currentTime,
//                         int restaurantsPerDay,
//                         int restaurantsAlreadyAssigned) {

//                 List<MealWindow> windows = getMealWindows(restaurantsPerDay);
//                 if (restaurantsAlreadyAssigned >= windows.size()) {
//                         return null;
//                 }

//                 for (int i = restaurantsAlreadyAssigned; i < windows.size(); i++) {
//                         MealWindow window = windows.get(i);
//                         if (!currentTime.isAfter(window.end)) {
//                                 return window;
//                         }
//                 }
//                 return null;
//         }

//         private LocalTime getNextMealWindowStart(
//                         LocalTime currentTime,
//                         int restaurantsPerDay,
//                         int restaurantsAlreadyAssigned) {

//                 MealWindow window = getNextMealWindowContainingOrAfter(
//                                 currentTime,
//                                 restaurantsPerDay,
//                                 restaurantsAlreadyAssigned);
//                 return window == null ? null : window.start;
//         }

//         private List<MealWindow> getMealWindows(int restaurantsPerDay) {
//                 List<MealWindow> windows = new ArrayList<>();

//                 if (restaurantsPerDay >= 1) {
//                         windows.add(new MealWindow(LocalTime.of(12, 0), LocalTime.of(14, 0), "LUNCH"));
//                 }
//                 if (restaurantsPerDay >= 2) {
//                         windows.add(new MealWindow(LocalTime.of(19, 0), LocalTime.of(22, 0), "DINNER"));
//                 }
//                 if (restaurantsPerDay >= 3) {
//                         // Insert the evening window before dinner.
//                         windows.add(1, new MealWindow(LocalTime.of(17, 0), LocalTime.of(19, 0), "EVENING"));
//                 }
//                 return windows;
//         }

//         // ==========================================================
//         // TOURIST PLACE SELECTION
//         // ==========================================================

//         private TouristPlace findBestTouristPlace(
//                         List<TouristPlace> places,
//                         Set<Long> usedIds,
//                         LocalTime currentTime,
//                         LocalTime dayEnd,
//                         double previousLat,
//                         double previousLng,
//                         boolean previousCoordinatesAvailable,
//                         int optionNumber,
//                         boolean primarySlot) {

//                 if (places == null || places.isEmpty()) {
//                         return null;
//                 }

//                 List<ScoredCandidate<TouristPlace>> candidates = new ArrayList<>();

//                 for (TouristPlace place : places) {
//                         if (place == null || place.getId() == null || usedIds.contains(place.getId())) {
//                                 continue;
//                         }

//                         int visitMinutes = getVisitMinutes(place);
//                         LocalTime end = currentTime.plusMinutes(visitMinutes);

//                         if (end.isAfter(dayEnd)) {
//                                 continue;
//                         }

//                         double distance = distanceKm(
//                                         previousCoordinatesAvailable,
//                                         previousLat,
//                                         previousLng,
//                                         getCoordinate(place, "latitude"),
//                                         getCoordinate(place, "longitude"));

//                         if (previousCoordinatesAvailable && hasCoordinates(place)
//                                         && distance > TOURIST_MAX_DISTANCE_KM) {
//                                 continue;
//                         }

//                         double score = scoreTouristPlace(place, distance, optionNumber, primarySlot);
//                         candidates.add(new ScoredCandidate<>(place, score));
//                 }

//                 return candidates.stream()
//                                 .sorted(Comparator.comparingDouble(ScoredCandidate<TouristPlace>::score).reversed())
//                                 .map(ScoredCandidate::value)
//                                 .findFirst()
//                                 .orElse(null);
//         }

//         private double scoreTouristPlace(
//                         TouristPlace place,
//                         double distance,
//                         int optionNumber,
//                         boolean primarySlot) {

//                 double rating = normalize(getNumericProperty(place, "averageRating"), 5.0);
//                 double popularity = normalize(getNumericProperty(place, "popularityScore"), 100.0);
//                 double weight = inverseWeight(getNumericProperty(place, "placeWeight", "touristPlaceWeight"));
//                 double distanceScore = distanceScore(distance, TOURIST_MAX_DISTANCE_KM);
//                 double budgetScore = budgetFriendliness(place.getPrice());

//                 double optionBonus;
//                 if (optionNumber == 1) {
//                         optionBonus = rating * 0.15 + popularity * 0.10;
//                 } else if (optionNumber == 2) {
//                         optionBonus = popularity * 0.20 + rating * 0.05;
//                 } else if (optionNumber == 3) {
//                         optionBonus = budgetScore * 0.20 + rating * 0.05;
//                 } else if (optionNumber == 4) {
//                         optionBonus = weight * 0.20 + distanceScore * 0.10;
//                 } else {
//                         optionBonus = distanceScore * 0.20 + rating * 0.10;
//                 }

//                 return weight * 0.30
//                                 + distanceScore * 0.30
//                                 + rating * 0.15
//                                 + popularity * 0.10
//                                 + budgetScore * 0.05
//                                 + optionBonus
//                                 + (primarySlot ? 0.05 : 0.0);
//         }

//         // ==========================================================
//         // RESTAURANT SELECTION
//         // ==========================================================

//         private Restaurant findBestRestaurantForSlot(
//                         List<Restaurant> restaurants,
//                         Set<Long> usedIds,
//                         LocalTime slotStart,
//                         LocalTime slotEnd,
//                         double previousLat,
//                         double previousLng,
//                         boolean previousCoordinatesAvailable,
//                         int optionNumber) {

//                 if (restaurants == null || restaurants.isEmpty()) {
//                         return null;
//                 }

//                 List<ScoredCandidate<Restaurant>> candidates = new ArrayList<>();

//                 for (Restaurant restaurant : restaurants) {
//                         if (restaurant == null || restaurant.getId() == null || usedIds.contains(restaurant.getId())) {
//                                 continue;
//                         }

//                         LocalTime possibleStart = slotStart;
//                         LocalTime opening = getTimeProperty(restaurant, "openingTime", "openTime", "openingHour");
//                         if (opening != null && opening.isAfter(possibleStart)) {
//                                 possibleStart = opening;
//                         }

//                         LocalTime possibleEnd = possibleStart.plusMinutes(RESTAURANT_DURATION_MINUTES);
//                         if (possibleEnd.isAfter(slotEnd)
//                                         || !isOpenForSlot(restaurant, possibleStart, possibleEnd)) {
//                                 continue;
//                         }

//                         double distance = distanceKm(
//                                         previousCoordinatesAvailable,
//                                         previousLat,
//                                         previousLng,
//                                         getCoordinate(restaurant, "latitude"),
//                                         getCoordinate(restaurant, "longitude"));

//                         if (previousCoordinatesAvailable && hasCoordinates(restaurant)
//                                         && distance > RESTAURANT_MAX_DISTANCE_KM) {
//                                 continue;
//                         }

//                         double score = scoreRestaurant(
//                                         restaurant,
//                                         distance,
//                                         optionNumber);
//                         candidates.add(new ScoredCandidate<>(restaurant, score));
//                 }

//                 return candidates.stream()
//                                 .sorted(Comparator.comparingDouble(ScoredCandidate<Restaurant>::score).reversed())
//                                 .map(ScoredCandidate::value)
//                                 .findFirst()
//                                 .orElse(null);
//         }

//         private double scoreRestaurant(
//                         Restaurant restaurant,
//                         double distance,
//                         int optionNumber) {

//                 double rating = normalize(getNumericProperty(restaurant, "averageRating"), 5.0);
//                 double weight = inverseWeight(getNumericProperty(restaurant, "restaurantWeight"));
//                 double distanceScore = distanceScore(distance, RESTAURANT_MAX_DISTANCE_KM);
//                 double costScore = budgetFriendliness(restaurant.getAverageCostPerPerson());
//                 double cuisineScore = hasTextProperty(restaurant, "cuisineType") ? 1.0 : 0.5;
//                 double availabilityScore = isOpenNow(restaurant) ? 1.0 : 0.5;

//                 double optionScore;
//                 if (optionNumber == 1) {
//                         optionScore = rating * 0.20;
//                 } else if (optionNumber == 2) {
//                         optionScore = costScore * 0.20;
//                 } else if (optionNumber == 3) {
//                         optionScore = rating * 0.10 + costScore * 0.10;
//                 } else if (optionNumber == 4) {
//                         optionScore = weight * 0.20;
//                 } else {
//                         optionScore = distanceScore * 0.20 + cuisineScore * 0.05;
//                 }

//                 return weight * 0.30
//                                 + distanceScore * 0.30
//                                 + rating * 0.15
//                                 + costScore * 0.10
//                                 + availabilityScore * 0.05
//                                 + optionScore;
//         }

//         // ==========================================================
//         // HOTEL SELECTION
//         // ==========================================================

//         private Hotel findBestHotelForEndOfDay(
//                         List<Hotel> hotels,
//                         Set<Long> usedIds,
//                         double previousLat,
//                         double previousLng,
//                         boolean previousCoordinatesAvailable,
//                         Itinerary itinerary,
//                         int optionNumber) {

//                 if (hotels == null || hotels.isEmpty()) {
//                         return null;
//                 }

//                 List<ScoredCandidate<Hotel>> candidates = new ArrayList<>();
//                 BigDecimal perDayBudget = safeBigDecimal(itinerary.getTotalBudget())
//                                 .multiply(HOTEL_BUDGET_PERCENTAGE)
//                                 .divide(BigDecimal.valueOf(Math.max(1, safeTotalDays(itinerary))), 2,
//                                                 RoundingMode.HALF_UP);

//                 for (Hotel hotel : hotels) {
//                         if (hotel == null || hotel.getId() == null || usedIds.contains(hotel.getId())) {
//                                 continue;
//                         }

//                         if (hotel.getPricePerNight() == null) {
//                                 continue;
//                         }

//                         double distance = distanceKm(
//                                         previousCoordinatesAvailable,
//                                         previousLat,
//                                         previousLng,
//                                         getCoordinate(hotel, "latitude"),
//                                         getCoordinate(hotel, "longitude"));

//                         if (previousCoordinatesAvailable && hasCoordinates(hotel)
//                                         && distance > HOTEL_MAX_DISTANCE_KM) {
//                                 continue;
//                         }

//                         double budgetScore = hotel.getPricePerNight().compareTo(perDayBudget) <= 0
//                                         ? 1.0
//                                         : 0.25;
//                         double rating = normalize(getNumericProperty(hotel, "averageRating"), 5.0);
//                         double weight = inverseWeight(getNumericProperty(hotel, "hotelWeight"));
//                         double distanceScore = distanceScore(distance, HOTEL_MAX_DISTANCE_KM);

//                         double optionScore = switch (optionNumber) {
//                                 case 1 -> rating * 0.20;
//                                 case 2 -> budgetScore * 0.20;
//                                 case 3 -> normalize(hotel.getPricePerNight().doubleValue(),
//                                                 Math.max(1.0, perDayBudget.doubleValue() * 2.0)) * 0.20;
//                                 case 4 -> weight * 0.20;
//                                 default -> distanceScore * 0.20;
//                         };

//                         double score = weight * 0.30
//                                         + distanceScore * 0.35
//                                         + rating * 0.15
//                                         + budgetScore * 0.10
//                                         + optionScore;

//                         candidates.add(new ScoredCandidate<>(hotel, score));
//                 }

//                 // If strict distance filtering removed every hotel, use the closest
//                 // available hotel rather than leaving the day without accommodation.
//                 if (candidates.isEmpty()) {
//                         for (Hotel hotel : hotels) {
//                                 if (hotel == null || hotel.getId() == null || usedIds.contains(hotel.getId())) {
//                                         continue;
//                                 }
//                                 if (hotel.getPricePerNight() == null) {
//                                         continue;
//                                 }
//                                 double distance = distanceKm(
//                                                 previousCoordinatesAvailable,
//                                                 previousLat,
//                                                 previousLng,
//                                                 getCoordinate(hotel, "latitude"),
//                                                 getCoordinate(hotel, "longitude"));
//                                 double score = distanceScore(distance, 20.0)
//                                                 + inverseWeight(getNumericProperty(hotel, "hotelWeight"));
//                                 candidates.add(new ScoredCandidate<>(hotel, score));
//                         }
//                 }

//                 return candidates.stream()
//                                 .sorted(Comparator.comparingDouble(ScoredCandidate<Hotel>::score).reversed())
//                                 .map(ScoredCandidate::value)
//                                 .findFirst()
//                                 .orElse(null);
//         }

//         // ==========================================================
//         // ITINERARY PLACE BUILDERS
//         // ==========================================================

//         private ItineraryPlace buildTouristPlaceItineraryPlace(
//                         ItineraryDay day,
//                         TouristPlace place,
//                         int placeIndex,
//                         LocalTime startTime,
//                         LocalTime endTime,
//                         int travelTimeMinutes,
//                         BigDecimal distanceKm) {

//                 return ItineraryPlace.builder()
//                                 .itineraryDay(day)
//                                 .placeType(PlaceType.TOURIST_PLACE)
//                                 .referenceId(place.getId())
//                                 .visitOrder(placeIndex)
//                                 .plannedStartTime(startTime)
//                                 .plannedEndTime(endTime)
//                                 .estimatedCost(
//                                                 place.getPrice() != null
//                                                                 ? place.getPrice()
//                                                                 : BigDecimal.ZERO)
//                                 .travelTimeMinutes(travelTimeMinutes)
//                                 .distanceKm(distanceKm)
//                                 .notes(place.getDescription())
//                                 .completed(false)
//                                 .build();
//         }

//         private ItineraryPlace buildRestaurantItineraryPlace(
//                         ItineraryDay day,
//                         Restaurant restaurant,
//                         int visitOrder,
//                         LocalTime startTime,
//                         BigDecimal distanceKm,
//                         int travelTimeMinutes) {

//                 LocalTime endTime = startTime.plusHours(1);

//                 return ItineraryPlace.builder()
//                                 .itineraryDay(day)
//                                 .placeType(PlaceType.RESTAURANT)
//                                 .referenceId(restaurant.getId())
//                                 .visitOrder(visitOrder)
//                                 .plannedStartTime(startTime)
//                                 .plannedEndTime(endTime)
//                                 .estimatedCost(
//                                                 restaurant.getAverageCostPerPerson() != null
//                                                                 ? restaurant.getAverageCostPerPerson()
//                                                                 : BigDecimal.ZERO)
//                                 .travelTimeMinutes(travelTimeMinutes)
//                                 .distanceKm(distanceKm)
//                                 .completed(false)
//                                 .notes(
//                                                 "Restaurant: "
//                                                                 + restaurant.getRestaurantName())
//                                 .build();
//         }

//         private ItineraryPlace buildHotelItineraryPlace(
//                         ItineraryDay day,
//                         Hotel hotel,
//                         int visitOrder,
//                         LocalTime startTime,
//                         BigDecimal distanceKm,
//                         int travelTimeMinutes) {

//                 LocalTime endTime = startTime.plusHours(1);

//                 return ItineraryPlace.builder()
//                                 .itineraryDay(day)
//                                 .placeType(PlaceType.HOTEL)
//                                 .referenceId(hotel.getId())
//                                 .visitOrder(visitOrder)
//                                 .plannedStartTime(startTime)
//                                 .plannedEndTime(endTime)
//                                 .estimatedCost(
//                                                 hotel.getPricePerNight() != null
//                                                                 ? hotel.getPricePerNight()
//                                                                 : BigDecimal.ZERO)
//                                 .travelTimeMinutes(travelTimeMinutes)
//                                 .distanceKm(distanceKm)
//                                 .completed(false)
//                                 .notes(
//                                                 "Hotel: "
//                                                                 + hotel.getHotelName())
//                                 .build();
//         }

//         // ==========================================================
//         // TIME / DISTANCE / OPENING-HOURS HELPERS
//         // ==========================================================

//         private int getVisitMinutes(TouristPlace place) {
//                 return place.getEstimatedVisitTimeMinutes() != null
//                                 ? Math.max(15, place.getEstimatedVisitTimeMinutes())
//                                 : DEFAULT_VISIT_MINUTES;
//         }

//         private int calculateTravelTimeMinutes(double distanceKm) {

//                 if (distanceKm <= 0.0) {
//                         return 0;
//                 }

//                 double minutes = (distanceKm / DEFAULT_AVERAGE_SPEED_KM_PER_HOUR) * 60.0;

//                 return Math.max(5, (int) Math.ceil(minutes));
//         }

//         private BigDecimal toDistanceBigDecimal(double distanceKm) {
//                 return BigDecimal.valueOf(Math.max(0.0, distanceKm))
//                                 .setScale(2, RoundingMode.HALF_UP);
//         }

//         private boolean isOpenForSlot(
//                         Object entity,
//                         LocalTime start,
//                         LocalTime end) {

//                 LocalTime opening = getTimeProperty(entity, "openingTime", "openTime", "openingHour");
//                 LocalTime closing = getTimeProperty(entity, "closingTime", "closeTime", "closingHour");

//                 if (opening == null || closing == null) {
//                         return true;
//                 }

//                 // Normal same-day opening hours.
//                 if (closing.isAfter(opening)) {
//                         return !start.isBefore(opening) && !end.isAfter(closing);
//                 }

//                 // Overnight hours, e.g. 18:00 -> 02:00.
//                 return !start.isBefore(opening) || !end.isAfter(closing);
//         }

//         private boolean isOpenNow(Object entity) {
//                 LocalTime now = LocalTime.now();
//                 return isOpenForSlot(entity, now, now.plusMinutes(1));
//         }

//         private double distanceKm(
//                         boolean fromAvailable,
//                         double lat1,
//                         double lon1,
//                         double lat2,
//                         double lon2) {

//                 if (!fromAvailable || (lat2 == 0.0 && lon2 == 0.0)) {
//                         return 0.0;
//                 }

//                 final double earthRadiusKm = 6371.0;
//                 double latDistance = Math.toRadians(lat2 - lat1);
//                 double lonDistance = Math.toRadians(lon2 - lon1);

//                 double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
//                                 + Math.cos(Math.toRadians(lat1))
//                                                 * Math.cos(Math.toRadians(lat2))
//                                                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

//                 double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//                 return earthRadiusKm * c;
//         }

//         private double distanceScore(double distance, double maxDistance) {
//                 if (distance <= 0.0)
//                         return 1.0;
//                 return Math.max(0.0, 1.0 - (distance / maxDistance));
//         }

//         private double budgetFriendliness(BigDecimal cost) {
//                 if (cost == null || cost.signum() <= 0)
//                         return 1.0;
//                 return 1.0 / (1.0 + cost.doubleValue() / 1000.0);
//         }

//         private double inverseWeight(double weight) {
//                 if (weight <= 0.0)
//                         return 0.5;
//                 return 1.0 / (1.0 + weight);
//         }

//         private double normalize(double value, double max) {
//                 if (max <= 0.0)
//                         return 0.0;
//                 return Math.max(0.0, Math.min(1.0, value / max));
//         }

//         private double getNumericProperty(Object entity, String... names) {
//                 if (entity == null)
//                         return 0.0;

//                 for (String name : names) {
//                         Object value = invokeGetter(entity, name);
//                         if (value instanceof Number number) {
//                                 return number.doubleValue();
//                         }
//                         if (value != null) {
//                                 try {
//                                         return Double.parseDouble(value.toString().trim());
//                                 } catch (Exception ignored) {
//                                 }
//                         }
//                 }
//                 return 0.0;
//         }

//         private double getCoordinate(Object entity, String axis) {
//                 return getNumericProperty(entity, axis);
//         }

//         private boolean hasCoordinates(Object entity) {
//                 if (entity == null)
//                         return false;
//                 Object lat = invokeGetter(entity, "latitude");
//                 Object lng = invokeGetter(entity, "longitude");
//                 if (lat == null || lng == null)
//                         return false;
//                 try {
//                         double la = Double.parseDouble(lat.toString());
//                         double lo = Double.parseDouble(lng.toString());
//                         return !(la == 0.0 && lo == 0.0);
//                 } catch (Exception e) {
//                         return false;
//                 }
//         }

//         private LocalTime getTimeProperty(Object entity, String... names) {
//                 if (entity == null)
//                         return null;

//                 for (String name : names) {
//                         Object value = invokeGetter(entity, name);
//                         if (value == null)
//                                 continue;
//                         if (value instanceof LocalTime time)
//                                 return time;
//                         try {
//                                 String text = value.toString().trim();
//                                 if (text.length() == 5)
//                                         return LocalTime.parse(text);
//                                 if (text.length() >= 8)
//                                         return LocalTime.parse(text.substring(0, 8));
//                         } catch (Exception ignored) {
//                         }
//                 }
//                 return null;
//         }

//         private boolean hasTextProperty(Object entity, String name) {
//                 Object value = invokeGetter(entity, name);
//                 return value != null && !value.toString().trim().isEmpty();
//         }

//         private Object invokeGetter(Object entity, String property) {
//                 if (entity == null || property == null || property.isBlank())
//                         return null;

//                 String suffix = Character.toUpperCase(property.charAt(0)) + property.substring(1);
//                 String getterName = "get" + suffix;

//                 try {
//                         Method method = entity.getClass().getMethod(getterName);
//                         return method.invoke(entity);
//                 } catch (Exception ignored) {
//                         return null;
//                 }
//         }

//         private static final class MealWindow {
//                 private final LocalTime start;
//                 private final LocalTime end;
//                 private final String name;

//                 private MealWindow(LocalTime start, LocalTime end, String name) {
//                         this.start = start;
//                         this.end = end;
//                         this.name = name;
//                 }
//         }

//         private static final class ScoredCandidate<T> {
//                 private final T value;
//                 private final double score;

//                 private ScoredCandidate(T value, double score) {
//                         this.value = value;
//                         this.score = score;
//                 }

//                 private T value() {
//                         return value;
//                 }

//                 private double score() {
//                         return score;
//                 }
//         }

//         private LocalTime getDefaultStartTime() {
//                 return LocalTime.of(DAY_START_HOUR, 0);
//         }

//         // ==========================================================
//         // SAFE TOTAL DAYS
//         // ==========================================================

//         private int safeTotalDays(
//                         Itinerary itinerary) {

//                 if (itinerary == null
//                                 || itinerary.getTotalDays() == null) {

//                         return 0;
//                 }

//                 return Math.max(
//                                 0,
//                                 itinerary.getTotalDays());
//         }
//         // ==========================================================
//         // SAFE BIG DECIMAL
//         // ==========================================================

//         private BigDecimal safeBigDecimal(
//                         BigDecimal value) {

//                 return value != null
//                                 ? value
//                                 : BigDecimal.ZERO;
//         }
//         // ==========================================================
//         // SAFE RESTAURANT NAME
//         // ==========================================================

//         private String getRestaurantName(
//                         Restaurant restaurant) {

//                 if (restaurant == null) {
//                         return "Restaurant";
//                 }

//                 String name = restaurant.getRestaurantName();

//                 if (name == null || name.trim().isEmpty()) {
//                         return "Restaurant";
//                 }

//                 return name.trim();
//         }

//         // ==========================================================
//         // HOTEL SELECTION
//         // ==========================================================

//         private List<Hotel> assignHotelsForOption(
//                         Itinerary itinerary,
//                         List<Hotel> hotels,
//                         int optionNumber) {

//                 if (hotels == null ||
//                                 hotels.isEmpty()) {

//                         return new ArrayList<>();
//                 }

//                 BigDecimal totalBudget = itinerary.getTotalBudget();

//                 BigDecimal hotelBudget = totalBudget.multiply(
//                                 HOTEL_BUDGET_PERCENTAGE);

//                 BigDecimal perDayBudget = hotelBudget.divide(
//                                 BigDecimal.valueOf(
//                                                 itinerary.getTotalDays()),
//                                 2,
//                                 java.math.RoundingMode.HALF_UP);

//                 // ======================================================
//                 // STRICT BUDGET FILTER
//                 // ======================================================

//                 List<Hotel> filtered = hotels.stream()
//                                 .filter(
//                                                 hotel -> hotel.getPricePerNight() != null)
//                                 .filter(
//                                                 hotel -> hotel.getPricePerNight()
//                                                                 .compareTo(
//                                                                                 perDayBudget) <= 0)
//                                 .toList();

//                 // ======================================================
//                 // FALLBACK
//                 // ======================================================
//                 //
//                 // Low budget ke wajah se zero hotel nahi.
//                 // Cheapest available hotels use karo.
//                 //
//                 // ======================================================

//                 if (filtered.isEmpty()) {

//                         log.warn(
//                                         "No hotel within budget {}. Using cheapest hotels.",
//                                         perDayBudget);

//                         filtered = hotels.stream()
//                                         .filter(
//                                                         hotel -> hotel.getPricePerNight() != null)
//                                         .sorted(
//                                                         Comparator.comparing(
//                                                                         Hotel::getPricePerNight))
//                                         .toList();
//                 }

//                 // ======================================================
//                 // OPTION 1 = BEST RATING
//                 // ======================================================

//                 if (optionNumber == 1) {

//                         return filtered.stream()
//                                         .sorted(
//                                                         Comparator.comparing(
//                                                                         Hotel::getAverageRating,
//                                                                         Comparator.nullsLast(
//                                                                                         Comparator.reverseOrder())))
//                                         .toList();
//                 }

//                 // ======================================================
//                 // OPTION 2 = CHEAPEST
//                 // ======================================================

//                 if (optionNumber == 2) {

//                         return filtered.stream()
//                                         .sorted(
//                                                         Comparator.comparing(
//                                                                         Hotel::getPricePerNight))
//                                         .toList();
//                 }

//                 // ======================================================
//                 // OPTION 3 = PREMIUM
//                 // ======================================================

//                 return filtered.stream()
//                                 .sorted(
//                                                 Comparator.comparing(
//                                                                 Hotel::getPricePerNight,
//                                                                 Comparator.reverseOrder()))
//                                 .toList();
//         }

//         // ==========================================================
//         // RESTAURANT SELECTION
//         // ==========================================================

//         // ==========================================================
//         // RESTAURANT SELECTION
//         // ==========================================================

//         private List<Restaurant> assignRestaurantsForOption(
//                         Itinerary itinerary,
//                         List<Restaurant> restaurants,
//                         int optionNumber) {

//                 if (itinerary == null
//                                 || restaurants == null
//                                 || restaurants.isEmpty()) {

//                         return new ArrayList<>();
//                 }

//                 int totalDays = safeTotalDays(itinerary);

//                 if (totalDays <= 0) {
//                         return new ArrayList<>();
//                 }

//                 int restaurantsPerDay = itinerary.getSafeRestaurantsPerDay();

//                 int totalRestaurantsRequired = totalDays * restaurantsPerDay;

//                 if (totalRestaurantsRequired <= 0) {
//                         return new ArrayList<>();
//                 }

//                 BigDecimal totalBudget = safeBigDecimal(
//                                 itinerary.getTotalBudget());

//                 BigDecimal restaurantBudget = totalBudget.multiply(
//                                 RESTAURANT_BUDGET_PERCENTAGE);

//                 BigDecimal perMealBudget = restaurantBudget.divide(
//                                 BigDecimal.valueOf(
//                                                 totalRestaurantsRequired),
//                                 2,
//                                 java.math.RoundingMode.HALF_UP);

//                 // ======================================================
//                 // STRICT BUDGET FILTER
//                 // ======================================================

//                 List<Restaurant> filtered = restaurants.stream()
//                                 .filter(Objects::nonNull)
//                                 .filter(restaurant -> restaurant.getAverageCostPerPerson() != null)
//                                 .filter(restaurant -> restaurant
//                                                 .getAverageCostPerPerson()
//                                                 .signum() >= 0)
//                                 .filter(restaurant -> restaurant
//                                                 .getAverageCostPerPerson()
//                                                 .compareTo(perMealBudget) <= 0)
//                                 .toList();

//                 // ======================================================
//                 // FALLBACK
//                 // ======================================================

//                 if (filtered.isEmpty()) {

//                         log.warn(
//                                         "No restaurant found within per-meal budget {}. "
//                                                         + "Using cheapest restaurants.",
//                                         perMealBudget);

//                         filtered = restaurants.stream()
//                                         .filter(Objects::nonNull)
//                                         .filter(restaurant -> restaurant.getAverageCostPerPerson() != null)
//                                         .filter(restaurant -> restaurant
//                                                         .getAverageCostPerPerson()
//                                                         .signum() >= 0)
//                                         .sorted(
//                                                         Comparator.comparing(
//                                                                         Restaurant::getAverageCostPerPerson))
//                                         .toList();
//                 }

//                 if (filtered.isEmpty()) {
//                         return new ArrayList<>();
//                 }

//                 int resultLimit = Math.min(
//                                 totalRestaurantsRequired,
//                                 filtered.size());

//                 // ======================================================
//                 // OPTION 1 = BEST RATING
//                 // ======================================================

//                 if (optionNumber == 1) {

//                         return filtered.stream()
//                                         .sorted(
//                                                         Comparator.comparing(
//                                                                         Restaurant::getAverageRating,
//                                                                         Comparator.nullsLast(
//                                                                                         Comparator.reverseOrder())))
//                                         .limit(resultLimit)
//                                         .toList();
//                 }

//                 // ======================================================
//                 // OPTION 2 = CHEAPEST
//                 // ======================================================

//                 if (optionNumber == 2) {

//                         return filtered.stream()
//                                         .sorted(
//                                                         Comparator.comparing(
//                                                                         Restaurant::getAverageCostPerPerson))
//                                         .limit(resultLimit)
//                                         .toList();
//                 }

//                 // ======================================================
//                 // OPTION 3 = PREMIUM
//                 // ======================================================

//                 return filtered.stream()
//                                 .sorted(
//                                                 Comparator.comparing(
//                                                                 Restaurant::getAverageCostPerPerson,
//                                                                 Comparator.reverseOrder()))
//                                 .limit(resultLimit)
//                                 .toList();
//         }

//         // ==========================================================
//         // SORT TOURIST PLACES FOR OPTION
//         // ==========================================================

//         private List<TouristPlace> sortTouristPlacesForOption(
//                         List<TouristPlace> places,
//                         int optionNumber) {

//                 if (places == null ||
//                                 places.isEmpty()) {

//                         return new ArrayList<>();
//                 }

//                 // ======================================================
//                 // OPTION 1 = RATING + POPULARITY
//                 // ======================================================

//                 if (optionNumber == 1) {

//                         return places.stream()
//                                         .sorted(
//                                                         Comparator
//                                                                         .comparing(
//                                                                                         TouristPlace::getAverageRating,
//                                                                                         Comparator.nullsLast(
//                                                                                                         Comparator.reverseOrder()))
//                                                                         .thenComparing(
//                                                                                         TouristPlace::getPopularityScore,
//                                                                                         Comparator.nullsLast(
//                                                                                                         Comparator.reverseOrder())))
//                                         .toList();
//                 }

//                 // ======================================================
//                 // OPTION 2 = POPULARITY
//                 // ======================================================

//                 if (optionNumber == 2) {

//                         return places.stream()
//                                         .sorted(
//                                                         Comparator
//                                                                         .comparing(
//                                                                                         TouristPlace::getPopularityScore,
//                                                                                         Comparator.nullsLast(
//                                                                                                         Comparator.reverseOrder()))
//                                                                         .thenComparing(
//                                                                                         TouristPlace::getAverageRating,
//                                                                                         Comparator.nullsLast(
//                                                                                                         Comparator.reverseOrder())))
//                                         .toList();
//                 }

//                 // ======================================================
//                 // OPTION 3 = BUDGET FRIENDLY
//                 // ======================================================

//                 return places.stream()
//                                 .sorted(
//                                                 Comparator
//                                                                 .comparing(
//                                                                                 TouristPlace::getPrice,
//                                                                                 Comparator.nullsLast(
//                                                                                                 Comparator.naturalOrder()))
//                                                                 .thenComparing(
//                                                                                 TouristPlace::getAverageRating,
//                                                                                 Comparator.nullsLast(
//                                                                                                 Comparator.reverseOrder())))
//                                 .toList();
//         }

//         // ==========================================================
//         // FETCH TOURIST PLACES
//         // ==========================================================

//         private List<TouristPlace> fetchTouristPlaces(
//                         Location location) {

//                 List<TouristPlace> places = touristPlaceRepository
//                                 .findByLocationIdAndActiveTrue(
//                                                 location.getId());

//                 if (places == null ||
//                                 places.isEmpty()) {

//                         log.warn(
//                                         "No tourist places found for location={}",
//                                         location.getCityName());

//                         return new ArrayList<>();
//                 }

//                 return places;
//         }

//         // ==========================================================
//         // FILTER TOURIST PLACES
//         // ==========================================================

//         private List<TouristPlace> filterTouristPlaces(
//                         List<TouristPlace> places,
//                         Itinerary itinerary) {

//                 if (places == null ||
//                                 places.isEmpty()) {

//                         return new ArrayList<>();
//                 }

//                 BigDecimal totalBudget = itinerary.getTotalBudget();

//                 BigDecimal placesBudget = totalBudget.multiply(
//                                 PLACES_BUDGET_PERCENTAGE);

//                 // ======================================================
//                 // ACTIVE
//                 // ======================================================

//                 List<TouristPlace> activePlaces = places.stream()
//                                 .filter(
//                                                 place -> Boolean.TRUE.equals(
//                                                                 place.getActive()))
//                                 .toList();

//                 // ======================================================
//                 // STRICT BUDGET FILTER
//                 // ======================================================

//                 List<TouristPlace> budgetPlaces = activePlaces.stream()
//                                 .filter(
//                                                 place -> place.getPrice() == null
//                                                                 ||
//                                                                 place.getPrice()
//                                                                                 .compareTo(
//                                                                                                 placesBudget) <= 0)
//                                 .toList();

//                 // ======================================================
//                 // TRAVEL TYPE
//                 // ======================================================

//                 List<TouristPlace> travelTypePlaces = budgetPlaces.stream()
//                                 .filter(
//                                                 place -> filterByTravelType(
//                                                                 place,
//                                                                 itinerary))
//                                 .toList();

//                 // ======================================================
//                 // SEASON
//                 // ======================================================

//                 List<TouristPlace> seasonPlaces = travelTypePlaces.stream()
//                                 .filter(
//                                                 place -> filterByBestSeason(
//                                                                 place,
//                                                                 itinerary))
//                                 .toList();

//                 // ======================================================
//                 // IF STRICT FILTER RETURNS RESULTS
//                 // ======================================================

//                 if (!seasonPlaces.isEmpty()) {

//                         int maxPlaces = MAX_PLACES_PER_DAY
//                                         * itinerary.getTotalDays();

//                         return seasonPlaces.stream()
//                                         .limit(maxPlaces)
//                                         .toList();
//                 }

//                 // ======================================================
//                 // FALLBACK
//                 // ======================================================
//                 //
//                 // Budget too low hone par itinerary empty nahi hogi.
//                 //
//                 // Budget filter remove kar diya.
//                 //
//                 // ======================================================

//                 log.warn(
//                                 "No tourist place matched strict budget {}. "
//                                                 + "Applying fallback filtering.",
//                                 placesBudget);

//                 List<TouristPlace> fallback = activePlaces.stream()
//                                 .filter(
//                                                 place -> filterByTravelType(
//                                                                 place,
//                                                                 itinerary))
//                                 .filter(
//                                                 place -> filterByBestSeason(
//                                                                 place,
//                                                                 itinerary))
//                                 .toList();

//                 // ======================================================
//                 // SECOND FALLBACK
//                 // ======================================================
//                 //
//                 // Agar season ke wajah se bhi zero hain,
//                 // active places use karo.
//                 //
//                 // ======================================================

//                 if (fallback.isEmpty()) {

//                         fallback = new ArrayList<>(
//                                         activePlaces);
//                 }

//                 int maxPlaces = MAX_PLACES_PER_DAY
//                                 * itinerary.getTotalDays();

//                 return fallback.stream()
//                                 .limit(maxPlaces)
//                                 .toList();
//         }

//         // ==========================================================
//         // TRAVEL TYPE FILTER
//         // ==========================================================

//         private boolean filterByTravelType(
//                         TouristPlace place,
//                         Itinerary itinerary) {

//                 if (place.getTravelTypes() == null ||
//                                 place.getTravelTypes().isEmpty()) {

//                         return true;
//                 }

//                 if (itinerary.getTravelType() == null) {
//                         return true;
//                 }

//                 String requestedTravelType = itinerary.getTravelType()
//                                 .name()
//                                 .trim()
//                                 .toUpperCase();

//                 return place.getTravelTypes()
//                                 .stream()
//                                 .filter(Objects::nonNull)
//                                 .map(String::trim)
//                                 .map(String::toUpperCase)
//                                 .anyMatch(type -> type.equals(requestedTravelType));
//         }

//         // ==========================================================
//         // BEST SEASON FILTER
//         // ==========================================================

//         private boolean filterByBestSeason(
//                         TouristPlace place,
//                         Itinerary itinerary) {

//                 String bestVisitMonths = place.getBestVisitMonths();

//                 if (bestVisitMonths == null ||
//                                 bestVisitMonths.trim().isEmpty()) {

//                         return true;
//                 }

//                 LocalDate startDate = itinerary.getStartDate();

//                 if (startDate == null) {
//                         startDate = LocalDate.now();
//                 }

//                 Month tripMonth = startDate.getMonth();

//                 String tripMonthName = tripMonth.name();

//                 String value = bestVisitMonths.trim();

//                 // ======================================================
//                 // COMMA SEPARATED
//                 // ======================================================

//                 if (value.contains(",")) {

//                         for (String month : value.split(",")) {

//                                 if (month.trim()
//                                                 .equalsIgnoreCase(
//                                                                 tripMonthName)) {

//                                         return true;
//                                 }
//                         }

//                         return false;
//                 }

//                 // ======================================================
//                 // MONTH RANGE
//                 // ======================================================

//                 if (value.toLowerCase()
//                                 .contains(" to ")) {

//                         String[] parts = value.split(
//                                         "(?i)\\s+to\\s+");

//                         if (parts.length == 2) {

//                                 try {

//                                         int startMonth = Month.valueOf(
//                                                         parts[0]
//                                                                         .trim()
//                                                                         .toUpperCase())
//                                                         .getValue();

//                                         int endMonth = Month.valueOf(
//                                                         parts[1]
//                                                                         .trim()
//                                                                         .toUpperCase())
//                                                         .getValue();

//                                         int tripMonthNumber = tripMonth.getValue();

//                                         if (startMonth <= endMonth) {

//                                                 return tripMonthNumber >= startMonth
//                                                                 &&
//                                                                 tripMonthNumber <= endMonth;
//                                         }

//                                         // Cross-year range
//                                         return tripMonthNumber >= startMonth
//                                                         ||
//                                                         tripMonthNumber <= endMonth;

//                                 } catch (IllegalArgumentException exception) {

//                                         log.warn(
//                                                         "Invalid best visit month value: {}",
//                                                         value);

//                                         return true;
//                                 }
//                         }
//                 }

//                 // ======================================================
//                 // SINGLE MONTH
//                 // ======================================================

//                 return value.equalsIgnoreCase(
//                                 tripMonthName);
//         }

//         // ==========================================================
//         // DAY TITLE
//         // ==========================================================

//         private String generateDayTitle(
//                         int dayNumber) {

//                 return switch (dayNumber) {

//                         case 1 ->
//                                 "Arrival & Local Sightseeing";

//                         case 2 ->
//                                 "Explore Famous Attractions";

//                         case 3 ->
//                                 "Adventure & Outdoor Activities";

//                         case 4 ->
//                                 "Cultural & Heritage Tour";

//                         case 5 ->
//                                 "Shopping & Food Experience";

//                         default ->
//                                 "Day "
//                                                 + dayNumber
//                                                 + " - Exploration";
//                 };
//         }

//         // ==========================================================
//         // BUILD ITINERARY
//         // ==========================================================

//         private void buildItinerary(
//                         Itinerary itinerary,
//                         ItineraryRequest request,
//                         User user,
//                         Location location) {

//                 itinerary.setUser(
//                                 user);

//                 itinerary.setLocation(
//                                 location);

//                 itinerary.setTitle(
//                                 request.getTitle().trim());

//                 itinerary.setDescription(
//                                 request.getDescription());

//                 itinerary.setTravelType(
//                                 request.getTravelType());

//                 itinerary.setTotalDays(
//                                 request.getTotalDays());

//                 itinerary.setTotalBudget(
//                                 request.getTotalBudget());

//                 itinerary.setRestaurantsPerDay(
//                                 request.getRestaurantsPerDay() != null
//                                                 ? Math.max(
//                                                                 1,
//                                                                 request.getRestaurantsPerDay())
//                                                 : 1);
//                 // IMPORTANT:
//                 // Request estimatedCost is ignored during generation.
//                 // Backend calculates actual estimated cost.
//                 itinerary.setEstimatedCost(
//                                 BigDecimal.ZERO);

//                 itinerary.setRemainingBudget(
//                                 request.getTotalBudget());

//                 itinerary.setItineraryStatus(
//                                 ItineraryStatus.GENERATED);

//                 itinerary.setStartDate(
//                                 request.getStartDate());

//                 itinerary.setEndDate(
//                                 request.getEndDate());

//                 itinerary.setItineraryDays(
//                                 new ArrayList<>());
//         }

//         // ==========================================================
//         // COST CALCULATION
//         // ==========================================================

//         // ==========================================================
//         // COST CALCULATION
//         // ==========================================================

//         private BigDecimal calculateTotalEstimatedCost(
//                         Itinerary itinerary) {

//                 if (itinerary == null) {
//                         return BigDecimal.ZERO;
//                 }

//                 List<ItineraryDay> days = itinerary.getItineraryDays();

//                 if (days == null || days.isEmpty()) {
//                         return BigDecimal.ZERO;
//                 }

//                 BigDecimal totalCost = BigDecimal.ZERO;

//                 for (ItineraryDay day : days) {

//                         if (day == null) {
//                                 continue;
//                         }

//                         List<ItineraryPlace> places = day.getItineraryPlaces();

//                         if (places == null || places.isEmpty()) {
//                                 continue;
//                         }

//                         for (ItineraryPlace place : places) {

//                                 if (place == null) {
//                                         continue;
//                                 }

//                                 BigDecimal cost = place.getEstimatedCost();

//                                 if (cost == null
//                                                 || cost.signum() <= 0) {
//                                         continue;
//                                 }

//                                 totalCost = totalCost.add(cost);
//                         }
//                 }

//                 return totalCost;
//         }

//         // ==========================================================
//         // REMAINING BUDGET
//         // ==========================================================

//         private BigDecimal calculateRemainingBudget(
//                         Itinerary itinerary) {

//                 if (itinerary.getTotalBudget() == null) {

//                         return BigDecimal.ZERO;
//                 }

//                 BigDecimal estimatedCost = itinerary.getEstimatedCost() != null
//                                 ? itinerary.getEstimatedCost()
//                                 : BigDecimal.ZERO;

//                 return itinerary
//                                 .getTotalBudget()
//                                 .subtract(
//                                                 estimatedCost);
//         }

//         // ==========================================================
//         // AUTOMATIC STATUS
//         // ==========================================================

//         private void updateStatusAutomatically(
//                         Itinerary itinerary) {

//                 if (itinerary.getTotalBudget() == null ||
//                                 itinerary.getEstimatedCost() == null) {

//                         itinerary.setItineraryStatus(
//                                         ItineraryStatus.GENERATED);

//                         return;
//                 }

//                 if (itinerary.getTotalBudget()
//                                 .compareTo(BigDecimal.ZERO) <= 0) {

//                         itinerary.setItineraryStatus(
//                                         ItineraryStatus.GENERATED);

//                         return;
//                 }

//                 BigDecimal remainingBudget = calculateRemainingBudget(
//                                 itinerary);

//                 BigDecimal budgetRatio = remainingBudget.divide(
//                                 itinerary.getTotalBudget(),
//                                 4,
//                                 java.math.RoundingMode.HALF_UP);

//                 // ======================================================
//                 // MORE THAN 20% OVER BUDGET
//                 // ======================================================

//                 if (budgetRatio.compareTo(
//                                 BigDecimal.valueOf(-0.20)) < 0) {

//                         itinerary.setItineraryStatus(
//                                         ItineraryStatus.CONCERNED);

//                         return;
//                 }

//                 // ======================================================
//                 // SLIGHTLY OVER BUDGET
//                 // ======================================================

//                 if (budgetRatio.compareTo(
//                                 BigDecimal.ZERO) < 0) {

//                         itinerary.setItineraryStatus(
//                                         ItineraryStatus.PLANNED);

//                         return;
//                 }

//                 // ======================================================
//                 // WITHIN BUDGET
//                 // ======================================================

//                 itinerary.setItineraryStatus(
//                                 ItineraryStatus.GENERATED);
//         }

//         // ==========================================================
//         // GET ITINERARY BY ID
//         // ==========================================================

//         @Override
//         @Transactional(readOnly = true)
//         public ItineraryResponse getById(@NonNull Long id) {

//                 Itinerary itinerary = itineraryRepository
//                                 .findWithDetailsById(id)
//                                 .orElseThrow(() -> new ResourceNotFoundException(
//                                                 "Itinerary not found with id: " + id));

//                 validateOwnership(itinerary);

//                 return itineraryMapper.toResponse(itinerary);
//         }

//         // ==========================================================
//         // GET MY ITINERARIES
//         // ==========================================================

//         @Override
//         @Transactional(readOnly = true)
//         public List<ItineraryResponse> getMyItineraries() {

//                 Long userId = securityUtils.getCurrentUserId();

//                 return itineraryRepository
//                                 .findByUserIdOrderByCreatedAtDesc(
//                                                 userId)
//                                 .stream()
//                                 .map(
//                                                 itineraryMapper::toResponse)
//                                 .toList();
//         }

//         // ==========================================================
//         // GET ITINERARIES BY USER ID
//         // ==========================================================

//         @Override
//         @Transactional(readOnly = true)
//         public List<ItineraryResponse> getItineraryByUserId(
//                         Long userId) {

//                 return itineraryRepository
//                                 .findByUserIdOrderByCreatedAtDesc(
//                                                 userId)
//                                 .stream()
//                                 .map(
//                                                 itineraryMapper::toResponse)
//                                 .toList();
//         }

//         // ==========================================================
//         // USER SUMMARY
//         // ==========================================================

//         @Override
//         @Transactional(readOnly = true)
//         public UserSummaryResponse getUserSummaryByUserId(Long userId) {

//                 // =========================================================
//                 // 1. GET ALL SUMMARY AGGREGATES IN ONE QUERY
//                 // =========================================================

//                 UserSummaryProjection summary = itineraryRepository.getUserSummary(
//                                 userId,
//                                 ItineraryStatus.COMPLETED);

//                 // =========================================================
//                 // 2. SAFE DEFAULT VALUES
//                 // =========================================================

//                 long totalTrips = 0L;

//                 long completedTrips = 0L;

//                 BigDecimal totalBudget = BigDecimal.ZERO;

//                 BigDecimal usedBudget = BigDecimal.ZERO;

//                 if (summary != null) {

//                         if (summary.getTotalTrips() != null) {
//                                 totalTrips = summary.getTotalTrips();
//                         }

//                         if (summary.getCompletedTrips() != null) {
//                                 completedTrips = summary.getCompletedTrips();
//                         }

//                         if (summary.getTotalBudget() != null) {
//                                 totalBudget = summary.getTotalBudget();
//                         }

//                         if (summary.getUsedBudget() != null) {
//                                 usedBudget = summary.getUsedBudget();
//                         }
//                 }

//                 // =========================================================
//                 // 3. VISITED PLACES
//                 // =========================================================

//                 long placesVisited = itineraryPlaceRepository.countVisitedPlacesByUserId(
//                                 userId);

//                 // =========================================================
//                 // 4. REMAINING BUDGET
//                 // =========================================================

//                 BigDecimal remainingBudget = totalBudget.subtract(usedBudget);

//                 if (remainingBudget.compareTo(BigDecimal.ZERO) < 0) {
//                         remainingBudget = BigDecimal.ZERO;
//                 }

//                 // =========================================================
//                 // 5. BUDGET PERCENTAGE
//                 // =========================================================

//                 BigDecimal percentageUsed = calculateBudgetPercentage(
//                                 usedBudget,
//                                 totalBudget);

//                 // =========================================================
//                 // 6. UPCOMING TRIP
//                 // =========================================================

//                 // Pageable limitOne = PageRequest.of(0, 1);
//                 List<Itinerary> upcomingTrips = itineraryRepository.findUpcomingTrips(
//                                 userId,
//                                 LocalDate.now(),
//                                 List.of(
//                                                 ItineraryStatus.COMPLETED,
//                                                 ItineraryStatus.CONCERNED,
//                                                 ItineraryStatus.DRAFT));

//                 UpcomingTripResponse upcomingTrip = null;

//                 if (!upcomingTrips.isEmpty()) {
//                         upcomingTrip = buildUpcomingTripResponse(
//                                         upcomingTrips.get(0));
//                 }

//                 // =========================================================
//                 // 7. FINAL RESPONSE
//                 // =========================================================

//                 return UserSummaryResponse.builder()

//                                 .totalTrips(
//                                                 Math.toIntExact(totalTrips))

//                                 .completedTrips(
//                                                 Math.toIntExact(completedTrips))

//                                 .placesVisited(
//                                                 Math.toIntExact(placesVisited))

//                                 // Country information is not available
//                                 // in current Location entity.
//                                 .countriesVisited(0)

//                                 .budget(
//                                                 BudgetSummaryResponse.builder()
//                                                                 .total(totalBudget)
//                                                                 .used(usedBudget)
//                                                                 .remaining(remainingBudget)
//                                                                 .percentageUsed(percentageUsed)
//                                                                 .build())

//                                 .upcomingTrip(upcomingTrip)

//                                 .travelPreferences(
//                                                 List.of())

//                                 .build();
//         }

//         private BigDecimal calculateBudgetPercentage(
//                         BigDecimal used,
//                         BigDecimal total) {

//                 if (used == null ||
//                                 total == null ||
//                                 total.compareTo(BigDecimal.ZERO) <= 0) {

//                         return BigDecimal.ZERO;
//                 }

//                 BigDecimal percentage = used.multiply(BigDecimal.valueOf(100))
//                                 .divide(
//                                                 total,
//                                                 2,
//                                                 RoundingMode.HALF_UP);

//                 if (percentage.compareTo(
//                                 BigDecimal.valueOf(100)) > 0) {

//                         return BigDecimal.valueOf(100);
//                 }

//                 if (percentage.compareTo(
//                                 BigDecimal.ZERO) < 0) {

//                         return BigDecimal.ZERO;
//                 }

//                 return percentage;
//         }

//         private UpcomingTripResponse buildUpcomingTripResponse(
//                         Itinerary itinerary) {

//                 if (itinerary == null) {
//                         return null;
//                 }

//                 return UpcomingTripResponse.builder()

//                                 .id(
//                                                 itinerary.getId())

//                                 .title(
//                                                 itinerary.getTitle())

//                                 .locationName(
//                                                 getLocationName(itinerary))

//                                 .startDate(
//                                                 itinerary.getStartDate())

//                                 .endDate(
//                                                 itinerary.getEndDate())

//                                 .totalDays(
//                                                 itinerary.getTotalDays())

//                                 .totalBudget(
//                                                 itinerary.getTotalBudget())

//                                 .estimatedCost(
//                                                 itinerary.getEstimatedCost())

//                                 .itineraryStatus(
//                                                 itinerary.getItineraryStatus())

//                                 .build();
//         }

//         private String getLocationName(
//                         Itinerary itinerary) {

//                 if (itinerary == null ||
//                                 itinerary.getLocation() == null) {

//                         return null;
//                 }

//                 String city = itinerary.getLocation().getCityName();

//                 String state = itinerary.getLocation().getStateName();

//                 if (city != null &&
//                                 !city.isBlank() &&
//                                 state != null &&
//                                 !state.isBlank()) {

//                         return city + ", " + state;
//                 }

//                 if (city != null &&
//                                 !city.isBlank()) {

//                         return city;
//                 }

//                 if (state != null &&
//                                 !state.isBlank()) {

//                         return state;
//                 }

//                 return null;
//         }

//         // ==========================================================
//         // DELETE
//         // ==========================================================

//         @Override
//         public void delete(
//                         @NonNull Long id) {

//                 Itinerary itinerary = getItinerary(id);

//                 validateOwnership(
//                                 itinerary);

//                 itineraryPlaceRepository
//                                 .deleteByItineraryDayItineraryId(
//                                                 id);

//                 itineraryDayRepository
//                                 .deleteByItineraryId(
//                                                 id);

//                 itineraryRepository.delete(
//                                 itinerary);

//                 log.info(
//                                 "Itinerary {} deleted successfully",
//                                 id);
//         }

//         // ==========================================================
//         // VALIDATION
//         // ==========================================================

//         private void validateRequest(
//                         ItineraryRequest request) {

//                 Objects.requireNonNull(
//                                 request,
//                                 "Itinerary request cannot be null.");

//                 if (request.getUserId() == null) {

//                         throw new IllegalArgumentException(
//                                         "User Id is required.");
//                 }

//                 if (request.getLocationId() == null) {

//                         throw new IllegalArgumentException(
//                                         "Location Id is required.");
//                 }

//                 if (request.getTitle() == null ||
//                                 request.getTitle().trim().isEmpty()) {

//                         throw new IllegalArgumentException(
//                                         "Trip title is required.");
//                 }

//                 if (request.getTotalDays() == null ||
//                                 request.getTotalDays() <= 0) {

//                         throw new IllegalArgumentException(
//                                         "Total days must be greater than zero.");
//                 }

//                 if (request.getTotalDays() > MAX_DAYS) {

//                         throw new IllegalArgumentException(
//                                         "Maximum trip duration is "
//                                                         + MAX_DAYS
//                                                         + " days.");
//                 }

//                 if (request.getTravelType() == null) {

//                         throw new IllegalArgumentException(
//                                         "Travel type is required.");
//                 }

//                 if (request.getTotalBudget() == null) {

//                         throw new IllegalArgumentException(
//                                         "Total budget is required.");
//                 }

//                 if (request.getTotalBudget()
//                                 .compareTo(BigDecimal.ZERO) <= 0) {

//                         throw new IllegalArgumentException(
//                                         "Budget must be greater than zero.");
//                 }

//                 if (request.getEstimatedCost() != null &&
//                                 request.getEstimatedCost()
//                                                 .compareTo(BigDecimal.ZERO) < 0) {

//                         throw new IllegalArgumentException(
//                                         "Estimated cost cannot be negative.");
//                 }

//                 // ======================================================
//                 // DATE VALIDATION
//                 // ======================================================

//                 if (request.getStartDate() != null &&
//                                 request.getEndDate() != null &&
//                                 request.getEndDate()
//                                                 .isBefore(
//                                                                 request.getStartDate())) {

//                         throw new IllegalArgumentException(
//                                         "End date cannot be before start date.");
//                 }
//         }

//         // ==========================================================
//         // OWNERSHIP
//         // ==========================================================

//         private void validateOwnership(
//                         Itinerary itinerary) {

//                 System.out.println(
//                                 "Validating ownership for itinerary IDss: "
//                                                 + itinerary.getId()
//                                                 + ", User ID: "
//                                                 + itinerary.getUser().getId() + securityUtils.getCurrentUserId());
//                 Long currentUserId = securityUtils.getCurrentUserId();

//                 System.out.println(
//                                 "Current User ID: "
//                                                 + currentUserId
//                                                 + ", Itinerary User ID: "
//                                                 + itinerary.getUser().getId());
//                 if (!Objects.equals(
//                                 itinerary.getUser().getId(),
//                                 currentUserId)) {

//                         throw new UnauthorizedException(
//                                         "You are not authorized to access this itinerary.");
//                 }
//         }

//         // ==========================================================
//         // GET USER
//         // ==========================================================

//         private User getUser(
//                         @NonNull Long userId) {

//                 return userRepository
//                                 .findById(userId)
//                                 .orElseThrow(
//                                                 () -> new ResourceNotFoundException(
//                                                                 "User not found with id: "
//                                                                                 + userId));
//         }

//         // ==========================================================
//         // GET LOCATION
//         // ==========================================================

//         private Location getLocation(
//                         @NonNull Long locationId) {

//                 return locationRepository
//                                 .findById(locationId)
//                                 .orElseThrow(
//                                                 () -> new ResourceNotFoundException(
//                                                                 "Location not found with id: "
//                                                                                 + locationId));
//         }

//         // ==========================================================
//         // GET ITINERARY
//         // ==========================================================

//         private Itinerary getItinerary(
//                         @NonNull Long itineraryId) {

//                 return itineraryRepository
//                                 .findById(itineraryId)
//                                 .orElseThrow(
//                                                 () -> new ResourceNotFoundException(
//                                                                 "Itinerary not found with id: "
//                                                                                 + itineraryId));
//         }

//         // ==========================================================
//         // CALCULATE REMAINING BUDGET BY ID
//         // ==========================================================

//         @Override
//         @Transactional(readOnly = true)
//         public BigDecimal calculateRemainingBudget(
//                         @NonNull Long itineraryId) {

//                 Itinerary itinerary = getItinerary(
//                                 itineraryId);

//                 validateOwnership(
//                                 itinerary);

//                 return calculateRemainingBudget(
//                                 itinerary);
//         }

//         // ==========================================================
//         // CHECK OWNER
//         // ==========================================================

//         @Override
//         @Transactional(readOnly = true)
//         public boolean isItineraryOwner(
//                         @NonNull Long itineraryId) {

//                 try {

//                         Itinerary itinerary = getItinerary(
//                                         itineraryId);

//                         Long currentUserId = securityUtils.getCurrentUserId();

//                         return Objects.equals(
//                                         itinerary.getUser().getId(),
//                                         currentUserId);

//                 } catch (Exception exception) {

//                         return false;
//                 }
//         }

//         // ==========================================================
//         // MANUAL STATUS UPDATE DISABLED
//         // ==========================================================

//         @Override
//         public ItineraryResponse updateStatus(
//                         @NonNull Long id,
//                         ItineraryStatus status) {

//                 throw new UnsupportedOperationException(
//                                 "Manual itinerary status update is not allowed. "
//                                                 + "Status is managed automatically by backend.");
//         }

//         // ==========================================================
//         // GET BY STATUS
//         // ==========================================================

//         @Override
//         @Transactional(readOnly = true)
//         public List<ItineraryResponse> getItinerariesByStatus(
//                         ItineraryStatus status) {

//                 Long userId = securityUtils.getCurrentUserId();

//                 return itineraryRepository
//                                 .findByUserIdAndItineraryStatus(
//                                                 userId,
//                                                 status)
//                                 .stream()
//                                 .map(
//                                                 itineraryMapper::toResponse)
//                                 .toList();
//         }

//         // ==========================================================
//         // UPCOMING
//         // ==========================================================

//         @Override
//         @Transactional(readOnly = true)
//         public List<ItineraryResponse> getUpcomingItineraries() {

//                 Long userId = securityUtils.getCurrentUserId();

//                 LocalDate today = LocalDate.now();

//                 return itineraryRepository
//                                 .findByUserIdAndStartDateAfterOrderByStartDateAsc(
//                                                 userId,
//                                                 today)
//                                 .stream()
//                                 .map(
//                                                 itineraryMapper::toResponse)
//                                 .toList();
//         }

//         // ==========================================================
//         // WITHIN BUDGET
//         // ==========================================================

//         @Override
//         @Transactional(readOnly = true)
//         public List<ItineraryResponse> getItinerariesWithinBudget(
//                         BigDecimal minBudget,
//                         BigDecimal maxBudget) {

//                 Long userId = securityUtils.getCurrentUserId();

//                 return itineraryRepository
//                                 .findByUserIdAndTotalBudgetBetween(
//                                                 userId,
//                                                 minBudget,
//                                                 maxBudget)
//                                 .stream()
//                                 .map(
//                                                 itineraryMapper::toResponse)
//                                 .toList();
//         }

//         // ==========================================================
//         // UPDATE DISABLED
//         // ==========================================================
//         //
//         // User-facing itinerary update nahi hoga.
//         //
//         // Backend automatically updates:
//         // - completed
//         // - status
//         // - updatedAt
//         //
//         // ==========================================================

//         @Override
//         public ItineraryResponse update(
//                         @NonNull Long id,
//                         @NonNull ItineraryRequest request) {

//                 throw new UnsupportedOperationException(
//                                 "Manual itinerary update is not allowed. "
//                                                 + "Itinerary is generated once and progress is "
//                                                 + "updated automatically by backend.");
//         }

// }

package com.example.tripItinerary.Service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.projection.UserSummaryProjection;
import com.example.tripItinerary.DTO.request.ItineraryRequest;
import com.example.tripItinerary.DTO.response.BudgetSummaryResponse;
import com.example.tripItinerary.DTO.response.ItineraryResponse;
import com.example.tripItinerary.DTO.response.UpcomingTripResponse;
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
        private final SecurityUtils securityUtils;
        private final ObjectMapper objectMapper;
        private final TemporaryItineraryRepository temporaryItineraryRepository;

        // ==========================================================
        // CONSTANTS
        // ==========================================================

        private static final int MAX_DAYS = 30;

        private static final int MAX_PLACES_PER_DAY = 7;
        private static final int MAX_PRIMARY_TOURIST_PLACES_PER_DAY = 3;
        private static final int MAX_TOTAL_ACTIVITIES_PER_DAY = 7;
        private static final int ACTIVITY_BUFFER_MINUTES = 10;
        private static final int RESTAURANT_DURATION_MINUTES = 60;
        private static final int HOTEL_DURATION_MINUTES = 60;
        private static final int DAY_START_HOUR = 9;
        private static final int DAY_END_HOUR = 23;
        private static final double RESTAURANT_MAX_DISTANCE_KM = 5.0;
        private static final double TOURIST_MAX_DISTANCE_KM = 7.0;
        private static final double HOTEL_MAX_DISTANCE_KM = 7.0;

        private static final int DEFAULT_VISIT_MINUTES = 90;

        // private static final int DEFAULT_TRAVEL_MINUTES = 30;
        private static final int DEFAULT_AVERAGE_SPEED_KM_PER_HOUR = 20;

        private static final int NUMBER_OF_OPTIONS = 5;

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

                @SuppressWarnings("null")
                User user = getUser(
                                request.getUserId());
                // ======================================================
                // STEP 3: FETCH LOCATION
                // ======================================================

                Location location = getLocation(
                                request.getLocationId());

                // ======================================================
                // STEP 4: RESOLVE HIERARCHICAL LOCATION SCOPE
                // ======================================================
                // CITY / AREA -> selected location + child areas
                // DISTRICT / REGION -> selected location + all descendants
                // STATE -> complete state hierarchy
                // COUNTRY -> complete country hierarchy
                //
                // Existing IDs are never changed. Tourism data is read from
                // the exact location_id where it was stored.
                // ======================================================

                List<Long> scopeLocationIds = getScopeLocationIds(location);

                log.info(
                                "Itinerary scope => locationId={}, type={}, locations={}",
                                location.getId(),
                                getLocationTypeName(location),
                                scopeLocationIds.size());

                // ======================================================
                // STEP 5: FETCH ALL SOURCE DATA FROM COMPLETE SCOPE
                // ======================================================

                List<TouristPlace> allTouristPlaces = fetchTouristPlaces(scopeLocationIds);
                List<Hotel> allHotels = fetchHotels(scopeLocationIds);
                List<Restaurant> allRestaurants = fetchRestaurants(scopeLocationIds);

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

                        // STATE/Country selections are not treated as one city.
                        // The engine first selects geographically diverse
                        // destination candidates, then the normal day scheduler
                        // builds the route. City/region/district selections use
                        // all descendants directly.
                        List<TouristPlace> preparedPlaces = prepareTouristPlacesForLocation(
                                        filteredPlaces,
                                        location,
                                        itinerary,
                                        optionNumber);

                        List<TouristPlace> sortedPlaces = sortTouristPlacesForOption(
                                        preparedPlaces,
                                        optionNumber);

                        int maxTouristPlaces = MAX_PLACES_PER_DAY * safeTotalDays(itinerary);
                        if (sortedPlaces.size() > maxTouristPlaces) {
                                sortedPlaces = sortedPlaces.subList(0, maxTouristPlaces);
                        }

                        // ==================================================
                        // SMART DAY-BY-DAY SCHEDULER
                        // ==================================================
                        // Tourist places, restaurants and hotels are scheduled
                        // together so that time, meal windows, distance and
                        // activity limits are respected.

                        List<Hotel> hotels = assignHotelsForOption(
                                        itinerary,
                                        allHotels,
                                        optionNumber);

                        List<Restaurant> restaurants = assignRestaurantsForOption(
                                        itinerary,
                                        allRestaurants,
                                        optionNumber);

                        generateSmartDaySchedules(
                                        itinerary,
                                        sortedPlaces,
                                        restaurants,
                                        hotels,
                                        location,
                                        optionNumber);

                        // ==================================================
                        // CALCULATE ESTIMATED COST
                        // ==================================================

                        BigDecimal estimatedCost = calculateTotalEstimatedCost(
                                        itinerary);

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

                Itinerary savedItinerary = itineraryRepository.save(
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

                        savedDays = itineraryDayRepository.saveAll(
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

                        @SuppressWarnings("null")
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
                                                .distanceKm(
                                                                oldPlace.getDistanceKm())
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

                        itineraryPlaceRepository.saveAll(
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
                                .restaurantsPerDay(
                                                response.getRestaurantsPerDay() != null
                                                                ? Math.max(
                                                                                1,
                                                                                response.getRestaurantsPerDay())
                                                                : 1)
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
                                                        .distanceKm(
                                                                        placeResponse.getDistanceKm())
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
        // SMART DAY-BY-DAY ITINERARY SCHEDULER
        // ==========================================================

        /**
         * Generates the complete day schedule in one pass.
         *
         * Important rules:
         * 1. Tourist places, restaurants and hotels are NOT scheduled independently.
         * 2. A meal window has priority. If a restaurant can be scheduled in that
         * window, no tourist-place activity is inserted in the same window.
         * 3. A tourist place already started is never interrupted by a meal window.
         * 4. Distance from the previous activity is considered before selecting the
         * next activity.
         * 5. Three tourist places/day is the preferred/primary target, NOT a reason
         * to stop the day early. If time remains, suitable activities can still
         * be added until the daily activity cap.
         * 6. Existing response structure is unchanged: ItineraryDay -> ItineraryPlace
         * with the same placeType/referenceId/time/cost/notes fields.
         */
        private void generateSmartDaySchedules(
                        Itinerary itinerary,
                        List<TouristPlace> touristPlaces,
                        List<Restaurant> restaurants,
                        List<Hotel> hotels,
                        Location location,
                        int optionNumber) {

                if (itinerary == null || itinerary.getItineraryDays() == null) {
                        return;
                }

                Set<Long> usedTouristPlaceIds = new HashSet<>();
                Set<Long> usedRestaurantIds = new HashSet<>();

                for (ItineraryDay day : itinerary.getItineraryDays()) {
                        if (day == null) {
                                continue;
                        }

                        scheduleSingleDay(
                                        day,
                                        itinerary,
                                        touristPlaces,
                                        restaurants,
                                        hotels,
                                        location,
                                        optionNumber,
                                        usedTouristPlaceIds,
                                        usedRestaurantIds);
                }
        }

        /**
         * Schedules one day without overlapping travel time, visit time and meal
         * windows. Travel time is now added BEFORE the next activity starts.
         */
        private void scheduleSingleDay(
                        ItineraryDay day,
                        Itinerary itinerary,
                        List<TouristPlace> touristPlaces,
                        List<Restaurant> restaurants,
                        List<Hotel> hotels,
                        Location location,
                        int optionNumber,
                        Set<Long> usedTouristPlaceIds,
                        Set<Long> usedRestaurantIds) {

                LocalTime currentTime = getDefaultStartTime();
                LocalTime dayEnd = LocalTime.of(DAY_END_HOUR, 0);
                int visitOrder = 1;
                int activityCount = 0;
                int touristCount = 0;
                int restaurantCount = 0;
                int restaurantsPerDay = Math.max(0, itinerary.getSafeRestaurantsPerDay());

                double previousLat = getCoordinate(location, "latitude");
                double previousLng = getCoordinate(location, "longitude");
                boolean previousCoordinatesAvailable = hasCoordinates(location);

                while (currentTime.isBefore(dayEnd)
                                && activityCount < MAX_TOTAL_ACTIVITIES_PER_DAY) {

                        // --------------------------------------------------
                        // 1. MEAL WINDOW
                        // --------------------------------------------------
                        MealWindow mealWindow = getCurrentMealWindow(currentTime, restaurantsPerDay, restaurantCount);

                        if (mealWindow != null) {
                                boolean restaurantSlotStillRequired = restaurantCount < restaurantsPerDay;

                                if (restaurantSlotStillRequired) {
                                        Restaurant restaurant = findBestRestaurantForSlot(
                                                        restaurants,
                                                        usedRestaurantIds,
                                                        mealWindow.start,
                                                        mealWindow.end,
                                                        previousLat,
                                                        previousLng,
                                                        previousCoordinatesAvailable,
                                                        optionNumber);

                                        if (restaurant != null) {
                                                double distance = distanceKm(
                                                                previousCoordinatesAvailable,
                                                                previousLat,
                                                                previousLng,
                                                                getCoordinate(restaurant, "latitude"),
                                                                getCoordinate(restaurant, "longitude"));

                                                int travelMinutes = calculateTravelTimeMinutes(distance);
                                                LocalTime restaurantStart = currentTime.plusMinutes(travelMinutes);

                                                if (restaurantStart.isBefore(mealWindow.start)) {
                                                        restaurantStart = mealWindow.start;
                                                }

                                                LocalTime opening = getTimeProperty(
                                                                restaurant,
                                                                "openingTime",
                                                                "openTime",
                                                                "openingHour");
                                                if (opening != null && opening.isAfter(restaurantStart)) {
                                                        restaurantStart = opening;
                                                }

                                                LocalTime restaurantEnd = restaurantStart
                                                                .plusMinutes(RESTAURANT_DURATION_MINUTES);

                                                if (!restaurantEnd.isAfter(mealWindow.end)
                                                                && !restaurantEnd.isAfter(dayEnd)
                                                                && isOpenForSlot(restaurant, restaurantStart,
                                                                                restaurantEnd)) {

                                                        day.addPlace(buildRestaurantItineraryPlace(
                                                                        day,
                                                                        restaurant,
                                                                        visitOrder++,
                                                                        restaurantStart,
                                                                        toDistanceBigDecimal(distance),
                                                                        travelMinutes));

                                                        usedRestaurantIds.add(restaurant.getId());
                                                        restaurantCount++;
                                                        activityCount++;

                                                        if (hasCoordinates(restaurant)) {
                                                                previousLat = getCoordinate(restaurant, "latitude");
                                                                previousLng = getCoordinate(restaurant, "longitude");
                                                                previousCoordinatesAvailable = true;
                                                        }

                                                        currentTime = restaurantEnd
                                                                        .plusMinutes(ACTIVITY_BUFFER_MINUTES);
                                                        continue;
                                                }
                                        }
                                }

                                // Never put sightseeing inside a reserved meal window.
                                if (currentTime.isBefore(mealWindow.end)) {
                                        currentTime = mealWindow.end;
                                        continue;
                                }
                        }

                        // --------------------------------------------------
                        // 2. HOTEL / END OF DAY
                        // --------------------------------------------------
                        if (currentTime.compareTo(LocalTime.of(20, 0)) >= 0) {
                                Hotel hotel = findBestHotelForEndOfDay(
                                                hotels,
                                                previousLat,
                                                previousLng,
                                                previousCoordinatesAvailable,
                                                itinerary,
                                                optionNumber);

                                if (hotel != null) {
                                        double hotelDistance = distanceKm(
                                                        previousCoordinatesAvailable,
                                                        previousLat,
                                                        previousLng,
                                                        getCoordinate(hotel, "latitude"),
                                                        getCoordinate(hotel, "longitude"));

                                        int hotelTravelMinutes = calculateTravelTimeMinutes(hotelDistance);
                                        LocalTime hotelStart = currentTime.plusMinutes(hotelTravelMinutes);
                                        LocalTime hotelEnd = hotelStart.plusMinutes(HOTEL_DURATION_MINUTES);

                                        if (!hotelEnd.isAfter(dayEnd)) {
                                                day.addPlace(buildHotelItineraryPlace(
                                                                day,
                                                                hotel,
                                                                visitOrder++,
                                                                hotelStart,
                                                                toDistanceBigDecimal(hotelDistance),
                                                                hotelTravelMinutes));

                                                activityCount++;
                                                if (hasCoordinates(hotel)) {
                                                        previousLat = getCoordinate(hotel, "latitude");
                                                        previousLng = getCoordinate(hotel, "longitude");
                                                        previousCoordinatesAvailable = true;
                                                }
                                                currentTime = hotelEnd;
                                        }
                                }
                                break;
                        }

                        // --------------------------------------------------
                        // 3. TOURIST PLACE
                        // --------------------------------------------------
                        TouristPlace nextPlace = findBestTouristPlace(
                                        touristPlaces,
                                        usedTouristPlaceIds,
                                        currentTime,
                                        dayEnd,
                                        previousLat,
                                        previousLng,
                                        previousCoordinatesAvailable,
                                        optionNumber,
                                        touristCount < MAX_PRIMARY_TOURIST_PLACES_PER_DAY);

                        if (nextPlace == null) {
                                LocalTime nextMealStart = getNextMealWindowStart(
                                                currentTime,
                                                restaurantsPerDay,
                                                restaurantCount);
                                if (nextMealStart != null && nextMealStart.isAfter(currentTime)) {
                                        currentTime = nextMealStart;
                                        continue;
                                }
                                break;
                        }

                        double touristDistance = distanceKm(
                                        previousCoordinatesAvailable,
                                        previousLat,
                                        previousLng,
                                        getCoordinate(nextPlace, "latitude"),
                                        getCoordinate(nextPlace, "longitude"));

                        int touristTravelMinutes = calculateTravelTimeMinutes(touristDistance);
                        LocalTime touristStart = currentTime.plusMinutes(touristTravelMinutes);
                        int visitMinutes = getVisitMinutes(nextPlace);
                        LocalTime touristEnd = touristStart.plusMinutes(visitMinutes);

                        if (touristEnd.isAfter(dayEnd)) {
                                break;
                        }

                        // Meal windows are hard boundaries.
                        // A tourist activity/travel segment must never enter a meal window.
                        LocalTime adjustedTime = moveBeforeMealWindowIfNeeded(
                                        currentTime,
                                        touristStart,
                                        touristEnd,
                                        restaurantsPerDay,
                                        restaurantCount);

                        if (!adjustedTime.equals(currentTime)) {
                                currentTime = adjustedTime;
                                continue;
                        }

                        if (overlapsMealWindow(
                                        touristStart,
                                        touristEnd,
                                        restaurantsPerDay,
                                        restaurantCount)) {
                                MealWindow nextMeal = getNextMealWindow(
                                                currentTime,
                                                restaurantsPerDay,
                                                restaurantCount);

                                currentTime = nextMeal != null
                                                ? nextMeal.start
                                                : touristStart;
                                continue;
                        }

                        day.addPlace(buildTouristPlaceItineraryPlace(
                                        day,
                                        nextPlace,
                                        visitOrder++,
                                        touristStart,
                                        touristEnd,
                                        touristTravelMinutes,
                                        toDistanceBigDecimal(touristDistance)));

                        usedTouristPlaceIds.add(nextPlace.getId());
                        touristCount++;
                        activityCount++;

                        if (hasCoordinates(nextPlace)) {
                                previousLat = getCoordinate(nextPlace, "latitude");
                                previousLng = getCoordinate(nextPlace, "longitude");
                                previousCoordinatesAvailable = true;
                        }

                        currentTime = touristEnd.plusMinutes(ACTIVITY_BUFFER_MINUTES);
                }

                log.info(
                                "Day {} scheduled: touristPlaces={}, restaurants={}, totalActivities={}",
                                day.getDayNumber(),
                                touristCount,
                                restaurantCount,
                                activityCount);
        }

        // ==========================================================
        // MEAL WINDOWS
        // ==========================================================

        private MealWindow getCurrentMealWindow(
                        LocalTime currentTime,
                        int restaurantsPerDay,
                        int restaurantCount) {

                if (restaurantsPerDay <= 0) {
                        return null;
                }

                for (MealWindow window : getMealWindows(restaurantsPerDay)) {

                        if (restaurantCount >= window.index + 1) {
                                continue;
                        }

                        if (!currentTime.isBefore(window.start)
                                        && currentTime.isBefore(window.end)) {
                                return window;
                        }
                }

                return null;
        }

        private MealWindow getNextMealWindow(
                        LocalTime currentTime,
                        int restaurantsPerDay,
                        int restaurantCount) {

                if (restaurantsPerDay <= 0) {
                        return null;
                }

                for (MealWindow window : getMealWindows(restaurantsPerDay)) {

                        if (restaurantCount >= window.index + 1) {
                                continue;
                        }

                        if (window.end.isAfter(currentTime)) {
                                return window;
                        }
                }

                return null;
        }

        private boolean overlapsMealWindow(
                        LocalTime activityStart,
                        LocalTime activityEnd,
                        int restaurantsPerDay,
                        int restaurantCount) {

                if (restaurantsPerDay <= 0
                                || activityStart == null
                                || activityEnd == null
                                || !activityEnd.isAfter(activityStart)) {
                        return false;
                }

                for (MealWindow window : getMealWindows(restaurantsPerDay)) {

                        if (restaurantCount >= window.index + 1) {
                                continue;
                        }

                        boolean overlaps = activityStart.isBefore(window.end)
                                        && activityEnd.isAfter(window.start);

                        if (overlaps) {
                                return true;
                        }
                }

                return false;
        }

        private LocalTime moveBeforeMealWindowIfNeeded(
                        LocalTime currentTime,
                        LocalTime activityStart,
                        LocalTime activityEnd,
                        int restaurantsPerDay,
                        int restaurantCount) {

                if (restaurantsPerDay <= 0) {
                        return currentTime;
                }

                for (MealWindow window : getMealWindows(restaurantsPerDay)) {

                        if (restaurantCount >= window.index + 1) {
                                continue;
                        }

                        if (!activityStart.isBefore(window.end)
                                        || !activityEnd.isAfter(window.start)) {
                                continue;
                        }

                        /*
                         * If the activity starts before the meal but reaches into it,
                         * wait until the meal starts. The meal must remain untouched.
                         */
                        if (activityStart.isBefore(window.start)) {
                                return window.start;
                        }

                        /*
                         * If travel/activity has already reached the meal window,
                         * move to the end of that meal window.
                         */
                        return window.end;
                }

                return currentTime;
        }

        private List<MealWindow> getMealWindows(int restaurantsPerDay) {

                List<MealWindow> windows = new ArrayList<>();

                if (restaurantsPerDay >= 1) {
                        windows.add(new MealWindow(
                                        0,
                                        LocalTime.of(12, 0),
                                        LocalTime.of(14, 0),
                                        "LUNCH"));
                }

                if (restaurantsPerDay >= 2) {
                        windows.add(new MealWindow(
                                        1,
                                        LocalTime.of(19, 0),
                                        LocalTime.of(22, 0),
                                        "DINNER"));
                }

                if (restaurantsPerDay >= 3) {
                        /*
                         * Third meal slot is evening.
                         * Order is always: lunch -> evening -> dinner.
                         */
                        windows.add(1, new MealWindow(
                                        1,
                                        LocalTime.of(17, 0),
                                        LocalTime.of(19, 0),
                                        "EVENING"));

                        /*
                         * Re-index after insertion so the restaurant count
                         * maps correctly to the windows.
                         */
                        windows.get(0).index = 0;
                        windows.get(1).index = 1;
                        windows.get(2).index = 2;
                }

                return windows;
        }

        private LocalTime getNextMealWindowStart(
                        LocalTime currentTime,
                        int restaurantsPerDay,
                        int restaurantCount) {

                MealWindow window = getNextMealWindow(
                                currentTime,
                                restaurantsPerDay,
                                restaurantCount);

                return window == null ? null : window.start;
        }

        // ==========================================================
        // TOURIST PLACE SELECTION
        // ==========================================================

        @SuppressWarnings("null")
        private TouristPlace findBestTouristPlace(
                        List<TouristPlace> places,
                        Set<Long> usedIds,
                        LocalTime currentTime,
                        LocalTime dayEnd,
                        double previousLat,
                        double previousLng,
                        boolean previousCoordinatesAvailable,
                        int optionNumber,
                        boolean primarySlot) {

                if (places == null || places.isEmpty()) {
                        return null;
                }

                List<ScoredCandidate<TouristPlace>> candidates = new ArrayList<>();

                for (TouristPlace place : places) {
                        if (place == null || place.getId() == null || usedIds.contains(place.getId())) {
                                continue;
                        }

                        int visitMinutes = getVisitMinutes(place);
                        LocalTime end = currentTime.plusMinutes(visitMinutes);

                        if (end.isAfter(dayEnd)) {
                                continue;
                        }

                        double distance = distanceKm(
                                        previousCoordinatesAvailable,
                                        previousLat,
                                        previousLng,
                                        getCoordinate(place, "latitude"),
                                        getCoordinate(place, "longitude"));

                        if (previousCoordinatesAvailable && hasCoordinates(place)
                                        && distance > TOURIST_MAX_DISTANCE_KM) {
                                continue;
                        }

                        double score = scoreTouristPlace(place, distance, optionNumber, primarySlot);
                        candidates.add(new ScoredCandidate<>(place, score));
                }

                return candidates.stream()
                                .sorted(Comparator.comparingDouble(ScoredCandidate<TouristPlace>::score).reversed())
                                .map(ScoredCandidate::value)
                                .findFirst()
                                .orElse(null);
        }

        private double scoreTouristPlace(
                        TouristPlace place,
                        double distance,
                        int optionNumber,
                        boolean primarySlot) {

                double rating = normalize(getNumericProperty(place, "averageRating"), 5.0);
                double popularity = normalize(getNumericProperty(place, "popularityScore"), 100.0);
                double weight = inverseWeight(getNumericProperty(place, "placeWeight", "touristPlaceWeight"));
                double distanceScore = distanceScore(distance, TOURIST_MAX_DISTANCE_KM);
                double budgetScore = budgetFriendliness(place.getPrice());

                double optionBonus;
                if (optionNumber == 1) {
                        optionBonus = rating * 0.15 + popularity * 0.10;
                } else if (optionNumber == 2) {
                        optionBonus = popularity * 0.20 + rating * 0.05;
                } else if (optionNumber == 3) {
                        optionBonus = budgetScore * 0.20 + rating * 0.05;
                } else if (optionNumber == 4) {
                        optionBonus = weight * 0.20 + distanceScore * 0.10;
                } else {
                        optionBonus = distanceScore * 0.20 + rating * 0.10;
                }

                return weight * 0.30
                                + distanceScore * 0.30
                                + rating * 0.15
                                + popularity * 0.10
                                + budgetScore * 0.05
                                + optionBonus
                                + (primarySlot ? 0.05 : 0.0);
        }

        // ==========================================================
        // RESTAURANT SELECTION
        // ==========================================================

        @SuppressWarnings("null")
        private Restaurant findBestRestaurantForSlot(
                        List<Restaurant> restaurants,
                        Set<Long> usedIds,
                        LocalTime slotStart,
                        LocalTime slotEnd,
                        double previousLat,
                        double previousLng,
                        boolean previousCoordinatesAvailable,
                        int optionNumber) {

                if (restaurants == null || restaurants.isEmpty()) {
                        return null;
                }

                List<ScoredCandidate<Restaurant>> candidates = new ArrayList<>();

                for (Restaurant restaurant : restaurants) {
                        if (restaurant == null || restaurant.getId() == null || usedIds.contains(restaurant.getId())) {
                                continue;
                        }

                        LocalTime possibleStart = slotStart;
                        LocalTime opening = getTimeProperty(restaurant, "openingTime", "openTime", "openingHour");
                        if (opening != null && opening.isAfter(possibleStart)) {
                                possibleStart = opening;
                        }

                        LocalTime possibleEnd = possibleStart.plusMinutes(RESTAURANT_DURATION_MINUTES);
                        if (possibleEnd.isAfter(slotEnd)
                                        || !isOpenForSlot(restaurant, possibleStart, possibleEnd)) {
                                continue;
                        }

                        double distance = distanceKm(
                                        previousCoordinatesAvailable,
                                        previousLat,
                                        previousLng,
                                        getCoordinate(restaurant, "latitude"),
                                        getCoordinate(restaurant, "longitude"));

                        if (previousCoordinatesAvailable && hasCoordinates(restaurant)
                                        && distance > RESTAURANT_MAX_DISTANCE_KM) {
                                continue;
                        }

                        double score = scoreRestaurant(
                                        restaurant,
                                        distance,
                                        optionNumber);
                        candidates.add(new ScoredCandidate<>(restaurant, score));
                }

                return candidates.stream()
                                .sorted(Comparator.comparingDouble(ScoredCandidate<Restaurant>::score).reversed())
                                .map(ScoredCandidate::value)
                                .findFirst()
                                .orElse(null);
        }

        private double scoreRestaurant(
                        Restaurant restaurant,
                        double distance,
                        int optionNumber) {

                double rating = normalize(getNumericProperty(restaurant, "averageRating"), 5.0);
                double weight = inverseWeight(getNumericProperty(restaurant, "restaurantWeight"));
                double distanceScore = distanceScore(distance, RESTAURANT_MAX_DISTANCE_KM);
                double costScore = budgetFriendliness(restaurant.getAverageCostPerPerson());
                double cuisineScore = hasTextProperty(restaurant, "cuisineType") ? 1.0 : 0.5;
                double availabilityScore = isOpenNow(restaurant) ? 1.0 : 0.5;

                double optionScore;
                if (optionNumber == 1) {
                        optionScore = rating * 0.20;
                } else if (optionNumber == 2) {
                        optionScore = costScore * 0.20;
                } else if (optionNumber == 3) {
                        optionScore = rating * 0.10 + costScore * 0.10;
                } else if (optionNumber == 4) {
                        optionScore = weight * 0.20;
                } else {
                        optionScore = distanceScore * 0.20 + cuisineScore * 0.05;
                }

                return weight * 0.30
                                + distanceScore * 0.30
                                + rating * 0.15
                                + costScore * 0.10
                                + availabilityScore * 0.05
                                + optionScore;
        }

        // ==========================================================
        // HOTEL SELECTION
        // ==========================================================

        @SuppressWarnings("null")
        private Hotel findBestHotelForEndOfDay(
                        List<Hotel> hotels,
                        double previousLat,
                        double previousLng,
                        boolean previousCoordinatesAvailable,
                        Itinerary itinerary,
                        int optionNumber) {

                if (hotels == null || hotels.isEmpty()) {
                        return null;
                }

                List<ScoredCandidate<Hotel>> candidates = new ArrayList<>();
                BigDecimal perDayBudget = safeBigDecimal(itinerary.getTotalBudget())
                                .multiply(HOTEL_BUDGET_PERCENTAGE)
                                .divide(BigDecimal.valueOf(Math.max(1, safeTotalDays(itinerary))), 2,
                                                RoundingMode.HALF_UP);

                for (Hotel hotel : hotels) {
                        if (hotel == null || hotel.getId() == null) {
                                continue;
                        }

                        if (hotel.getPricePerNight() == null) {
                                continue;
                        }

                        double distance = distanceKm(
                                        previousCoordinatesAvailable,
                                        previousLat,
                                        previousLng,
                                        getCoordinate(hotel, "latitude"),
                                        getCoordinate(hotel, "longitude"));

                        if (previousCoordinatesAvailable && hasCoordinates(hotel)
                                        && distance > HOTEL_MAX_DISTANCE_KM) {
                                continue;
                        }

                        double budgetScore = hotel.getPricePerNight().compareTo(perDayBudget) <= 0
                                        ? 1.0
                                        : 0.25;
                        double rating = normalize(getNumericProperty(hotel, "averageRating"), 5.0);
                        double weight = inverseWeight(getNumericProperty(hotel, "hotelWeight"));
                        double distanceScore = distanceScore(distance, HOTEL_MAX_DISTANCE_KM);

                        double optionScore = switch (optionNumber) {
                                case 1 -> rating * 0.20;
                                case 2 -> budgetScore * 0.20;
                                case 3 -> normalize(hotel.getPricePerNight().doubleValue(),
                                                Math.max(1.0, perDayBudget.doubleValue() * 2.0)) * 0.20;
                                case 4 -> weight * 0.20;
                                default -> distanceScore * 0.20;
                        };

                        double score = weight * 0.30
                                        + distanceScore * 0.35
                                        + rating * 0.15
                                        + budgetScore * 0.10
                                        + optionScore;

                        candidates.add(new ScoredCandidate<>(hotel, score));
                }

                // If strict distance filtering removed every hotel, use the closest
                // available hotel rather than leaving the day without accommodation.
                if (candidates.isEmpty()) {
                        for (Hotel hotel : hotels) {
                                if (hotel == null || hotel.getId() == null) {
                                        continue;
                                }
                                if (hotel.getPricePerNight() == null) {
                                        continue;
                                }
                                double distance = distanceKm(
                                                previousCoordinatesAvailable,
                                                previousLat,
                                                previousLng,
                                                getCoordinate(hotel, "latitude"),
                                                getCoordinate(hotel, "longitude"));
                                double score = distanceScore(distance, 20.0)
                                                + inverseWeight(getNumericProperty(hotel, "hotelWeight"));
                                candidates.add(new ScoredCandidate<>(hotel, score));
                        }
                }

                return candidates.stream()
                                .sorted(Comparator.comparingDouble(ScoredCandidate<Hotel>::score).reversed())
                                .map(ScoredCandidate::value)
                                .findFirst()
                                .orElse(null);
        }

        // ==========================================================
        // ITINERARY PLACE BUILDERS
        // ==========================================================

        private ItineraryPlace buildTouristPlaceItineraryPlace(
                        ItineraryDay day,
                        TouristPlace place,
                        int placeIndex,
                        LocalTime startTime,
                        LocalTime endTime,
                        int travelTimeMinutes,
                        BigDecimal distanceKm) {

                return ItineraryPlace.builder()
                                .itineraryDay(day)
                                .placeType(PlaceType.TOURIST_PLACE)
                                .referenceId(place.getId())
                                .visitOrder(placeIndex)
                                .plannedStartTime(startTime)
                                .plannedEndTime(endTime)
                                .estimatedCost(
                                                place.getPrice() != null
                                                                ? place.getPrice()
                                                                : BigDecimal.ZERO)
                                .travelTimeMinutes(travelTimeMinutes)
                                .distanceKm(distanceKm)
                                .notes(place.getDescription())
                                .completed(false)
                                .build();
        }

        private ItineraryPlace buildRestaurantItineraryPlace(
                        ItineraryDay day,
                        Restaurant restaurant,
                        int visitOrder,
                        LocalTime startTime,
                        BigDecimal distanceKm,
                        int travelTimeMinutes) {

                LocalTime endTime = startTime.plusMinutes(RESTAURANT_DURATION_MINUTES);

                return ItineraryPlace.builder()
                                .itineraryDay(day)
                                .placeType(PlaceType.RESTAURANT)
                                .referenceId(restaurant.getId())
                                .visitOrder(visitOrder)
                                .plannedStartTime(startTime)
                                .plannedEndTime(endTime)
                                .estimatedCost(
                                                restaurant.getAverageCostPerPerson() != null
                                                                ? restaurant.getAverageCostPerPerson()
                                                                : BigDecimal.ZERO)
                                .travelTimeMinutes(travelTimeMinutes)
                                .distanceKm(distanceKm)
                                .completed(false)
                                .notes(
                                                "Restaurant: "
                                                                + restaurant.getRestaurantName())
                                .build();
        }

        private ItineraryPlace buildHotelItineraryPlace(
                        ItineraryDay day,
                        Hotel hotel,
                        int visitOrder,
                        LocalTime startTime,
                        BigDecimal distanceKm,
                        int travelTimeMinutes) {

                LocalTime endTime = startTime.plusMinutes(HOTEL_DURATION_MINUTES);

                return ItineraryPlace.builder()
                                .itineraryDay(day)
                                .placeType(PlaceType.HOTEL)
                                .referenceId(hotel.getId())
                                .visitOrder(visitOrder)
                                .plannedStartTime(startTime)
                                .plannedEndTime(endTime)
                                .estimatedCost(
                                                hotel.getPricePerNight() != null
                                                                ? hotel.getPricePerNight()
                                                                : BigDecimal.ZERO)
                                .travelTimeMinutes(travelTimeMinutes)
                                .distanceKm(distanceKm)
                                .completed(false)
                                .notes(
                                                "Hotel: "
                                                                + hotel.getHotelName())
                                .build();
        }

        // ==========================================================
        // TIME / DISTANCE / OPENING-HOURS HELPERS
        // ==========================================================

        private int getVisitMinutes(TouristPlace place) {
                return place.getEstimatedVisitTimeMinutes() != null
                                ? Math.max(15, place.getEstimatedVisitTimeMinutes())
                                : DEFAULT_VISIT_MINUTES;
        }

        private int calculateTravelTimeMinutes(double distanceKm) {

                if (distanceKm <= 0.0) {
                        return 0;
                }

                double minutes = (distanceKm / DEFAULT_AVERAGE_SPEED_KM_PER_HOUR) * 60.0;

                return Math.max(5, (int) Math.ceil(minutes));
        }

        private BigDecimal toDistanceBigDecimal(double distanceKm) {
                return BigDecimal.valueOf(Math.max(0.0, distanceKm))
                                .setScale(2, RoundingMode.HALF_UP);
        }

        private boolean isOpenForSlot(
                        Object entity,
                        LocalTime start,
                        LocalTime end) {

                LocalTime opening = getTimeProperty(entity, "openingTime", "openTime", "openingHour");
                LocalTime closing = getTimeProperty(entity, "closingTime", "closeTime", "closingHour");

                if (opening == null || closing == null) {
                        return true;
                }

                // Normal same-day opening hours.
                if (closing.isAfter(opening)) {
                        return !start.isBefore(opening) && !end.isAfter(closing);
                }

                // Overnight hours, e.g. 18:00 -> 02:00.
                return !start.isBefore(opening) || !end.isAfter(closing);
        }

        private boolean isOpenNow(Object entity) {
                LocalTime now = LocalTime.now();
                return isOpenForSlot(entity, now, now.plusMinutes(1));
        }

        private double distanceKm(
                        boolean fromAvailable,
                        double lat1,
                        double lon1,
                        double lat2,
                        double lon2) {

                if (!fromAvailable || (lat2 == 0.0 && lon2 == 0.0)) {
                        return 0.0;
                }

                final double earthRadiusKm = 6371.0;
                double latDistance = Math.toRadians(lat2 - lat1);
                double lonDistance = Math.toRadians(lon2 - lon1);

                double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                                + Math.cos(Math.toRadians(lat1))
                                                * Math.cos(Math.toRadians(lat2))
                                                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                return earthRadiusKm * c;
        }

        private double distanceScore(double distance, double maxDistance) {
                if (distance <= 0.0)
                        return 1.0;
                return Math.max(0.0, 1.0 - (distance / maxDistance));
        }

        private double budgetFriendliness(BigDecimal cost) {
                if (cost == null || cost.signum() <= 0)
                        return 1.0;
                return 1.0 / (1.0 + cost.doubleValue() / 1000.0);
        }

        private double inverseWeight(double weight) {
                if (weight <= 0.0)
                        return 0.5;
                return 1.0 / (1.0 + weight);
        }

        private double normalize(double value, double max) {
                if (max <= 0.0)
                        return 0.0;
                return Math.max(0.0, Math.min(1.0, value / max));
        }

        private double getNumericProperty(Object entity, String... names) {
                if (entity == null)
                        return 0.0;

                for (String name : names) {
                        Object value = invokeGetter(entity, name);
                        if (value instanceof Number number) {
                                return number.doubleValue();
                        }
                        if (value != null) {
                                try {
                                        return Double.parseDouble(value.toString().trim());
                                } catch (Exception ignored) {
                                }
                        }
                }
                return 0.0;
        }

        private double getCoordinate(Object entity, String axis) {
                return getNumericProperty(entity, axis);
        }

        private boolean hasCoordinates(Object entity) {
                if (entity == null)
                        return false;
                Object lat = invokeGetter(entity, "latitude");
                Object lng = invokeGetter(entity, "longitude");
                if (lat == null || lng == null)
                        return false;
                try {
                        double la = Double.parseDouble(lat.toString());
                        double lo = Double.parseDouble(lng.toString());
                        return !(la == 0.0 && lo == 0.0);
                } catch (Exception e) {
                        return false;
                }
        }

        private LocalTime getTimeProperty(Object entity, String... names) {
                if (entity == null)
                        return null;

                for (String name : names) {
                        Object value = invokeGetter(entity, name);
                        if (value == null)
                                continue;
                        if (value instanceof LocalTime time)
                                return time;
                        try {
                                String text = value.toString().trim();
                                if (text.length() == 5)
                                        return LocalTime.parse(text);
                                if (text.length() >= 8)
                                        return LocalTime.parse(text.substring(0, 8));
                        } catch (Exception ignored) {
                        }
                }
                return null;
        }

        private boolean hasTextProperty(Object entity, String name) {
                Object value = invokeGetter(entity, name);
                return value != null && !value.toString().trim().isEmpty();
        }

        private Object invokeGetter(Object entity, String property) {
                if (entity == null || property == null || property.isBlank())
                        return null;

                String suffix = Character.toUpperCase(property.charAt(0)) + property.substring(1);
                String getterName = "get" + suffix;

                try {
                        Method method = entity.getClass().getMethod(getterName);
                        return method.invoke(entity);
                } catch (Exception ignored) {
                        return null;
                }
        }

        private static final class MealWindow {
                private int index;
                private final LocalTime start;
                private final LocalTime end;
                private final String name;

                private MealWindow(
                                int index,
                                LocalTime start,
                                LocalTime end,
                                String name) {
                        this.index = index;
                        this.start = start;
                        this.end = end;
                        this.name = name;
                }
        }

        private static final class ScoredCandidate<T> {
                private final T value;
                private final double score;

                private ScoredCandidate(T value, double score) {
                        this.value = value;
                        this.score = score;
                }

                private T value() {
                        return value;
                }

                private double score() {
                        return score;
                }
        }

        private LocalTime getDefaultStartTime() {
                return LocalTime.of(DAY_START_HOUR, 0);
        }

        // ==========================================================
        // SAFE TOTAL DAYS
        // ==========================================================

        private int safeTotalDays(
                        Itinerary itinerary) {

                if (itinerary == null
                                || itinerary.getTotalDays() == null) {

                        return 0;
                }

                return Math.max(
                                0,
                                itinerary.getTotalDays());
        }
        // ==========================================================
        // SAFE BIG DECIMAL
        // ==========================================================

        private BigDecimal safeBigDecimal(
                        BigDecimal value) {

                return value != null
                                ? value
                                : BigDecimal.ZERO;
        }

        // ==========================================================
        // HOTEL SELECTION
        // ==========================================================

        @SuppressWarnings("null")
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
                                .toList();
        }

        // ==========================================================
        // RESTAURANT SELECTION
        // ==========================================================

        // ==========================================================
        // RESTAURANT SELECTION
        // ==========================================================

        @SuppressWarnings("null")
        private List<Restaurant> assignRestaurantsForOption(
                        Itinerary itinerary,
                        List<Restaurant> restaurants,
                        int optionNumber) {

                if (itinerary == null
                                || restaurants == null
                                || restaurants.isEmpty()) {

                        return new ArrayList<>();
                }

                int totalDays = safeTotalDays(itinerary);

                if (totalDays <= 0) {
                        return new ArrayList<>();
                }

                int restaurantsPerDay = itinerary.getSafeRestaurantsPerDay();

                int totalRestaurantsRequired = totalDays * restaurantsPerDay;

                if (totalRestaurantsRequired <= 0) {
                        return new ArrayList<>();
                }

                BigDecimal totalBudget = safeBigDecimal(
                                itinerary.getTotalBudget());

                BigDecimal restaurantBudget = totalBudget.multiply(
                                RESTAURANT_BUDGET_PERCENTAGE);

                BigDecimal perMealBudget = restaurantBudget.divide(
                                BigDecimal.valueOf(
                                                totalRestaurantsRequired),
                                2,
                                java.math.RoundingMode.HALF_UP);

                // ======================================================
                // STRICT BUDGET FILTER
                // ======================================================

                List<Restaurant> filtered = restaurants.stream()
                                .filter(Objects::nonNull)
                                .filter(restaurant -> restaurant.getAverageCostPerPerson() != null)
                                .filter(restaurant -> restaurant
                                                .getAverageCostPerPerson()
                                                .signum() >= 0)
                                .filter(restaurant -> restaurant
                                                .getAverageCostPerPerson()
                                                .compareTo(perMealBudget) <= 0)
                                .toList();

                // ======================================================
                // FALLBACK
                // ======================================================

                if (filtered.isEmpty()) {

                        log.warn(
                                        "No restaurant found within per-meal budget {}. "
                                                        + "Using cheapest restaurants.",
                                        perMealBudget);

                        filtered = restaurants.stream()
                                        .filter(Objects::nonNull)
                                        .filter(restaurant -> restaurant.getAverageCostPerPerson() != null)
                                        .filter(restaurant -> restaurant
                                                        .getAverageCostPerPerson()
                                                        .signum() >= 0)
                                        .sorted(
                                                        Comparator.comparing(
                                                                        Restaurant::getAverageCostPerPerson))
                                        .toList();
                }

                if (filtered.isEmpty()) {
                        return new ArrayList<>();
                }

                int resultLimit = Math.min(
                                totalRestaurantsRequired,
                                filtered.size());

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
                                        .limit(resultLimit)
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
                                        .limit(resultLimit)
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
                                .limit(resultLimit)
                                .toList();
        }

        // ==========================================================
        // SORT TOURIST PLACES FOR OPTION
        // ==========================================================

        @SuppressWarnings("null")
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

        /**
         * Fetch tourist places for the selected location and ALL descendants.
         * This is the core hierarchy fix: selecting Bihar/Goa/South Goa no longer
         * looks only at the parent location_id.
         */
        private List<TouristPlace> fetchTouristPlaces(List<Long> locationIds) {

                List<TouristPlace> result = new ArrayList<>();

                if (locationIds == null || locationIds.isEmpty()) {
                        return result;
                }

                Set<Long> seen = new HashSet<>();

                for (Long locationId : locationIds) {
                        if (locationId == null) {
                                continue;
                        }

                        List<TouristPlace> places = touristPlaceRepository
                                        .findActiveByLocationId(locationId);

                        if (places == null) {
                                continue;
                        }

                        for (TouristPlace place : places) {
                                if (place != null && place.getId() != null && seen.add(place.getId())) {
                                        result.add(place);
                                }
                        }
                }

                log.info("Hierarchy tourist places fetched: {}", result.size());
                return result;
        }

        private List<Hotel> fetchHotels(List<Long> locationIds) {

                List<Hotel> result = new ArrayList<>();
                Set<Long> seen = new HashSet<>();

                if (locationIds == null) {
                        return result;
                }

                for (Long locationId : locationIds) {
                        if (locationId == null) {
                                continue;
                        }

                        List<Hotel> hotels = hotelRepository.findActiveByLocationId(locationId);
                        if (hotels == null) {
                                continue;
                        }

                        for (Hotel hotel : hotels) {
                                if (hotel != null && hotel.getId() != null && seen.add(hotel.getId())) {
                                        result.add(hotel);
                                }
                        }
                }

                log.info("Hierarchy hotels fetched: {}", result.size());
                return result;
        }

        private List<Restaurant> fetchRestaurants(List<Long> locationIds) {

                List<Restaurant> result = new ArrayList<>();
                Set<Long> seen = new HashSet<>();

                if (locationIds == null) {
                        return result;
                }

                for (Long locationId : locationIds) {
                        if (locationId == null) {
                                continue;
                        }

                        List<Restaurant> restaurants = restaurantRepository
                                        .findByLocationIdAndActiveTrue(locationId);

                        if (restaurants == null) {
                                continue;
                        }

                        for (Restaurant restaurant : restaurants) {
                                if (restaurant != null && restaurant.getId() != null && seen.add(restaurant.getId())) {
                                        result.add(restaurant);
                                }
                        }
                }

                log.info("Hierarchy restaurants fetched: {}", result.size());
                return result;
        }

        // ==========================================================
        // LOCATION HIERARCHY
        // ==========================================================

        private List<Long> getScopeLocationIds(Location selectedLocation) {

                if (selectedLocation == null || selectedLocation.getId() == null) {
                        return new ArrayList<>();
                }

                List<Location> allLocations = locationRepository.findAll();
                Map<Long, List<Location>> childrenByParent = new HashMap<>();

                for (Location candidate : allLocations) {
                        if (candidate == null || candidate.getId() == null) {
                                continue;
                        }

                        Object parentObject = invokeGetter(candidate, "parent");
                        Object parentIdObject = invokeGetter(parentObject, "id");

                        if (parentIdObject instanceof Number number) {
                                Long parentId = number.longValue();
                                childrenByParent
                                                .computeIfAbsent(parentId, ignored -> new ArrayList<>())
                                                .add(candidate);
                        }
                }

                List<Long> ids = new ArrayList<>();
                Set<Long> visited = new HashSet<>();
                collectScopeIds(
                                selectedLocation.getId(),
                                childrenByParent,
                                visited,
                                ids);

                return ids;
        }

        @SuppressWarnings("null")
        private void collectScopeIds(
                        Long parentId,
                        Map<Long, List<Location>> childrenByParent,
                        Set<Long> visited,
                        List<Long> result) {

                if (parentId == null || !visited.add(parentId)) {
                        return;
                }

                result.add(parentId);

                List<Location> children = childrenByParent.get(parentId);
                if (children == null || children.isEmpty()) {
                        return;
                }

                children.sort(Comparator.comparing(
                                Location::getCityName,
                                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));

                for (Location child : children) {
                        if (child != null && child.getId() != null) {
                                collectScopeIds(
                                                child.getId(),
                                                childrenByParent,
                                                visited,
                                                result);
                        }
                }
        }

        private String getLocationTypeName(Location location) {
                Object type = invokeGetter(location, "locationType");
                return type == null ? "UNKNOWN" : type.toString().trim().toUpperCase();
        }

        private boolean isBroadLocation(Location location) {
                String type = getLocationTypeName(location);
                return "STATE".equals(type) || "COUNTRY".equals(type);
        }

        /**
         * State/country selection needs destination discovery rather than simply
         * taking the globally highest-rated places. A diversity pass prevents a
         * Bihar/Goa-level itinerary from selecting several places from one small
         * cluster while ignoring other useful destinations.
         */
        private List<TouristPlace> prepareTouristPlacesForLocation(
                        List<TouristPlace> places,
                        Location selectedLocation,
                        Itinerary itinerary,
                        int optionNumber) {

                if (places == null || places.isEmpty()) {
                        return new ArrayList<>();
                }

                int maxPlaces = MAX_PLACES_PER_DAY * safeTotalDays(itinerary);

                if (!isBroadLocation(selectedLocation)) {
                        return places.stream()
                                        .limit(Math.max(1, maxPlaces))
                                        .toList();
                }

                return selectBroadLocationPlaces(places, maxPlaces, optionNumber);
        }

        private List<TouristPlace> selectBroadLocationPlaces(
                        List<TouristPlace> places,
                        int limit,
                        int optionNumber) {

                int target = Math.max(1, limit);
                List<TouristPlace> remaining = new ArrayList<>(places);
                List<TouristPlace> selected = new ArrayList<>();

                remaining.sort(Comparator.comparingDouble(
                                place -> -scoreTouristPlace(place, 0.0, optionNumber, true)));

                if (!remaining.isEmpty()) {
                        selected.add(remaining.remove(0));
                }

                while (selected.size() < target && !remaining.isEmpty()) {
                        TouristPlace best = null;
                        double bestScore = Double.NEGATIVE_INFINITY;

                        for (TouristPlace candidate : remaining) {
                                double quality = scoreTouristPlace(
                                                candidate,
                                                0.0,
                                                optionNumber,
                                                selected.size() < MAX_PRIMARY_TOURIST_PLACES_PER_DAY);

                                double diversity = 0.0;
                                if (hasCoordinates(candidate)) {
                                        double nearestDistance = Double.MAX_VALUE;

                                        for (TouristPlace alreadySelected : selected) {
                                                if (!hasCoordinates(alreadySelected)) {
                                                        continue;
                                                }

                                                double distance = distanceKm(
                                                                true,
                                                                getCoordinate(candidate, "latitude"),
                                                                getCoordinate(candidate, "longitude"),
                                                                getCoordinate(alreadySelected, "latitude"),
                                                                getCoordinate(alreadySelected, "longitude"));

                                                nearestDistance = Math.min(nearestDistance, distance);
                                        }

                                        if (nearestDistance != Double.MAX_VALUE) {
                                                diversity = Math.min(1.0, nearestDistance / 50.0);
                                        }
                                }

                                double combinedScore = quality * 0.70 + diversity * 0.30;

                                if (combinedScore > bestScore) {
                                        bestScore = combinedScore;
                                        best = candidate;
                                }
                        }

                        if (best == null) {
                                break;
                        }

                        selected.add(best);
                        remaining.remove(best);
                }

                return selected;
        }

        // ==========================================================
        // FILTER TOURIST PLACES
        // ==========================================================

        private List<TouristPlace> filterTouristPlaces(
                        List<TouristPlace> places,
                        Itinerary itinerary) {

                if (places == null || places.isEmpty()) {
                        return new ArrayList<>();
                }

                BigDecimal totalBudget = safeBigDecimal(itinerary.getTotalBudget());
                BigDecimal placesBudget = totalBudget.multiply(PLACES_BUDGET_PERCENTAGE);

                List<TouristPlace> activePlaces = places.stream()
                                .filter(Objects::nonNull)
                                .filter(place -> Boolean.TRUE.equals(place.getActive()))
                                .toList();

                List<TouristPlace> budgetPlaces = activePlaces.stream()
                                .filter(place -> place.getPrice() == null
                                                || place.getPrice().compareTo(placesBudget) <= 0)
                                .toList();

                List<TouristPlace> travelTypePlaces = budgetPlaces.stream()
                                .filter(place -> filterByTravelType(place, itinerary))
                                .toList();

                List<TouristPlace> seasonPlaces = travelTypePlaces.stream()
                                .filter(place -> filterByBestSeason(place, itinerary))
                                .toList();

                if (!seasonPlaces.isEmpty()) {
                        return seasonPlaces;
                }

                log.warn(
                                "No tourist place matched strict filters for budget {}. Applying fallback.",
                                placesBudget);

                List<TouristPlace> fallback = activePlaces.stream()
                                .filter(place -> filterByTravelType(place, itinerary))
                                .filter(place -> filterByBestSeason(place, itinerary))
                                .toList();

                return fallback.isEmpty() ? new ArrayList<>(activePlaces) : fallback;
        }

        // ==========================================================
        // TRAVEL TYPE FILTER
        // ==========================================================

        @SuppressWarnings("null")
        private boolean filterByTravelType(
                        TouristPlace place,
                        Itinerary itinerary) {

                if (place.getTravelTypes() == null ||
                                place.getTravelTypes().isEmpty()) {

                        return true;
                }

                if (itinerary.getTravelType() == null) {
                        return true;
                }

                String requestedTravelType = itinerary.getTravelType()
                                .name()
                                .trim()
                                .toUpperCase();

                return place.getTravelTypes()
                                .stream()
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .map(String::toUpperCase)
                                .anyMatch(type -> type.equals(requestedTravelType));
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

                itinerary.setRestaurantsPerDay(
                                request.getRestaurantsPerDay() != null
                                                ? Math.max(
                                                                1,
                                                                request.getRestaurantsPerDay())
                                                : 1);
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

        // ==========================================================
        // COST CALCULATION
        // ==========================================================

        private BigDecimal calculateTotalEstimatedCost(
                        Itinerary itinerary) {

                if (itinerary == null) {
                        return BigDecimal.ZERO;
                }

                List<ItineraryDay> days = itinerary.getItineraryDays();

                if (days == null || days.isEmpty()) {
                        return BigDecimal.ZERO;
                }

                BigDecimal totalCost = BigDecimal.ZERO;

                for (ItineraryDay day : days) {

                        if (day == null) {
                                continue;
                        }

                        List<ItineraryPlace> places = day.getItineraryPlaces();

                        if (places == null || places.isEmpty()) {
                                continue;
                        }

                        for (ItineraryPlace place : places) {

                                if (place == null) {
                                        continue;
                                }

                                BigDecimal cost = place.getEstimatedCost();

                                if (cost == null
                                                || cost.signum() <= 0) {
                                        continue;
                                }

                                totalCost = totalCost.add(cost);
                        }
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
        public ItineraryResponse getById(@NonNull Long id) {

                Itinerary itinerary = itineraryRepository
                                .findWithDetailsById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Itinerary not found with id: " + id));

                validateOwnership(itinerary);

                return itineraryMapper.toResponse(itinerary);
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
        public UserSummaryResponse getUserSummaryByUserId(Long userId) {

                // =========================================================
                // 1. GET ALL SUMMARY AGGREGATES IN ONE QUERY
                // =========================================================

                UserSummaryProjection summary = itineraryRepository.getUserSummary(
                                userId,
                                ItineraryStatus.COMPLETED);

                // =========================================================
                // 2. SAFE DEFAULT VALUES
                // =========================================================

                long totalTrips = 0L;

                long completedTrips = 0L;

                BigDecimal totalBudget = BigDecimal.ZERO;

                BigDecimal usedBudget = BigDecimal.ZERO;

                if (summary != null) {

                        if (summary.getTotalTrips() != null) {
                                totalTrips = summary.getTotalTrips();
                        }

                        if (summary.getCompletedTrips() != null) {
                                completedTrips = summary.getCompletedTrips();
                        }

                        if (summary.getTotalBudget() != null) {
                                totalBudget = summary.getTotalBudget();
                        }

                        if (summary.getUsedBudget() != null) {
                                usedBudget = summary.getUsedBudget();
                        }
                }

                // =========================================================
                // 3. VISITED PLACES
                // =========================================================

                long placesVisited = itineraryPlaceRepository.countVisitedPlacesByUserId(
                                userId);

                // =========================================================
                // 4. REMAINING BUDGET
                // =========================================================

                BigDecimal remainingBudget = totalBudget.subtract(usedBudget);

                if (remainingBudget.compareTo(BigDecimal.ZERO) < 0) {
                        remainingBudget = BigDecimal.ZERO;
                }

                // =========================================================
                // 5. BUDGET PERCENTAGE
                // =========================================================

                BigDecimal percentageUsed = calculateBudgetPercentage(
                                usedBudget,
                                totalBudget);

                // =========================================================
                // 6. UPCOMING TRIP
                // =========================================================

                // Pageable limitOne = PageRequest.of(0, 1);
                List<Itinerary> upcomingTrips = itineraryRepository.findUpcomingTrips(
                                userId,
                                LocalDate.now(),
                                List.of(
                                                ItineraryStatus.COMPLETED,
                                                ItineraryStatus.CONCERNED,
                                                ItineraryStatus.DRAFT));

                UpcomingTripResponse upcomingTrip = null;

                if (!upcomingTrips.isEmpty()) {
                        upcomingTrip = buildUpcomingTripResponse(
                                        upcomingTrips.get(0));
                }

                // =========================================================
                // 7. FINAL RESPONSE
                // =========================================================

                return UserSummaryResponse.builder()

                                .totalTrips(
                                                Math.toIntExact(totalTrips))

                                .completedTrips(
                                                Math.toIntExact(completedTrips))

                                .placesVisited(
                                                Math.toIntExact(placesVisited))

                                // Country information is not available
                                // in current Location entity.
                                .countriesVisited(0)

                                .budget(
                                                BudgetSummaryResponse.builder()
                                                                .total(totalBudget)
                                                                .used(usedBudget)
                                                                .remaining(remainingBudget)
                                                                .percentageUsed(percentageUsed)
                                                                .build())

                                .upcomingTrip(upcomingTrip)

                                .travelPreferences(
                                                List.of())

                                .build();
        }

        private BigDecimal calculateBudgetPercentage(
                        BigDecimal used,
                        BigDecimal total) {

                if (used == null ||
                                total == null ||
                                total.compareTo(BigDecimal.ZERO) <= 0) {

                        return BigDecimal.ZERO;
                }

                BigDecimal percentage = used.multiply(BigDecimal.valueOf(100))
                                .divide(
                                                total,
                                                2,
                                                RoundingMode.HALF_UP);

                if (percentage.compareTo(
                                BigDecimal.valueOf(100)) > 0) {

                        return BigDecimal.valueOf(100);
                }

                if (percentage.compareTo(
                                BigDecimal.ZERO) < 0) {

                        return BigDecimal.ZERO;
                }

                return percentage;
        }

        private UpcomingTripResponse buildUpcomingTripResponse(
                        Itinerary itinerary) {

                if (itinerary == null) {
                        return null;
                }

                return UpcomingTripResponse.builder()

                                .id(
                                                itinerary.getId())

                                .title(
                                                itinerary.getTitle())

                                .locationName(
                                                getLocationName(itinerary))

                                .startDate(
                                                itinerary.getStartDate())

                                .endDate(
                                                itinerary.getEndDate())

                                .totalDays(
                                                itinerary.getTotalDays())

                                .totalBudget(
                                                itinerary.getTotalBudget())

                                .estimatedCost(
                                                itinerary.getEstimatedCost())

                                .itineraryStatus(
                                                itinerary.getItineraryStatus())

                                .build();
        }

        private String getLocationName(
                        Itinerary itinerary) {

                if (itinerary == null ||
                                itinerary.getLocation() == null) {

                        return null;
                }

                String city = itinerary.getLocation().getCityName();

                String state = itinerary.getLocation().getStateName();

                if (city != null &&
                                !city.isBlank() &&
                                state != null &&
                                !state.isBlank()) {

                        return city + ", " + state;
                }

                if (city != null &&
                                !city.isBlank()) {

                        return city;
                }

                if (state != null &&
                                !state.isBlank()) {

                        return state;
                }

                return null;
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
                Long currentUserId = securityUtils.getCurrentUserId();
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