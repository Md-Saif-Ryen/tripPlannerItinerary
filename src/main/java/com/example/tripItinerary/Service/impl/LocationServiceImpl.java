package com.example.tripItinerary.Service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.projection.MissingDataLocationProjection;
import com.example.tripItinerary.DTO.request.LocationRequest;
import com.example.tripItinerary.DTO.request.MissingLocationRequest;
import com.example.tripItinerary.DTO.request.ResolveMissingLocationRequest;
import com.example.tripItinerary.DTO.response.LocationNameResponse;
import com.example.tripItinerary.DTO.response.LocationResponse;
import com.example.tripItinerary.DTO.response.MissingDataResponse;
import com.example.tripItinerary.DTO.response.MostSearchedLocationResponse;
import com.example.tripItinerary.DTO.response.OlaAutocompleteResponse;
import com.example.tripItinerary.DTO.response.OlaPredictionResponse;
import com.example.tripItinerary.DTO.response.OlaTermResponse;
import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Entity.LocationSearch;
import com.example.tripItinerary.Entity.MissingLocation;
import com.example.tripItinerary.Entity.MissingLocationRequester;
import com.example.tripItinerary.Mapper.LocationMapper;
import com.example.tripItinerary.Repo.LocationRepository;
import com.example.tripItinerary.Repo.LocationSearchRepository;
import com.example.tripItinerary.Repo.MissingLocationRepository;
import com.example.tripItinerary.Repo.MissingLocationRequesterRepository;
// import com.example.tripItinerary.Service.FcmNotificationService;
import com.example.tripItinerary.Service.LocationService;
import com.example.tripItinerary.Service.OlaMapsService;
import com.example.tripItinerary.enums.LocationType;
import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LocationServiceImpl implements LocationService {

        private final LocationRepository locationRepository;
        private final LocationMapper locationMapper;
        private final LocationSearchRepository locationSearchRepository;
        private final MissingLocationRepository missingLocationRepository;
        private final MissingLocationRequesterRepository missingLocationRequesterRepository;
        private final OlaMapsService olaMapsService;
        // private final FcmNotificationService fcmNotificationService;

        // ============================================================
        // CREATE
        // ============================================================

        @Override
        public LocationResponse create(LocationRequest request) {

                Location parent = null;

                if (request.getParentId() != null) {
                        parent = locationRepository.findById(request.getParentId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Parent location not found with id: "
                                                                        + request.getParentId()));
                }

                validateHierarchy(request, parent);

                validateDuplicateLocation(request, null);

                Location location = locationMapper.toEntity(request);
                location.setParent(parent);

                location = locationRepository.save(location);

                return locationMapper.toResponse(location);
        }

        // ============================================================
        // UPDATE
        // ============================================================

        @Override
        public LocationResponse update(Long id, LocationRequest request) {

                Location location = locationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id: " + id));

                Location parent = null;

                if (request.getParentId() != null) {
                        parent = locationRepository.findById(request.getParentId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Parent location not found with id: "
                                                                        + request.getParentId()));
                }

                if (request.getParentId() != null
                                && request.getParentId().equals(id)) {
                        throw new IllegalArgumentException(
                                        "Location cannot be its own parent.");
                }

                validateHierarchy(request, parent);

                if (parent != null && isDescendant(id, parent.getId())) {
                        throw new IllegalArgumentException(
                                        "Invalid parent. Circular location hierarchy detected.");
                }

                validateDuplicateLocation(request, id);

                location.setStateName(request.getStateName());
                location.setCityName(request.getCityName());
                location.setAddress(request.getAddress());
                location.setLatitude(request.getLatitude());
                location.setLongitude(request.getLongitude());
                location.setLocationType(request.getLocationType());
                location.setParent(parent);

                location = locationRepository.save(location);

                return locationMapper.toResponse(location);
        }

        // ============================================================
        // DUPLICATE LOCATION VALIDATION
        // ============================================================

        private void validateDuplicateLocation(
                        LocationRequest request,
                        Long currentId) {

                Optional<Location> existing = locationRepository
                                .findByCityNameIgnoreCaseAndStateNameIgnoreCase(
                                                request.getCityName().trim(),
                                                request.getStateName().trim());

                if (existing.isEmpty()) {
                        return;
                }

                if (currentId != null
                                && existing.get().getId().equals(currentId)) {
                        return;
                }

                throw new IllegalArgumentException(
                                "Location already exists: "
                                                + request.getCityName()
                                                + ", "
                                                + request.getStateName());
        }

        // ============================================================
        // GET BY ID
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public LocationResponse getById(Long id) {

                Location location = locationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id: " + id));

                return locationMapper.toResponse(location);
        }

        // ============================================================
        // GET ALL
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public List<LocationResponse> getAll() {

                return locationRepository.findAll()
                                .stream()
                                .map(locationMapper::toResponse)
                                .collect(Collectors.toList());
        }

        // ============================================================
        // CHILDREN
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public List<LocationResponse> getChildren(Long parentId) {

                locationRepository.findById(parentId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Parent location not found with id: " + parentId));

                return locationRepository
                                .findByParentIdOrderByCityNameAsc(parentId)
                                .stream()
                                .map(locationMapper::toResponse)
                                .toList();
        }

        // ============================================================
        // DESCENDANTS
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public List<LocationResponse> getDescendants(Long locationId) {

                locationRepository.findById(locationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id: " + locationId));

                return collectDescendants(locationId)
                                .stream()
                                .map(locationMapper::toResponse)
                                .toList();
        }

        private List<Location> collectDescendants(Long parentId) {

                List<Location> result = new ArrayList<>();

                List<Location> children = locationRepository.findByParentIdOrderByCityNameAsc(parentId);

                for (Location child : children) {
                        result.add(child);
                        result.addAll(collectDescendants(child.getId()));
                }

                return result;
        }

        // ============================================================
        // DESCENDANT IDS
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public List<Long> getDescendantLocationIds(Long locationId) {

                locationRepository.findById(locationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id: " + locationId));

                List<Long> ids = new ArrayList<>();
                ids.add(locationId);

                collectDescendantIds(locationId, ids);

                return ids;
        }

        private void collectDescendantIds(
                        Long parentId,
                        List<Long> ids) {

                List<Location> children = locationRepository.findByParentId(parentId);

                for (Location child : children) {
                        ids.add(child.getId());
                        collectDescendantIds(child.getId(), ids);
                }
        }

        // ============================================================
        // CIRCULAR HIERARCHY CHECK
        // ============================================================

        private boolean isDescendant(
                        Long locationId,
                        Long possibleDescendantId) {

                List<Long> descendantIds = getDescendantLocationIds(locationId);

                return descendantIds.contains(possibleDescendantId);
        }

        // ============================================================
        // VALIDATE HIERARCHY
        // ============================================================

        private void validateHierarchy(
                        LocationRequest request,
                        Location parent) {

                LocationType type = request.getLocationType();

                if (type == null) {
                        throw new IllegalArgumentException(
                                        "Location type is required.");
                }

                // COUNTRY has no parent
                if (type == LocationType.COUNTRY) {

                        if (parent != null) {
                                throw new IllegalArgumentException(
                                                "COUNTRY cannot have a parent location.");
                        }

                        return;
                }

                // Everything except COUNTRY requires a parent
                if (parent == null) {
                        throw new IllegalArgumentException(
                                        type + " must have a parent location.");
                }

                LocationType parentType = parent.getLocationType();

                switch (type) {

                        case STATE:

                                if (parentType != LocationType.COUNTRY) {
                                        throw new IllegalArgumentException(
                                                        "STATE must have COUNTRY as parent.");
                                }

                                break;

                        case CITY:

                                if (parentType != LocationType.STATE) {
                                        throw new IllegalArgumentException(
                                                        "CITY must have STATE as parent.");
                                }

                                break;

                        case AREA:

                                if (parentType != LocationType.CITY) {
                                        throw new IllegalArgumentException(
                                                        "AREA must have CITY as parent.");
                                }

                                break;

                        default:

                                throw new IllegalArgumentException(
                                                "Invalid location hierarchy.");
                }
        }

        // ============================================================
        // DELETE
        // ============================================================

        @Override
        public void delete(@NonNull Long id) {

                Location location = locationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id: " + id));

                if (!location.getChildren().isEmpty()) {
                        throw new IllegalStateException(
                                        "Cannot delete location because child locations exist.");
                }

                locationRepository.delete(location);
        }

        // ============================================================
        // GET LOCATION NAMES
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public List<LocationNameResponse> getByLocationName() {

                return locationRepository.findAll()
                                .stream()
                                .map(location -> new LocationNameResponse(
                                                location.getId(),
                                                location.getCityName(),
                                                location.getStateName(),
                                                location.getLocationType()))
                                .toList();
        }

        // ============================================================
        // SEARCH
        // ============================================================

        @Override
        public List<LocationNameResponse> searchLocations(String query) {

                if (query == null || query.trim().isEmpty()) {
                        return List.of();
                }

                String originalQuery = query.trim();

                String normalizedQuery = originalQuery.toLowerCase(Locale.ROOT);

                // Save search history only.
                // Missing location is NOT created here.
                locationSearchRepository.save(
                                LocationSearch.builder()
                                                .searchQuery(originalQuery)
                                                .normalizedQuery(normalizedQuery)
                                                .build());

                List<Location> locations = locationRepository.searchLocations(
                                normalizedQuery);

                if (locations == null || locations.isEmpty()) {
                        return List.of();
                }

                return locations.stream()
                                .filter(location -> location.getCityName() != null
                                                && !location.getCityName()
                                                                .trim()
                                                                .isEmpty())
                                .sorted((a, b) -> compareSearchResult(
                                                a,
                                                b,
                                                normalizedQuery))
                                .map(location -> new LocationNameResponse(
                                                location.getId(),
                                                location.getCityName().trim(),
                                                location.getStateName(),
                                                location.getLocationType()))
                                .collect(Collectors.toMap(
                                                response -> response.getCityName()
                                                                .trim()
                                                                .toLowerCase(Locale.ROOT),
                                                response -> response,
                                                (existing, duplicate) -> existing,
                                                LinkedHashMap::new))
                                .values()
                                .stream()
                                .toList();
        }

        private int compareSearchResult(
                        Location a,
                        Location b,
                        String query) {

                String cityA = a.getCityName()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                String cityB = b.getCityName()
                                .trim()
                                .toLowerCase(Locale.ROOT);

                boolean exactA = cityA.equals(query);
                boolean exactB = cityB.equals(query);

                if (exactA != exactB) {
                        return exactA ? -1 : 1;
                }

                boolean startsA = cityA.startsWith(query);
                boolean startsB = cityB.startsWith(query);

                if (startsA != startsB) {
                        return startsA ? -1 : 1;
                }

                boolean containsA = cityA.contains(query);
                boolean containsB = cityB.contains(query);

                if (containsA != containsB) {
                        return containsA ? -1 : 1;
                }

                int lengthCompare = Integer.compare(cityA.length(), cityB.length());

                if (lengthCompare != 0) {
                        return lengthCompare;
                }

                return cityA.compareTo(cityB);
        }

        // ============================================================
        // TOP SEARCHED
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public List<MostSearchedLocationResponse> getTopSearchedLocations() {

                List<Object[]> results = locationSearchRepository
                                .findMostSearchedLocations();

                List<MostSearchedLocationResponse> response = new ArrayList<>();

                int rank = 1;

                for (Object[] result : results) {

                        if (rank > 5) {
                                break;
                        }

                        String searchQuery = (String) result[0];

                        Long count = ((Number) result[1]).longValue();

                        response.add(
                                        MostSearchedLocationResponse.builder()
                                                        .searchQuery(searchQuery)
                                                        .searchCount(count)
                                                        .rank(rank)
                                                        .build());

                        rank++;
                }

                return response;
        }

        // ============================================================
        // REQUEST MISSING LOCATION
        // ============================================================

        @Override
        public MissingLocation requestMissingLocation(
                        MissingLocationRequest request) {

                String locationName = request.getQuery().trim();

                String normalizedName = locationName.toLowerCase(Locale.ROOT);

                // ==========================================
                // Check whether exact city already exists
                // ==========================================

                List<Location> existingLocations = locationRepository.searchLocations(
                                normalizedName);

                boolean alreadyExists = existingLocations != null
                                && existingLocations.stream()
                                                .anyMatch(location -> location.getCityName() != null
                                                                && location.getCityName()
                                                                                .trim()
                                                                                .equalsIgnoreCase(
                                                                                                locationName));

                if (alreadyExists) {
                        throw new IllegalArgumentException(
                                        "This location already exists.");
                }

                // ==========================================
                // Find or create missing location
                // ==========================================

                MissingLocation missingLocation = missingLocationRepository
                                .findByLocationNameIgnoreCase(
                                                locationName)
                                .orElse(null);

                LocalDateTime now = LocalDateTime.now();

                if (missingLocation == null) {

                        missingLocation = MissingLocation.builder()
                                        .locationName(locationName)
                                        .searchCount(0)
                                        .resolved(false)
                                        .lastSearchedAt(now)
                                        .createdAt(now)
                                        .updatedAt(now)
                                        .build();

                } else {

                        if (Boolean.TRUE.equals(
                                        missingLocation.getResolved())) {

                                throw new IllegalArgumentException(
                                                "This location has already been resolved.");
                        }

                        missingLocation.setLastSearchedAt(now);
                        missingLocation.setUpdatedAt(now);
                }

                // Every request/search increases demand count
                missingLocation.setSearchCount(
                                missingLocation.getSearchCount() + 1);

                missingLocation = missingLocationRepository.save(
                                missingLocation);

                // ==========================================
                // One user can request same location once
                // ==========================================

                Optional<MissingLocationRequester> existingRequester = missingLocationRequesterRepository
                                .findByMissingLocationIdAndUserId(
                                                missingLocation.getId(),
                                                request.getUserId());

                if (existingRequester.isPresent()) {

                        MissingLocationRequester requester = existingRequester.get();

                        // Keep latest profile/token
                        requester.setUserName(
                                        request.getUserName());

                        requester.setFcmToken(
                                        request.getFcmToken());

                        missingLocationRequesterRepository.save(
                                        requester);

                } else {

                        MissingLocationRequester requester = MissingLocationRequester.builder()
                                        .missingLocation(missingLocation)
                                        .userId(request.getUserId())
                                        .userName(request.getUserName())
                                        .fcmToken(request.getFcmToken())
                                        .notified(false)
                                        .requestedAt(now)
                                        .build();

                        missingLocationRequesterRepository.save(
                                        requester);
                }

                return missingLocation;
        }

        // ============================================================
        // PENDING LOCATIONS
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public List<MissingLocation> getPendingLocations() {

                return missingLocationRepository
                                .findByResolvedFalseOrderBySearchCountDesc();
        }

        // ============================================================
        // OLA AUTOCOMPLETE
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public OlaAutocompleteResponse getMissingLocationAutocomplete(
                        Long missingLocationId) {

                MissingLocation missingLocation = missingLocationRepository
                                .findById(missingLocationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Missing location not found with id: "
                                                                + missingLocationId));

                if (Boolean.TRUE.equals(
                                missingLocation.getResolved())) {

                        throw new IllegalArgumentException(
                                        "Location has already been resolved.");
                }

                return olaMapsService.autocomplete(
                                missingLocation.getLocationName());
        }

        // ============================================================
        // RESOLVE MISSING LOCATION
        // ============================================================

        @Override
        public MissingLocation resolveMissingLocation(
                        Long missingLocationId,
                        ResolveMissingLocationRequest request) {

                MissingLocation missingLocation = missingLocationRepository
                                .findById(missingLocationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Missing location not found with id: "
                                                                + missingLocationId));

                if (Boolean.TRUE.equals(
                                missingLocation.getResolved())) {

                        throw new IllegalArgumentException(
                                        "Location is already resolved.");
                }

                // ==========================================
                // Get fresh OLA suggestions
                // ==========================================

                OlaAutocompleteResponse olaResponse = olaMapsService.autocomplete(
                                missingLocation.getLocationName());

                if (olaResponse == null
                                || olaResponse.getPredictions() == null
                                || olaResponse.getPredictions().isEmpty()) {

                        throw new ResourceNotFoundException(
                                        "No OLA location found for: "
                                                        + missingLocation.getLocationName());
                }

                // ==========================================
                // Find selected OLA result
                // ==========================================

                OlaPredictionResponse prediction = olaResponse.getPredictions()
                                .stream()
                                .filter(item -> request.getOlaPlaceId()
                                                .equals(item.getPlaceId()))
                                .findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Selected OLA place was not found."));

                // ==========================================
                // Extract city/state
                // ==========================================

                String cityName = extractCity(prediction);

                String stateName = extractState(prediction);

                if (cityName == null
                                || cityName.isBlank()
                                || stateName == null
                                || stateName.isBlank()) {

                        throw new IllegalArgumentException(
                                        "Unable to determine city/state from OLA result: "
                                                        + prediction.getDescription());
                }

                // ==========================================
                // Validate coordinates
                // ==========================================

                if (prediction.getGeometry() == null
                                || prediction.getGeometry().getLocation() == null
                                || prediction.getGeometry().getLocation().getLat() == null
                                || prediction.getGeometry().getLocation().getLng() == null) {

                        throw new IllegalArgumentException(
                                        "OLA result does not contain valid coordinates.");
                }

                BigDecimal latitude = BigDecimal.valueOf(
                                prediction.getGeometry()
                                                .getLocation()
                                                .getLat());

                BigDecimal longitude = BigDecimal.valueOf(
                                prediction.getGeometry()
                                                .getLocation()
                                                .getLng());

                // ==========================================
                // Check whether city already exists
                // ==========================================

                Optional<Location> existingCity = locationRepository
                                .findByCityNameIgnoreCaseAndStateNameIgnoreCase(
                                                cityName,
                                                stateName);

                Location city;

                if (existingCity.isPresent()) {

                        // Do NOT create duplicate location.
                        city = existingCity.get();

                } else {

                        // ==========================================
                        // Find COUNTRY
                        // ==========================================
                        List<Location> countries = locationRepository.findByCityNameIgnoreCase("India");

                        if (countries.isEmpty()) {
                                throw new ResourceNotFoundException(
                                                "India country location not found.");
                        }

                        Location country = countries.get(0);
                        // ==========================================
                        // Find or create STATE
                        // ==========================================

                        Location state = locationRepository
                                        .findByStateNameIgnoreCase(stateName)
                                        .stream()
                                        .filter(item -> item.getLocationType() == LocationType.STATE)
                                        .findFirst()
                                        .orElseGet(() -> {

                                                Location newState = Location.builder()
                                                                .stateName(stateName)
                                                                .cityName(stateName)
                                                                .address(
                                                                                stateName
                                                                                                + ", India")
                                                                .locationType(
                                                                                LocationType.STATE)
                                                                .parent(country)
                                                                .build();

                                                return locationRepository.save(
                                                                newState);
                                        });

                        // ==========================================
                        // Create CITY
                        // ==========================================

                        city = Location.builder()
                                        .stateName(stateName)
                                        .cityName(cityName)
                                        .address(
                                                        prediction.getDescription())
                                        .latitude(latitude)
                                        .longitude(longitude)
                                        .locationType(
                                                        LocationType.CITY)
                                        .parent(state)
                                        .build();

                        city = locationRepository.save(city);
                }

                // ==========================================
                // Mark missing location as resolved
                // ==========================================

                LocalDateTime now = LocalDateTime.now();

                missingLocation.setResolved(true);
                missingLocation.setResolvedLocationId(
                                city.getId());
                missingLocation.setResolvedCityName(
                                city.getCityName());
                missingLocation.setResolvedStateName(
                                city.getStateName());
                missingLocation.setOlaPlaceId(
                                prediction.getPlaceId());
                missingLocation.setUpdatedAt(now);

                missingLocationRepository.save(
                                missingLocation);

                // ==========================================
                // Notify every requester
                // ==========================================

                List<MissingLocationRequester> requesters = missingLocationRequesterRepository
                                .findByMissingLocationId(
                                                missingLocation.getId());

                for (MissingLocationRequester requester : requesters) {

                        if (requester.getFcmToken() == null
                                        || requester.getFcmToken().isBlank()) {
                                continue;
                        }

                        try {

                                // fcmNotificationService
                                //                 .sendLocationAddedNotification(
                                //                                 requester.getFcmToken(),
                                //                                 city.getCityName());

                                requester.setNotified(true);
                                requester.setNotifiedAt(now);

                                missingLocationRequesterRepository.save(
                                                requester);

                        } catch (Exception ignored) {
                                // Do not rollback successful location creation
                                // just because one notification failed.
                        }
                }

                return missingLocation;
        }

        // ============================================================
        // EXTRACT STATE FROM OLA TERMS
        // ============================================================

        private String extractState(
                        OlaPredictionResponse prediction) {

                List<OlaTermResponse> terms = prediction.getTerms();

                if (terms == null || terms.isEmpty()) {
                        return null;
                }

                for (OlaTermResponse term : terms) {

                        if (term == null
                                        || term.getValue() == null
                                        || term.getValue().isBlank()) {
                                continue;
                        }

                        String value = term.getValue().trim();

                        if (isIndianState(value)) {
                                return value;
                        }
                }

                return null;
        }

        // ============================================================
        // EXTRACT CITY FROM OLA TERMS
        // ============================================================

        private String extractCity(
                        OlaPredictionResponse prediction) {

                List<OlaTermResponse> terms = prediction.getTerms();

                if (terms == null || terms.isEmpty()) {
                        return null;
                }

                String state = extractState(prediction);

                if (state == null) {
                        return null;
                }

                for (int i = 0; i < terms.size(); i++) {

                        String value = terms.get(i) != null
                                        ? terms.get(i).getValue()
                                        : null;

                        if (value == null || value.isBlank()) {
                                continue;
                        }

                        value = value.trim();

                        if (!value.equalsIgnoreCase(state)) {
                                continue;
                        }

                        // City is normally the term immediately before state.
                        for (int j = i - 1; j >= 0; j--) {

                                String possibleCity = terms.get(j) != null
                                                ? terms.get(j).getValue()
                                                : null;

                                if (possibleCity == null
                                                || possibleCity.isBlank()) {
                                        continue;
                                }

                                possibleCity = possibleCity.trim();

                                if (possibleCity.equalsIgnoreCase("India")
                                                || possibleCity.matches("\\d{6}")
                                                || isIndianState(possibleCity)) {
                                        continue;
                                }

                                return possibleCity;
                        }
                }

                return null;
        }

        // ============================================================
        // INDIAN STATES
        // ============================================================

        private boolean isIndianState(String value) {

                Set<String> states = Set.of(
                                "Andhra Pradesh",
                                "Arunachal Pradesh",
                                "Assam",
                                "Bihar",
                                "Chhattisgarh",
                                "Goa",
                                "Gujarat",
                                "Haryana",
                                "Himachal Pradesh",
                                "Jharkhand",
                                "Karnataka",
                                "Kerala",
                                "Madhya Pradesh",
                                "Maharashtra",
                                "Manipur",
                                "Meghalaya",
                                "Mizoram",
                                "Nagaland",
                                "Odisha",
                                "Punjab",
                                "Rajasthan",
                                "Sikkim",
                                "Tamil Nadu",
                                "Telangana",
                                "Tripura",
                                "Uttar Pradesh",
                                "Uttarakhand",
                                "West Bengal",
                                "Delhi",
                                "Jammu and Kashmir",
                                "Ladakh");

                return states.stream()
                                .anyMatch(state -> state.equalsIgnoreCase(value));
        }

        // ============================================================
        // MISSING DATA
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public MissingDataResponse getLocationsMissingData(
                        Pageable pageable) {

                Page<MissingDataLocationProjection> hotels = locationRepository
                                .findLocationsWithoutHotels(pageable);

                Page<MissingDataLocationProjection> restaurants = locationRepository
                                .findLocationsWithoutRestaurants(pageable);

                Page<MissingDataLocationProjection> touristPlaces = locationRepository
                                .findLocationsWithoutTouristPlaces(pageable);

                return new MissingDataResponse(
                                hotels,
                                restaurants,
                                touristPlaces);
        }
}
