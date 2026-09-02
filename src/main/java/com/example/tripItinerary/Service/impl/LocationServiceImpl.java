package com.example.tripItinerary.Service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.LocationRequest;
import com.example.tripItinerary.DTO.response.LocationNameResponse;
import com.example.tripItinerary.DTO.response.LocationResponse;
import com.example.tripItinerary.DTO.response.MostSearchedLocationResponse;
import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Entity.LocationSearch;
import com.example.tripItinerary.Entity.MissingLocation;
import com.example.tripItinerary.Mapper.LocationMapper;
import com.example.tripItinerary.Repo.LocationRepository;
import com.example.tripItinerary.Repo.LocationSearchRepository;
import com.example.tripItinerary.Repo.MissingLocationRepository;
import com.example.tripItinerary.Service.LocationService;
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

        // ============================================================
        // CREATE LOCATION
        // ============================================================

        @SuppressWarnings("null")
        @Override
        public LocationResponse create(LocationRequest request) {

                Location location = locationMapper.toEntity(request);

                location = locationRepository.save(location);

                return locationMapper.toResponse(location);
        }

        // ============================================================
        // UPDATE LOCATION
        // ============================================================

        @Override
        public LocationResponse update(
                        Long id,
                        LocationRequest request) {

                @SuppressWarnings("null")
                Location location = locationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id : "
                                                                + id));

                location.setStateName(
                                request.getStateName());

                location.setCityName(
                                request.getCityName());

                location.setAddress(
                                request.getAddress());

                location.setLatitude(
                                request.getLatitude());

                location.setLongitude(
                                request.getLongitude());

                location = locationRepository.save(location);

                return locationMapper.toResponse(location);
        }

        // ============================================================
        // GET LOCATION BY ID
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public LocationResponse getById(Long id) {

                @SuppressWarnings("null")
                Location location = locationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id : "
                                                                + id));

                return locationMapper.toResponse(location);
        }

        // ============================================================
        // GET ALL LOCATIONS
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
        // DELETE LOCATION
        // ============================================================

        @SuppressWarnings("null")
        @Override
        public void delete(@NonNull Long id) {

                Location location = locationRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id : "
                                                                + id));

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
                                                location.getStateName()))
                                .toList();
        }

        // ============================================================
        // SEARCH LOCATIONS
        //
        // Existing API:
        //
        // GET /api/v1/locations/search?query=darbh
        //
        // This method will:
        // 1. Save search query
        // 2. Search location
        // 3. Return existing LocationNameResponse
        //
        // ============================================================

        @Override
        public List<LocationNameResponse> searchLocations(String query) {

                if (query == null || query.trim().isEmpty()) {
                        return List.of();
                }

                String originalQuery = query.trim();
                String normalizedQuery = originalQuery.toLowerCase();

                // ========================================================
                // SAVE SEARCH HISTORY
                // ========================================================

                LocationSearch search = LocationSearch.builder()
                                .searchQuery(originalQuery)
                                .normalizedQuery(normalizedQuery)
                                .build();

                locationSearchRepository.save(search);

                // ========================================================
                // SEARCH LOCATION
                // ========================================================

                List<Location> locations = locationRepository.searchLocations(normalizedQuery);

                // ========================================================
                // LOCATION NOT FOUND
                // ========================================================

                if (locations.isEmpty()) {
                        saveMissingLocation(originalQuery);
                        return List.of();
                }

                // ========================================================
                // REMOVE DUPLICATE CITY NAMES
                // ========================================================

                return locations
                                .stream()
                                .filter(location -> location.getCityName() != null &&
                                                !location.getCityName().trim().isEmpty())
                                .collect(Collectors.toMap(
                                                location -> location.getCityName()
                                                                .trim()
                                                                .toLowerCase(),

                                                location -> new LocationNameResponse(
                                                                location.getId(),
                                                                location.getCityName(),
                                                                location.getStateName()),

                                                // Same city milne par first record rakho
                                                (existing, duplicate) -> existing,

                                                // Order maintain rahe
                                                LinkedHashMap::new))
                                .values()
                                .stream()
                                .toList();
        }
        // ============================================================
        // TOP 5 MOST SEARCHED LOCATIONS
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

                        Long count = (Long) result[1];

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

        public void saveMissingLocation(String locationName) {

                String name = locationName.trim();

                Optional<MissingLocation> existing = missingLocationRepository
                                .findByLocationNameIgnoreCase(name);

                if (existing.isPresent()) {

                        MissingLocation location = existing.get();

                        location.setSearchCount(
                                        location.getSearchCount() + 1);

                        location.setLastSearchedAt(
                                        LocalDateTime.now());

                        missingLocationRepository.save(location);

                } else {

                        MissingLocation location = new MissingLocation();

                        location.setLocationName(name);
                        location.setSearchCount(1);
                        location.setResolved(false);
                        location.setLastSearchedAt(LocalDateTime.now());

                        missingLocationRepository.save(location);
                }
        }

        public List<MissingLocation> getPendingLocations() {

                return missingLocationRepository
                                .findByResolvedFalseOrderBySearchCountDesc();
        }

}