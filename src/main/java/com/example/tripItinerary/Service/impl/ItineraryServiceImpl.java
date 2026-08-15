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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.ItineraryRequest;
import com.example.tripItinerary.DTO.response.ItineraryResponse;
import com.example.tripItinerary.Entity.Hotel;
import com.example.tripItinerary.Entity.Itinerary;
import com.example.tripItinerary.Entity.ItineraryDay;
import com.example.tripItinerary.Entity.ItineraryPlace;
import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Entity.Restaurant;
import com.example.tripItinerary.Entity.TouristPlace;
import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.Mapper.ItineraryMapper;
import com.example.tripItinerary.Repo.HotelRepository;
import com.example.tripItinerary.Repo.ItineraryDayRepository;
import com.example.tripItinerary.Repo.ItineraryPlaceRepository;
import com.example.tripItinerary.Repo.ItineraryRepository;
import com.example.tripItinerary.Repo.LocationRepository;
import com.example.tripItinerary.Repo.RestaurantRepository;
import com.example.tripItinerary.Repo.TouristPlaceRepository;
import com.example.tripItinerary.Repo.UserRepository;
import com.example.tripItinerary.Service.ItineraryService;
import com.example.tripItinerary.enums.ItineraryStatus;
import com.example.tripItinerary.enums.PlaceType;
import com.example.tripItinerary.enums.TravelType;
import com.example.tripItinerary.exception.ResourceNotFoundException;
import com.example.tripItinerary.exception.UnauthorizedException;
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
        private final TouristPlaceRepository touristPlaceRepository;
        private final HotelRepository hotelRepository;
        private final RestaurantRepository restaurantRepository;
        private final ItineraryDayRepository itineraryDayRepository;
        private final ItineraryPlaceRepository itineraryPlaceRepository;
        private final ItineraryMapper itineraryMapper;
        private final SecurityUtils securityUtils;

        // ==========================================================
        // CONSTANTS
        // ==========================================================

        private static final int MAX_DAYS = 30;
        private static final int MAX_PLACES_PER_DAY = 5;
        private static final int DEFAULT_VISIT_MINUTES = 90;
        private static final int DEFAULT_TRAVEL_MINUTES = 30;

        // ==========================================================
        // BUDGET DISTRIBUTION CONSTANTS
        // ==========================================================

        private static final BigDecimal HOTEL_BUDGET_PERCENTAGE = BigDecimal.valueOf(0.30); // 30%
        private static final BigDecimal RESTAURANT_BUDGET_PERCENTAGE = BigDecimal.valueOf(0.30); // 30%
        private static final BigDecimal PLACES_BUDGET_PERCENTAGE = BigDecimal.valueOf(0.25); // 25%
        private static final BigDecimal BUFFER_BUDGET_PERCENTAGE = BigDecimal.valueOf(0.15);

        // ==========================================================
        // CREATE SMART ITINERARY
        // ==========================================================

        @SuppressWarnings("null")
        @Override
        public ItineraryResponse create(ItineraryRequest request) {
                log.info("Creating Smart Itinerary");

                // STEP-1: Validate Request
                validateRequest(request);
                System.out.println("Validated Itinerary Request: " + request);

                // STEP-2: Fetch User
                User user = getUser(request.getUserId());
                System.out.println("Fetched User: " + user.getId() + ", Email: " + user.getEmail() + ", Full Name: "
                                + user.getFullName());
                // STEP-3: Fetch Location
                Location location = getLocation(request.getLocationId());
                System.out.println("Fetched Location: " + location.getId() + ", Name: " + location.getCityName());

                // STEP-4: Convert DTO -> Entity
                Itinerary itinerary = itineraryMapper.toEntity(request);
                System.out.println("Converted Itinerary DTO to Entity: " + itinerary.getTitle() + ", Total Days: "
                                + itinerary.getTotalDays() + ", Total Budget: " + itinerary.getTotalBudget());
                buildItinerary(itinerary, request, user, location);

                // STEP-5: Save Basic Itinerary
                itinerary = itineraryRepository.save(itinerary);
                System.out.println("Successfully saved into db: " + itinerary.getId());

                // STEP-6: Create Default Days
                createDefaultDays(itinerary);
                System.out.println("Default days created for Itinerary: " + itinerary.getId());

                // STEP-7: Fetch Tourist Places
                List<TouristPlace> touristPlaces = fetchTouristPlaces(location);
                System.out.println("Fetched Tourist Places: " + touristPlaces.size() + " places found for location: "
                                + location.getCityName());

                // STEP-8: Filter Tourist Places
                touristPlaces = filterTouristPlaces(touristPlaces, itinerary);
                System.out.println("Filtered Tourist Places: " + touristPlaces.size() + " places remaining.");
                // STEP-9: Sort Tourist Places
                touristPlaces = sortTouristPlaces(touristPlaces);
                System.out.println("Sorted Tourist Places.");

                // STEP-10: Assign Places To Days
                assignPlacesToDays(itinerary, touristPlaces);

                System.out.println("Assigned Tourist Places to Itinerary Days.");

                // STEP-11: Assign Hotels
                List<Hotel> hotels = assignHotels(itinerary, location);
                // ✅ NEW: Save hotels to itinerary
                assignHotelsToItinerary(itinerary, hotels);

                // STEP-12: Assign Restaurants
                List<Restaurant> restaurants = assignRestaurants(itinerary, location);
                // ✅ NEW: Save restaurants to itinerary
                assignRestaurantsToItinerary(itinerary, restaurants);
                System.out.println("Assigned Hotels: " + hotels.size() + " hotels assigned for itinerary.");
                // STEP-12: Assign Restaurants

                System.out.println(
                                "Assigned Restaurants: " + restaurants.size() + " restaurants assigned for itinerary.");
                // STEP-13: Calculate Estimated Cost
                BigDecimal estimatedCost = calculateTotalEstimatedCost(
                                itinerary, hotels, restaurants, touristPlaces);
                itinerary.setEstimatedCost(estimatedCost);

                System.out.println("Estimated Cost: " + estimatedCost);

                // STEP-14: Calculate Remaining Budget
                BigDecimal remainingBudget = calculateRemainingBudget(itinerary);
                itinerary.setRemainingBudget(remainingBudget);
                log.info("Remaining Budget: {}", remainingBudget);

                System.out.println("Remaining Budget: " + remainingBudget);
                // STEP-15: Update Status
                updateStatusAutomatically(itinerary);

                System.out.println("Itinerary Status: " + itinerary.getItineraryStatus());
                // STEP-16: Save Final Itinerary
                itinerary = itineraryRepository.save(itinerary);

                // STEP-17: Return Response
                return itineraryMapper.toResponse(itinerary);
        }

        // ==========================================================
        // ASSIGN HOTELS TO ITINERARY PLACES - ✅ FIX
        // ==========================================================

        private void assignHotelsToItinerary(Itinerary itinerary, List<Hotel> hotels) {
                if (hotels.isEmpty()) {
                        log.warn("No hotels to assign to itinerary.");
                        return;
                }

                List<ItineraryDay> itineraryDays = itineraryDayRepository
                                .findByItineraryIdOrderByDayNumber(itinerary.getId());

                if (itineraryDays.isEmpty()) {
                        log.warn("No itinerary days found to assign hotels.");
                        return;
                }

                // Assign 1 hotel per day (or distribute evenly)
                int hotelIndex = 0;
                for (ItineraryDay day : itineraryDays) {
                        if (hotelIndex >= hotels.size()) {
                                break;
                        }

                        Hotel hotel = hotels.get(hotelIndex);

                        // Create ItineraryPlace for Hotel
                        ItineraryPlace hotelPlace = ItineraryPlace.builder()
                                        .itineraryDay(day)
                                        .placeType(PlaceType.HOTEL)
                                        .referenceId(hotel.getId())
                                        .visitOrder(99) // Hotels at the end of day
                                        .plannedStartTime(LocalTime.of(20, 0)) // Evening check-in
                                        .plannedEndTime(LocalTime.of(21, 0))
                                        .estimatedCost(hotel.getPricePerNight() != null ? hotel.getPricePerNight()
                                                        : BigDecimal.ZERO)
                                        .travelTimeMinutes(0)
                                        .completed(false)
                                        .notes("Hotel: " + hotel.getHotelName())
                                        .build();

                        day.addPlace(hotelPlace);
                        itineraryDayRepository.save(day);
                        hotelIndex++;
                }

                log.info("{} hotels assigned to itinerary.", hotelIndex);
        }

        // ==========================================================
        // ASSIGN RESTAURANTS TO ITINERARY PLACES - ✅ FIX
        // ==========================================================

        private void assignRestaurantsToItinerary(Itinerary itinerary, List<Restaurant> restaurants) {
                if (restaurants.isEmpty()) {
                        log.warn("No restaurants to assign to itinerary.");
                        return;
                }

                List<ItineraryDay> itineraryDays = itineraryDayRepository
                                .findByItineraryIdOrderByDayNumber(itinerary.getId());

                if (itineraryDays.isEmpty()) {
                        log.warn("No itinerary days found to assign restaurants.");
                        return;
                }

                // Assign restaurants to days
                int restaurantIndex = 0;
                for (ItineraryDay day : itineraryDays) {
                        if (restaurantIndex >= restaurants.size()) {
                                break;
                        }

                        Restaurant restaurant = restaurants.get(restaurantIndex);

                        // Create ItineraryPlace for Restaurant (Lunch)
                        ItineraryPlace restaurantPlace = ItineraryPlace.builder()
                                        .itineraryDay(day)
                                        .placeType(PlaceType.RESTAURANT)
                                        .referenceId(restaurant.getId())
                                        .visitOrder(98) // Before hotel
                                        .plannedStartTime(LocalTime.of(13, 0)) // Lunch time
                                        .plannedEndTime(LocalTime.of(14, 0))
                                        .estimatedCost(restaurant.getAverageCostPerPerson() != null
                                                        ? restaurant.getAverageCostPerPerson()
                                                        : BigDecimal.ZERO)
                                        .travelTimeMinutes(0)
                                        .completed(false)
                                        .notes("Restaurant: " + restaurant.getRestaurantName())
                                        .build();

                        day.addPlace(restaurantPlace);
                        itineraryDayRepository.save(day);
                        restaurantIndex++;
                }

                log.info("{} restaurants assigned to itinerary.", restaurantIndex);
        }

        // ==========================================================
        // UPDATE ITINERARY
        // ==========================================================

        @Override
        public ItineraryResponse update(@NonNull Long id, @NonNull ItineraryRequest request) {
                log.info("Updating itinerary with id={}", id);

                validateRequest(request);

                Itinerary itinerary = getItinerary(id);
                validateOwnership(itinerary);

                User user = getUser(request.getUserId());
                Location location = getLocation(request.getLocationId());

                buildItinerary(itinerary, request, user, location);
                itinerary.setUpdatedAt(LocalDateTime.now());

                // Recalculate everything
                List<TouristPlace> touristPlaces = fetchTouristPlaces(location);
                touristPlaces = filterTouristPlaces(touristPlaces, itinerary);
                touristPlaces = sortTouristPlaces(touristPlaces);

                // Clear existing days and recreate
                itineraryDayRepository.deleteByItineraryId(itinerary.getId());
                createDefaultDays(itinerary);
                assignPlacesToDays(itinerary, touristPlaces);

                List<Hotel> hotels = assignHotels(itinerary, location);
                List<Restaurant> restaurants = assignRestaurants(itinerary, location);

                BigDecimal estimatedCost = calculateTotalEstimatedCost(
                                itinerary, hotels, restaurants, touristPlaces);
                itinerary.setEstimatedCost(estimatedCost);

                BigDecimal remainingBudget = calculateRemainingBudget(itinerary);
                itinerary.setRemainingBudget(remainingBudget);

                updateStatusAutomatically(itinerary);

                Itinerary updatedItinerary = itineraryRepository.save(itinerary);
                log.info("Itinerary updated successfully with id={}", updatedItinerary.getId());

                return itineraryMapper.toResponse(updatedItinerary);
        }

        // ==========================================================
        // GET ITINERARY BY ID
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public ItineraryResponse getById(@NonNull Long id) {
                log.info("Fetching itinerary with id={}", id);
                Itinerary itinerary = getItinerary(id);
                return itineraryMapper.toResponse(itinerary);
        }

        // ==========================================================
        // GET MY ITINERARIES
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public List<ItineraryResponse> getMyItineraries() {
                log.info("Fetching logged-in user itineraries...");
                Long userId = securityUtils.getCurrentUserId();
                List<Itinerary> itineraries = itineraryRepository.findByUserIdOrderByCreatedAtDesc(userId);
                return itineraries.stream()
                                .map(itineraryMapper::toResponse)
                                .toList();
        }

        // ==========================================================
        // DELETE ITINERARY
        // ==========================================================

        @SuppressWarnings("null")
        @Override
        public void delete(Long id) {
                log.info("Deleting itinerary id={}", id);

                Itinerary itinerary = getItinerary(id);
                validateOwnership(itinerary);

                // Delete associated days and places first
                itineraryPlaceRepository.deleteByItineraryDayItineraryId(id);
                itineraryDayRepository.deleteByItineraryId(id);

                // Delete itinerary
                itineraryRepository.delete(itinerary);
                log.info("Itinerary deleted successfully.");
        }

        // ==========================================================
        // VALIDATION METHODS
        // ==========================================================

        private void validateRequest(ItineraryRequest request) {
                Objects.requireNonNull(request, "Itinerary request cannot be null.");

                if (request.getUserId() == null) {
                        throw new IllegalArgumentException("User Id is required.");
                }

                if (request.getLocationId() == null) {
                        throw new IllegalArgumentException("Location Id is required.");
                }

                if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                        throw new IllegalArgumentException("Trip title is required.");
                }

                if (request.getTotalDays() == null || request.getTotalDays() <= 0) {
                        throw new IllegalArgumentException("Total days must be greater than zero.");
                }

                if (request.getTotalDays() > MAX_DAYS) {
                        throw new IllegalArgumentException("Maximum trip duration is " + MAX_DAYS + " days.");
                }

                if (request.getTravelType() == null) {
                        throw new IllegalArgumentException("Travel type is required.");
                }

                if (request.getTotalBudget() == null) {
                        throw new IllegalArgumentException("Total budget is required.");
                }

                if (request.getTotalBudget().compareTo(BigDecimal.ZERO) <= 0) {
                        throw new IllegalArgumentException("Budget must be greater than zero.");
                }

                if (request.getEstimatedCost() != null
                                && request.getEstimatedCost().compareTo(BigDecimal.ZERO) < 0) {
                        throw new IllegalArgumentException("Estimated cost cannot be negative.");
                }
        }

        private void validateOwnership(Itinerary itinerary) {
                Long currentUserId = securityUtils.getCurrentUserId();
                if (!Objects.equals(itinerary.getUser().getId(), currentUserId)) {
                        throw new UnauthorizedException("You are not authorized to access this itinerary.");
                }
        }

        // ==========================================================
        // BUILD ITINERARY
        // ==========================================================

        private void buildItinerary(Itinerary itinerary, ItineraryRequest request,
                        User user, Location location) {
                itinerary.setUser(user);
                itinerary.setLocation(location);
                itinerary.setTitle(request.getTitle().trim());
                itinerary.setDescription(request.getDescription());
                itinerary.setTravelType(request.getTravelType());
                itinerary.setTotalDays(request.getTotalDays());
                itinerary.setTotalBudget(request.getTotalBudget());
                itinerary.setEstimatedCost(request.getEstimatedCost() == null
                                ? BigDecimal.ZERO
                                : request.getEstimatedCost());
                itinerary.setRemainingBudget(request.getTotalBudget());
                itinerary.setItineraryStatus(ItineraryStatus.GENERATED);
                itinerary.setStartDate(request.getStartDate());
                itinerary.setEndDate(request.getEndDate());
        }

        // ==========================================================
        // HELPER METHODS FOR ENTITY FETCHING
        // ==========================================================

        private User getUser(@NonNull Long userId) {
                return userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        }

        private Location getLocation(@NonNull Long locationId) {
                return locationRepository.findById(locationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id: " + locationId));
        }

        private Itinerary getItinerary(@NonNull Long itineraryId) {
                return itineraryRepository.findById(itineraryId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Itinerary not found with id: " + itineraryId));
        }

        // ==========================================================
        // CREATE DEFAULT DAYS
        // ==========================================================

        private void createDefaultDays(Itinerary itinerary) {
                log.info("Creating {} itinerary days.", itinerary.getTotalDays());

                List<ItineraryDay> itineraryDays = new ArrayList<>();

                // ✅ Itinerary ki start date lo
                LocalDate startDate = itinerary.getStartDate();

                // Agar start date null hai toh current date use karo (fallback)
                if (startDate == null) {
                        log.warn("Itinerary start date is null, using current date as fallback");
                        startDate = LocalDate.now();
                }

                for (int day = 1; day <= itinerary.getTotalDays(); day++) {
                        // ✅ Har day ke liye date calculate karo: startDate + (day - 1)
                        LocalDate travelDate = startDate.plusDays(day - 1);

                        ItineraryDay itineraryDay = ItineraryDay.builder()
                                        .itinerary(itinerary)
                                        .dayNumber(day)
                                        .title(generateDayTitle(day))
                                        .notes("Trip activities for Day " + day)
                                        .travelDate(travelDate) // ✅ travelDate set karo
                                        .build();

                        itineraryDays.add(itineraryDay);
                        log.debug("Day {}: travelDate = {}", day, travelDate);
                }

                itineraryDayRepository.saveAll(itineraryDays);
                itinerary.setItineraryDays(itineraryDays);
                log.info("{} itinerary days created successfully with travel dates.", itineraryDays.size());
        }

        private String generateDayTitle(int dayNumber) {
                return switch (dayNumber) {
                        case 1 -> "Arrival & Local Sightseeing";
                        case 2 -> "Explore Famous Attractions";
                        case 3 -> "Adventure & Outdoor Activities";
                        case 4 -> "Cultural & Heritage Tour";
                        case 5 -> "Shopping & Food Experience";
                        default -> "Day " + dayNumber + " - Exploration";
                };
        }

        // ==========================================================
        // FETCH & FILTER TOURIST PLACES
        // ==========================================================

        private List<TouristPlace> fetchTouristPlaces(Location location) {
                log.info("Fetching tourist places for location: {}", location.getCityName());

                List<TouristPlace> places = touristPlaceRepository.findByLocationIdAndActiveTrue(location.getId());

                if (places.isEmpty()) {
                        log.warn("No tourist places found for location: {}", location.getCityName());
                        return new ArrayList<>();
                }

                log.info("{} tourist places found.", places.size());
                return places;
        }

        private List<TouristPlace> filterTouristPlaces(List<TouristPlace> places, Itinerary itinerary) {
                log.info("Filtering tourist places...");

                if (places.isEmpty()) {
                        System.out.println("⚠️ No tourist places found!");
                        return places;
                }

                // ✅ Calculate places budget (25% of total budget)
                BigDecimal totalBudget = itinerary.getTotalBudget();
                BigDecimal placesBudget = totalBudget.multiply(PLACES_BUDGET_PERCENTAGE);

                System.out.println("================== PLACES BUDGET ==================");
                System.out.println("Total Budget: ₹" + totalBudget);
                System.out.println("Places Budget (25%): ₹" + placesBudget);
                System.out.println("===================================================");

                // Step 1: Active
                List<TouristPlace> activePlaces = places.stream()
                                .filter(TouristPlace::getActive)
                                .toList();
                System.out.println("1️⃣ After Active Filter: " + activePlaces.size());

                // Step 2: Budget - Use placesBudget instead of total budget
                List<TouristPlace> budgetPlaces = activePlaces.stream()
                                .filter(place -> place.getPrice() == null
                                                || place.getPrice().compareTo(placesBudget) <= 0)
                                .toList();
                System.out.println("2️⃣ After Budget Filter: " + budgetPlaces.size());

                // ✅ Show which places passed budget filter
                budgetPlaces.forEach(place -> System.out.println("   ✅ " + place.getPlaceName() +
                                " (₹" + place.getPrice() + " ≤ ₹" + placesBudget + ")"));

                // Step 3: Travel Type
                List<TouristPlace> travelTypePlaces = budgetPlaces.stream()
                                .filter(place -> filterByTravelType(place, itinerary))
                                .toList();
                System.out.println("3️⃣ After Travel Type Filter: " + travelTypePlaces.size());

                // Step 4: Best Season
                List<TouristPlace> seasonPlaces = travelTypePlaces.stream()
                                .filter(place -> filterByBestSeason(place, itinerary))
                                .toList();
                System.out.println("4️⃣ After Best Season Filter: " + seasonPlaces.size());

                // Step 5: Limit
                int maxPlaces = MAX_PLACES_PER_DAY * itinerary.getTotalDays();
                List<TouristPlace> finalPlaces = seasonPlaces.stream()
                                .limit(maxPlaces)
                                .toList();
                System.out.println("5️⃣ Final Places: " + finalPlaces.size() + " (Limit: " + maxPlaces + ")");

                finalPlaces.forEach(place -> System.out.println("   🏆 " + place.getPlaceName() +
                                " (Rating: " + place.getAverageRating() +
                                ", Price: ₹" + place.getPrice() + ")"));

                System.out.println("================== END DEBUG ==================");

                return finalPlaces;
        }

        @Override
        public List<ItineraryResponse> getItineraryByUserId(Long userId) {
                log.info("Fetching itineraries for user: {}", userId);

                return itineraryRepository
                                .findByUserIdOrderByCreatedAtDesc(userId)
                                .stream()
                                .map(itineraryMapper::toResponse)
                                .toList();
        }
        // private List<TouristPlace> filterTouristPlaces(List<TouristPlace> places,
        // Itinerary itinerary) {
        // log.info("Filtering tourist places...");

        // if (places.isEmpty()) {
        // System.out.println("⚠️ No tourist places found!");
        // return places;
        // }

        // BigDecimal budget = itinerary.getTotalBudget();
        // System.out.println("================== FILTER DEBUG ==================");
        // System.out.println("Total Places: " + places.size());
        // System.out.println("Total Budget: " + budget);

        // // Step 1: Active
        // List<TouristPlace> activePlaces = places.stream()
        // .filter(TouristPlace::getActive)
        // .toList();
        // System.out.println("1️⃣ After Active Filter: " + activePlaces.size());

        // // Step 2: Rating (Commented)
        // // List<TouristPlace> ratedPlaces = activePlaces.stream()
        // // .filter(place -> place.getAverageRating() != null
        // // && place.getAverageRating().compareTo(BigDecimal.valueOf(4.0)) >= 0)
        // // .toList();
        // // System.out.println("2️⃣ After Rating Filter: " + ratedPlaces.size());

        // // Step 3: Budget
        // List<TouristPlace> budgetPlaces = activePlaces.stream()
        // .filter(place -> place.getPrice() == null
        // || place.getPrice().compareTo(budget) <= 0)
        // .toList();
        // System.out.println("3️⃣ After Budget Filter: " + budgetPlaces.size());

        // // ✅ Show which places passed budget filter
        // budgetPlaces.forEach(place -> System.out.println(
        // " ✅ " + place.getPlaceName() + " (₹" + place.getPrice() + " ≤ ₹" + budget +
        // ")"));

        // // Step 4: Travel Type
        // List<TouristPlace> travelTypePlaces = budgetPlaces.stream()
        // .filter(place -> filterByTravelType(place, itinerary))
        // .toList();
        // System.out.println("4️⃣ After Travel Type Filter: " +
        // travelTypePlaces.size());

        // // Step 5: Best Season
        // List<TouristPlace> seasonPlaces = travelTypePlaces.stream()
        // .filter(place -> filterByBestSeason(place, itinerary))
        // .toList();
        // System.out.println("5️⃣ After Best Season Filter: " + seasonPlaces.size());

        // // Step 6: Limit
        // int maxPlaces = MAX_PLACES_PER_DAY * itinerary.getTotalDays();
        // List<TouristPlace> finalPlaces = seasonPlaces.stream()
        // .limit(maxPlaces)
        // .toList();
        // System.out.println("6️⃣ Final Places: " + finalPlaces.size() + " (Limit: " +
        // maxPlaces + ")");

        // finalPlaces.forEach(place -> System.out.println(
        // " 🏆 " + place.getPlaceName() + " (Rating: " + place.getAverageRating() +
        // ")"));

        // System.out.println("================== END DEBUG ==================");

        // return finalPlaces;
        // }

        /**
         * Filter tourist places by travel type compatibility
         * Checks if the place supports the user's travel type
         */
        private boolean filterByTravelType(TouristPlace place, Itinerary itinerary) {
                // If place has travel types defined in entity
                // Check if the place supports the travel type
                // if (place.getTravelTypes() != null && !place.getTravelTypes().isEmpty()) {
                // TravelType travelType = itinerary.getTravelType();
                // return place.getTravelTypes().contains(travelType);
                // }

                // If place has no travel types defined, include it by default
                return true;
        }

        /**
         * Filter tourist places by best season to visit
         * Checks if current month is in the place's best visit months
         */
        private boolean filterByBestSeason(TouristPlace place, Itinerary itinerary) {
                System.out.println("Check the best season of tourist place: " + place.getBestVisitMonths());

                // Agar best visit months null ya empty hai, toh allow karo
                if (place.getBestVisitMonths() == null || place.getBestVisitMonths().isEmpty()) {
                        System.out.println("No best visit months defined, allowing: " + place.getPlaceName());
                        return true;
                }

                // Itinerary ki start date use karo
                LocalDate startDate = itinerary.getStartDate();
                if (startDate == null) {
                        System.out.println("Itinerary start date is null, using current date as fallback");
                        startDate = LocalDate.now();
                }

                Month tripMonth = startDate.getMonth();
                String tripMonthName = tripMonth.name();

                System.out.println("Trip Month: " + tripMonthName);
                System.out.println("Best Visit Months: " + place.getBestVisitMonths());

                // ✅ Case 1: Exact month match (e.g., "October")
                if (place.getBestVisitMonths().contains(tripMonthName)) {
                        System.out.println("✅ Exact month match found!");
                        return true;
                }

                // ✅ Case 2: Month range check (e.g., "October to March")
                String[] parts = place.getBestVisitMonths().split(" to ");
                if (parts.length == 2) {
                        String startMonth = parts[0].trim();
                        String endMonth = parts[1].trim();

                        int tripMonthNumber = tripMonth.getValue(); // 1-12
                        int startMonthNumber = Month.valueOf(startMonth.toUpperCase()).getValue();
                        int endMonthNumber = Month.valueOf(endMonth.toUpperCase()).getValue();

                        boolean isInRange;
                        if (startMonthNumber <= endMonthNumber) {
                                // Normal range: October(10) to March(3) - NO
                                // Normal range: January(1) to June(6) - YES
                                isInRange = tripMonthNumber >= startMonthNumber && tripMonthNumber <= endMonthNumber;
                        } else {
                                // Cross-year range: October(10) to March(3) - YES
                                isInRange = tripMonthNumber >= startMonthNumber || tripMonthNumber <= endMonthNumber;
                        }

                        System.out.println("Month Range: " + startMonth + " to " + endMonth);
                        System.out.println("Trip Month: " + tripMonth + " (" + tripMonthNumber + ")");
                        System.out.println("Is in range: " + isInRange);

                        return isInRange;
                }

                // ✅ Case 3: Comma-separated months (e.g., "October, November, December")
                if (place.getBestVisitMonths().contains(",")) {
                        String[] months = place.getBestVisitMonths().split(",");
                        for (String month : months) {
                                if (month.trim().equalsIgnoreCase(tripMonthName)) {
                                        System.out.println("✅ Month found in comma-separated list!");
                                        return true;
                                }
                        }
                }

                System.out.println("❌ No match found for month: " + tripMonthName);
                return false;
        }
        // ==========================================================
        // SORT TOURIST PLACES
        // ==========================================================

        @SuppressWarnings("null")
        private List<TouristPlace> sortTouristPlaces(List<TouristPlace> places) {
                log.info("Sorting tourist places...");

                return places.stream()
                                .sorted(Comparator
                                                .comparing(TouristPlace::getPopularityScore,
                                                                Comparator.nullsLast(Comparator.reverseOrder()))
                                                .thenComparing(TouristPlace::getAverageRating,
                                                                Comparator.nullsLast(Comparator.reverseOrder()))
                                                .thenComparing(TouristPlace::getPrice,
                                                                Comparator.nullsLast(Comparator.naturalOrder())))
                                .toList();
        }

        // ==========================================================
        // ASSIGN PLACES TO DAYS
        // ==========================================================

        @SuppressWarnings("null")
        private void assignPlacesToDays(Itinerary itinerary, List<TouristPlace> touristPlaces) {
                log.info("Assigning tourist places to itinerary days.");

                if (touristPlaces.isEmpty()) {
                        log.warn("No tourist places to assign.");
                        return;
                }

                List<ItineraryDay> itineraryDays = itineraryDayRepository
                                .findByItineraryIdOrderByDayNumber(itinerary.getId());

                if (itineraryDays.isEmpty()) {
                        throw new ResourceNotFoundException("No itinerary days found.");
                }

                int totalDays = itineraryDays.size();
                int totalPlaces = touristPlaces.size();
                int currentPlaceIndex = 0;
                int placesPerDay = Math.max(1, (int) Math.ceil((double) totalPlaces / totalDays));

                for (ItineraryDay day : itineraryDays) {
                        LocalTime currentTime = getDefaultStartTime();
                        int visitOrder = 1;
                        int placesAssigned = 0;

                        for (int i = 0; i < placesPerDay && currentPlaceIndex < totalPlaces
                                        && placesAssigned < MAX_PLACES_PER_DAY; i++) {

                                TouristPlace place = touristPlaces.get(currentPlaceIndex++);
                                ItineraryPlace itineraryPlace = buildItineraryPlace(day, place, visitOrder++,
                                                currentTime);
                                day.addPlace(itineraryPlace);

                                // Update time for next place
                                currentTime = itineraryPlace.getPlannedEndTime().plusMinutes(DEFAULT_TRAVEL_MINUTES);
                                placesAssigned++;
                        }

                        itineraryDayRepository.save(day);
                }

                log.info("Tourist place assignment completed.");
        }

        // ==========================================================
        // BUILD ITINERARY PLACE
        // ==========================================================

        private ItineraryPlace buildItineraryPlace(ItineraryDay day, TouristPlace place,
                        int visitOrder, LocalTime startTime) {
                Integer visitMinutes = place.getEstimatedVisitTimeMinutes() != null
                                ? place.getEstimatedVisitTimeMinutes()
                                : DEFAULT_VISIT_MINUTES;

                LocalTime endTime = startTime.plusMinutes(visitMinutes);

                return ItineraryPlace.builder()
                                .itineraryDay(day)
                                .placeType(PlaceType.TOURIST_PLACE)
                                .referenceId(place.getId())
                                .visitOrder(visitOrder)
                                .plannedStartTime(startTime)
                                .plannedEndTime(endTime)
                                .estimatedCost(place.getPrice() != null ? place.getPrice() : BigDecimal.ZERO)
                                .travelTimeMinutes(DEFAULT_TRAVEL_MINUTES)
                                .completed(false)
                                .notes(place.getPlaceName())
                                .build();
        }

        private LocalTime getDefaultStartTime() {
                return LocalTime.of(9, 0);
        }

        // ==========================================================
        // ASSIGN HOTELS
        // ==========================================================

        // @SuppressWarnings("null")
        // private List<Hotel> assignHotels(Itinerary itinerary, Location location) {
        // log.info("Assigning hotels for itinerary...");

        // List<Hotel> hotels = hotelRepository.findByLocationId(location.getId());

        // System.out.println("find hotels size according to location" + hotels.size());
        // if (hotels.isEmpty()) {
        // log.warn("No hotels found for location: {}", location.getCityName());
        // return new ArrayList<>();
        // }

        // // Filter hotels based on budget
        // BigDecimal budget = itinerary.getTotalBudget();
        // System.out.println("Current total budget for hotels is " + budget);
        // BigDecimal hotelBudget = budget.multiply(BigDecimal.valueOf(0.3)); // 30% for
        // hotels
        // System.out.println("Assign budget for hotels is " + hotelBudget);

        // List<Hotel> assignedHotel = hotels.stream()
        // .filter(hotel -> hotel.getPricePerNight() != null
        // && hotel.getPricePerNight().compareTo(
        // hotelBudget.divide(
        // BigDecimal.valueOf(itinerary
        // .getTotalDays()),
        // 2, BigDecimal.ROUND_HALF_UP)) <= 0)
        // .sorted(Comparator.comparing(Hotel::getAverageRating,
        // Comparator.nullsLast(Comparator.reverseOrder())))
        // .limit(3)
        // .toList();

        // List<Hotel> filteredHotels = hotels.stream()
        // .filter(hotel -> hotel.getPricePerNight() != null
        // && hotel.getPricePerNight().compareTo(
        // hotelBudget.divide(
        // BigDecimal.valueOf(itinerary
        // .getTotalDays()),
        // 2, BigDecimal.ROUND_HALF_UP)) <= 0)
        // .toList();
        // System.out.println("Total number of hotel after filtering is " +
        // filteredHotels.size());

        // List<Hotel> sortedHotels = filteredHotels.stream()
        // .sorted(Comparator.comparing(Hotel::getAverageRating,
        // Comparator.nullsLast(Comparator.reverseOrder())))
        // .toList();
        // System.out.println("Total number of hotel after sorting is " +
        // sortedHotels.size());
        // List<Hotel> limitedHotels = sortedHotels.stream()
        // .limit(3).toList();
        // sortedHotels.forEach(hotel -> System.out.println(
        // "Hotel Name: " + hotel.getHotelName() + ", Price Per Night: " +
        // hotel.getPricePerNight()
        // + ", Average Rating: " + hotel.getAverageRating()));
        // System.out.println("Total number of hotel after limiting is " +
        // limitedHotels.size());
        // System.out.println("Total number of hotel after assigning budget is " +
        // assignedHotel.size());
        // return assignedHotel;
        // }

        @SuppressWarnings("null")
        private List<Hotel> assignHotels(Itinerary itinerary, Location location) {
                log.info("Assigning hotels for itinerary...");

                List<Hotel> hotels = hotelRepository.findByLocationId(location.getId());

                System.out.println("Total hotels found for location: " + hotels.size());
                if (hotels.isEmpty()) {
                        log.warn("No hotels found for location: {}", location.getCityName());
                        return new ArrayList<>();
                }

                // ✅ Calculate hotel budget (30% of total budget)
                BigDecimal totalBudget = itinerary.getTotalBudget();
                BigDecimal hotelBudget = totalBudget.multiply(HOTEL_BUDGET_PERCENTAGE);

                // ✅ Per day hotel budget
                BigDecimal perDayHotelBudget = hotelBudget.divide(
                                BigDecimal.valueOf(itinerary.getTotalDays()),
                                2,
                                BigDecimal.ROUND_HALF_UP);

                System.out.println("================== HOTEL BUDGET ==================");
                System.out.println("Total Budget: ₹" + totalBudget);
                System.out.println("Hotel Budget (30%): ₹" + hotelBudget);
                System.out.println("Per Day Hotel Budget: ₹" + perDayHotelBudget);
                System.out.println("==================================================");

                // Filter hotels within budget
                List<Hotel> filteredHotels = hotels.stream()
                                .filter(hotel -> hotel.getPricePerNight() != null)
                                .filter(hotel -> hotel.getPricePerNight().compareTo(perDayHotelBudget) <= 0)
                                .sorted(Comparator.comparing(Hotel::getAverageRating,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .limit(3)
                                .toList();

                System.out.println("Filtered Hotels (within budget): " + filteredHotels.size());
                filteredHotels.forEach(hotel -> System.out.println("  ✅ " + hotel.getHotelName() +
                                " - ₹" + hotel.getPricePerNight() +
                                " (Rating: " + hotel.getAverageRating() + ")"));

                return filteredHotels;
        }

        // ==========================================================
        // ASSIGN RESTAURANTS
        // ==========================================================

        @SuppressWarnings("null")
        private List<Restaurant> assignRestaurants(Itinerary itinerary, Location location) {
                log.info("Assigning restaurants for itinerary...");

                List<Restaurant> restaurants = restaurantRepository.findByLocationIdAndActiveTrue(location.getId());

                if (restaurants.isEmpty()) {
                        log.warn("No restaurants found for location: {}", location.getCityName());
                        return new ArrayList<>();
                }

                // ✅ Calculate restaurant budget (30% of total budget)
                BigDecimal totalBudget = itinerary.getTotalBudget();
                BigDecimal restaurantBudget = totalBudget.multiply(RESTAURANT_BUDGET_PERCENTAGE);

                // ✅ Per meal budget (2 meals per day)
                BigDecimal perMealBudget = restaurantBudget.divide(
                                BigDecimal.valueOf(itinerary.getTotalDays() * 2),
                                2,
                                BigDecimal.ROUND_HALF_UP);

                System.out.println("================== RESTAURANT BUDGET ==================");
                System.out.println("Total Budget: ₹" + totalBudget);
                System.out.println("Restaurant Budget (30%): ₹" + restaurantBudget);
                System.out.println("Per Meal Budget: ₹" + perMealBudget);
                System.out.println("=======================================================");

                // Filter restaurants within budget
                List<Restaurant> filteredRestaurants = restaurants.stream()
                                .filter(restaurant -> restaurant.getAverageCostPerPerson() != null)
                                .filter(restaurant -> restaurant.getAverageCostPerPerson()
                                                .compareTo(perMealBudget) <= 0)
                                .sorted(Comparator.comparing(Restaurant::getAverageRating,
                                                Comparator.nullsLast(Comparator.reverseOrder())))
                                .limit(3)
                                .toList();

                System.out.println("Filtered Restaurants (within budget): " + filteredRestaurants.size());
                filteredRestaurants.forEach(restaurant -> System.out.println("  ✅ " + restaurant.getRestaurantName() +
                                " - ₹" + restaurant.getAverageCostPerPerson() +
                                " (Rating: " + restaurant.getAverageRating() + ")"));

                return filteredRestaurants;
        }
        // @SuppressWarnings("null")
        // private List<Restaurant> assignRestaurants(Itinerary itinerary, Location
        // location) {
        // log.info("Assigning restaurants for itinerary...");

        // List<Restaurant> restaurants =
        // restaurantRepository.findByLocationIdAndActiveTrue(location.getId());

        // if (restaurants.isEmpty()) {
        // log.warn("No restaurants found for location: {}", location.getCityName());
        // return new ArrayList<>();
        // }

        // // Filter restaurants based on budget
        // BigDecimal budget = itinerary.getTotalBudget();
        // BigDecimal foodBudget = budget.multiply(BigDecimal.valueOf(0.2)); // 20% for
        // food

        // return restaurants.stream()
        // .filter(restaurant -> restaurant.getAverageCostPerPerson() != null
        // && restaurant.getAverageCostPerPerson().compareTo(
        // foodBudget.divide(BigDecimal
        // .valueOf(itinerary.getTotalDays() * 2),
        // 2, BigDecimal.ROUND_HALF_UP)) <= 0)
        // .sorted(Comparator.comparing(Restaurant::getAverageRating,
        // Comparator.nullsLast(Comparator.reverseOrder())))
        // .limit(3)
        // .toList();
        // }

        // ==========================================================
        // COST CALCULATIONS
        // ==========================================================

        private BigDecimal calculateTotalEstimatedCost(Itinerary itinerary,
                        List<Hotel> hotels,
                        List<Restaurant> restaurants,
                        List<TouristPlace> touristPlaces) {
                BigDecimal totalCost = BigDecimal.ZERO;

                // Hotel costs
                if (!hotels.isEmpty()) {
                        Hotel bestHotel = hotels.get(0);
                        BigDecimal hotelCost = bestHotel.getPricePerNight() != null
                                        ? bestHotel.getPricePerNight()
                                                        .multiply(BigDecimal.valueOf(itinerary.getTotalDays()))
                                        : BigDecimal.ZERO;
                        totalCost = totalCost.add(hotelCost);
                }

                // Restaurant costs
                if (!restaurants.isEmpty()) {
                        Restaurant bestRestaurant = restaurants.get(0);
                        BigDecimal foodCost = bestRestaurant.getAverageCostPerPerson() != null
                                        ? bestRestaurant.getAverageCostPerPerson()
                                                        .multiply(BigDecimal.valueOf(itinerary.getTotalDays() * 2))
                                        : BigDecimal.ZERO;
                        totalCost = totalCost.add(foodCost);
                }

                // Tourist place costs
                @SuppressWarnings("null")
                BigDecimal placeCost = touristPlaces.stream()
                                .map(place -> place.getPrice() != null ? place.getPrice() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                totalCost = totalCost.add(placeCost);

                return totalCost;
        }

        private BigDecimal calculateRemainingBudget(Itinerary itinerary) {
                if (itinerary.getTotalBudget() == null) {
                        return BigDecimal.ZERO;
                }

                BigDecimal estimatedCost = itinerary.getEstimatedCost() != null
                                ? itinerary.getEstimatedCost()
                                : BigDecimal.ZERO;

                return itinerary.getTotalBudget().subtract(estimatedCost);
        }

        // ==========================================================
        // STATUS MANAGEMENT
        // ==========================================================

        private void updateStatusAutomatically(Itinerary itinerary) {
                if (itinerary.getEstimatedCost() == null || itinerary.getTotalBudget() == null) {
                        itinerary.setItineraryStatus(ItineraryStatus.GENERATED);
                        return;
                }

                BigDecimal remainingBudget = calculateRemainingBudget(itinerary);
                BigDecimal budgetRatio = remainingBudget.divide(itinerary.getTotalBudget(),
                                2, BigDecimal.ROUND_HALF_UP);

                if (budgetRatio.compareTo(BigDecimal.valueOf(-0.2)) < 0) {
                        itinerary.setItineraryStatus(ItineraryStatus.CONCERNED);
                        log.warn("Estimated cost significantly exceeds budget for itinerary={}", itinerary.getId());
                } else if (budgetRatio.compareTo(BigDecimal.ZERO) < 0) {
                        itinerary.setItineraryStatus(ItineraryStatus.PLANNED);
                        log.info("Estimated cost slightly exceeds budget for itinerary={}", itinerary.getId());
                } else {
                        itinerary.setItineraryStatus(ItineraryStatus.GENERATED);
                }
        }

        // ==========================================================
        // ADDITIONAL BUSINESS METHODS (Interface Implementation)
        // ==========================================================

        @Override
        @Transactional(readOnly = true)
        public BigDecimal calculateRemainingBudget(Long itineraryId) {
                Itinerary itinerary = getItinerary(itineraryId);
                return calculateRemainingBudget(itinerary);
        }

        @Override
        @Transactional(readOnly = true)
        public boolean isItineraryOwner(@NonNull Long itineraryId) {
                try {
                        Itinerary itinerary = getItinerary(itineraryId);
                        Long currentUserId = securityUtils.getCurrentUserId();
                        return itinerary.getUser().getId().equals(currentUserId);
                } catch (Exception e) {
                        return false;
                }
        }

        @Override
        public ItineraryResponse updateStatus(@NonNull Long id, ItineraryStatus status) {
                log.info("Updating status for itinerary id={} to {}", id, status);

                Itinerary itinerary = getItinerary(id);
                validateOwnership(itinerary);

                itinerary.setItineraryStatus(status);
                itinerary.setUpdatedAt(LocalDateTime.now());

                Itinerary updatedItinerary = itineraryRepository.save(itinerary);
                return itineraryMapper.toResponse(updatedItinerary);
        }

        @Override
        @Transactional(readOnly = true)
        public List<ItineraryResponse> getItinerariesByStatus(ItineraryStatus status) {
                log.info("Fetching itineraries with status={}", status);
                Long userId = securityUtils.getCurrentUserId();
                List<Itinerary> itineraries = itineraryRepository.findByUserIdAndItineraryStatus(userId, status);
                return itineraries.stream()
                                .map(itineraryMapper::toResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<ItineraryResponse> getUpcomingItineraries() {
                log.info("Fetching upcoming itineraries...");
                Long userId = securityUtils.getCurrentUserId();
                LocalDate now = LocalDate.now();
                List<Itinerary> itineraries = itineraryRepository
                                .findByUserIdAndStartDateAfterOrderByStartDateAsc(userId, now);
                return itineraries.stream()
                                .map(itineraryMapper::toResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<ItineraryResponse> getItinerariesWithinBudget(BigDecimal minBudget, BigDecimal maxBudget) {
                log.info("Fetching itineraries within budget range: {} - {}", minBudget, maxBudget);
                Long userId = securityUtils.getCurrentUserId();
                List<Itinerary> itineraries = itineraryRepository
                                .findByUserIdAndTotalBudgetBetween(userId, minBudget, maxBudget);
                return itineraries.stream()
                                .map(itineraryMapper::toResponse)
                                .toList();
        }

        @Override
        @Transactional(readOnly = true)
        public List<ItineraryResponse> searchItineraries(String keyword) {
                log.info("Searching itineraries with keyword: {}", keyword);
                Long userId = securityUtils.getCurrentUserId();
                List<Itinerary> itineraries = itineraryRepository
                                .findByUserIdAndTitleContainingOrDescriptionContaining(userId, keyword, keyword);
                return itineraries.stream()
                                .map(itineraryMapper::toResponse)
                                .toList();
        }
}