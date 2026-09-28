package com.example.tripItinerary.Service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.tripItinerary.DTO.request.TouristPlaceRequest;
import com.example.tripItinerary.DTO.response.TouristPlaceCsvImportResponse;
import com.example.tripItinerary.DTO.response.TouristPlaceResponse;
import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Entity.TouristPlace;
import com.example.tripItinerary.Mapper.TouristPlaceMapper;
import com.example.tripItinerary.Repo.LocationRepository;
import com.example.tripItinerary.Repo.TouristPlaceRepository;
import com.example.tripItinerary.Service.TouristPlaceService;
import com.example.tripItinerary.enums.PlaceCategory;
import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TouristPlaceServiceImpl
                implements TouristPlaceService {

        private final TouristPlaceRepository touristPlaceRepository;

        private final LocationRepository locationRepository;

        private final TouristPlaceMapper touristPlaceMapper;

        // ============================================================
        // PAGINATION CONFIG
        // ============================================================

        private static final int MAX_PAGE_SIZE = 100;

        private static final String DEFAULT_SORT_FIELD = "placeWeight";

        private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
                        "id",
                        "placeWeight",
                        "popularityScore",
                        "averageRating",
                        "placeName",
                        "price",
                        "createdAt",
                        "updatedAt");

        // ============================================================
        // CSV CONFIG
        // ============================================================

        private static final String CSV_LOCATION_ID = "locationId";

        private static final String CSV_PLACE_NAME = "placeName";

        private static final Set<String> REQUIRED_CSV_HEADERS = Set.of(
                        CSV_LOCATION_ID,
                        CSV_PLACE_NAME);

        // ============================================================
        // CREATE
        // ============================================================

        @Override
        public TouristPlaceResponse create(
                        TouristPlaceRequest request) {

                Location location = locationRepository
                                .findById(request.getLocationId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id : "
                                                                + request.getLocationId()));

                TouristPlace touristPlace = touristPlaceMapper.toEntity(request);

                touristPlace.setLocation(location);

                TouristPlace savedTouristPlace = touristPlaceRepository.save(
                                touristPlace);

                return touristPlaceMapper.toResponse(
                                savedTouristPlace);
        }

        // ============================================================
        // UPDATE
        // ============================================================

        @Override
        public TouristPlaceResponse update(
                        @NonNull Long id,
                        TouristPlaceRequest request) {

                TouristPlace touristPlace = touristPlaceRepository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tourist place not found with id : "
                                                                + id));

                Location location = locationRepository
                                .findById(
                                                request.getLocationId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Location not found with id : "
                                                                + request.getLocationId()));

                touristPlace.setLocation(location);

                touristPlace.setPlaceName(
                                request.getPlaceName());

                touristPlace.setDescription(
                                request.getDescription());

                touristPlace.setPrice(
                                request.getPrice());

                touristPlace.setPlaceWeight(
                                request.getPlaceWeight());

                touristPlace.setLatitude(
                                request.getLatitude());

                touristPlace.setLongitude(
                                request.getLongitude());

                touristPlace.setPopularityScore(
                                request.getPopularityScore());

                touristPlace.setBestVisitMonths(
                                request.getBestVisitMonths());

                touristPlace.setGooglePlaceId(
                                request.getGooglePlaceId());

                touristPlace.setAddress(
                                request.getAddress());

                touristPlace.setContactNumber(
                                request.getContactNumber());

                touristPlace.setWebsiteUrl(
                                request.getWebsiteUrl());

                touristPlace.setEstimatedVisitTimeMinutes(
                                request.getEstimatedVisitTimeMinutes());

                touristPlace.setCategory(
                                request.getCategory());

                touristPlace.setTravelTypes(
                                request.getTravelTypes());

                touristPlace.setOpeningTime(
                                request.getOpeningTime());

                touristPlace.setClosingTime(
                                request.getClosingTime());

                touristPlace.setActive(
                                request.getActive());

                TouristPlace updatedTouristPlace = touristPlaceRepository.save(
                                touristPlace);

                return touristPlaceMapper.toResponse(
                                updatedTouristPlace);
        }

        // ============================================================
        // GET BY ID
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public TouristPlaceResponse getById(
                        Long id) {

                TouristPlace touristPlace = touristPlaceRepository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tourist place not found with id : "
                                                                + id));

                return touristPlaceMapper.toResponse(
                                touristPlace);
        }

        // ============================================================
        // GET ALL - PAGINATED
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public Page<TouristPlaceResponse> getAll(
                        int page,
                        int size,
                        String sortBy,
                        String direction) {

                Pageable pageable = createPageable(
                                page,
                                size,
                                sortBy,
                                direction);

                return touristPlaceRepository
                                .findAllOptimized(pageable)
                                .map(touristPlaceMapper::toResponse);
        }

        // ============================================================
        // GET BY LOCATION - PAGINATED
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public Page<TouristPlaceResponse> getByLocation(
                        Long locationId,
                        int page,
                        int size,
                        String sortBy,
                        String direction) {

                Pageable pageable = createPageable(
                                page,
                                size,
                                sortBy,
                                direction);

                return touristPlaceRepository
                                .findByLocationOptimized(
                                                locationId,
                                                pageable)
                                .map(touristPlaceMapper::toResponse);
        }

        // ============================================================
        // GET ACTIVE - PAGINATED
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public Page<TouristPlaceResponse> getActive(
                        int page,
                        int size,
                        String sortBy,
                        String direction) {

                Pageable pageable = createPageable(
                                page,
                                size,
                                sortBy,
                                direction);

                return touristPlaceRepository
                                .findActiveOptimized(
                                                pageable)
                                .map(touristPlaceMapper::toResponse);
        }

        // ============================================================
        // GET ACTIVE BY LOCATION - PAGINATED
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public Page<TouristPlaceResponse> getActiveByLocation(
                        Long locationId,
                        int page,
                        int size,
                        String sortBy,
                        String direction) {

                Pageable pageable = createPageable(
                                page,
                                size,
                                sortBy,
                                direction);

                return touristPlaceRepository
                                .findActiveByLocationOptimized(
                                                locationId,
                                                pageable)
                                .map(touristPlaceMapper::toResponse);
        }

        // ============================================================
        // DELETE
        // ============================================================

        @Override
        public void delete(
                        @NonNull Long id) {

                TouristPlace touristPlace = touristPlaceRepository
                                .findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tourist place not found with id : "
                                                                + id));

                touristPlaceRepository.delete(
                                touristPlace);
        }

        // ============================================================
        // CSV IMPORT
        // ============================================================

        @Override
        public TouristPlaceCsvImportResponse importCsv(
                        MultipartFile file) {

                if (file == null || file.isEmpty()) {

                        throw new IllegalArgumentException(
                                        "CSV file is empty.");
                }

                int totalRows = 0;
                int inserted = 0;
                int updated = 0;
                int skipped = 0;
                int failed = 0;

                List<String> errors = new ArrayList<>();

                try (
                                BufferedReader reader = new BufferedReader(
                                                new InputStreamReader(
                                                                file.getInputStream(),
                                                                StandardCharsets.UTF_8));

                                CSVParser csvParser = CSVFormat.DEFAULT
                                                .builder()
                                                .setHeader()
                                                .setSkipHeaderRecord(true)
                                                .setIgnoreEmptyLines(true)
                                                .setTrim(true)
                                                .build()
                                                .parse(reader)) {

                        // ====================================================
                        // VALIDATE HEADERS
                        // ====================================================

                        validateCsvHeaders(
                                        csvParser);

                        // ====================================================
                        // READ CSV RECORDS
                        // ====================================================

                        List<CSVRecord> records = new ArrayList<>();

                        Set<Long> locationIds = new HashSet<>();

                        for (CSVRecord record : csvParser) {

                                totalRows++;

                                String locationIdValue = getValue(
                                                record,
                                                CSV_LOCATION_ID);

                                String placeName = getValue(
                                                record,
                                                CSV_PLACE_NAME);

                                // ----------------------------------------------
                                // LOCATION VALIDATION
                                // ----------------------------------------------

                                if (isBlank(locationIdValue)) {

                                        failed++;

                                        errors.add(
                                                        "Row "
                                                                        + record.getRecordNumber()
                                                                        + ": locationId is required.");

                                        continue;
                                }

                                // ----------------------------------------------
                                // PLACE NAME VALIDATION
                                // ----------------------------------------------

                                if (isBlank(placeName)) {

                                        failed++;

                                        errors.add(
                                                        "Row "
                                                                        + record.getRecordNumber()
                                                                        + ": placeName is required.");

                                        continue;
                                }

                                try {

                                        Long locationId = Long.parseLong(
                                                        locationIdValue);

                                        locationIds.add(
                                                        locationId);

                                        records.add(
                                                        record);

                                } catch (NumberFormatException ex) {

                                        failed++;

                                        errors.add(
                                                        "Row "
                                                                        + record.getRecordNumber()
                                                                        + ": invalid locationId: "
                                                                        + locationIdValue);
                                }
                        }

                        // ====================================================
                        // LOAD ALL LOCATIONS
                        //
                        // Current LocationRepository API may not have
                        // findAllById custom method.
                        //
                        // JpaRepository already provides findAllById().
                        // ====================================================

                        Map<Long, Location> locationCache = new HashMap<>(
                                        Math.max(
                                                        16,
                                                        locationIds.size() * 2));

                        if (!locationIds.isEmpty()) {

                                Iterable<Location> locations = locationRepository
                                                .findAllById(
                                                                locationIds);

                                for (Location location : locations) {

                                        locationCache.put(
                                                        location.getId(),
                                                        location);
                                }
                        }

                        // ====================================================
                        // LOAD EXISTING TOURIST PLACES
                        //
                        // ONE DB QUERY FOR ALL LOCATIONS
                        // ====================================================

                        Map<String, TouristPlace> existingPlaces = new HashMap<>();

                        if (!locationIds.isEmpty()) {

                                List<TouristPlace> existingFromDatabase = touristPlaceRepository
                                                .findForCsvImport(
                                                                locationIds);

                                for (TouristPlace touristPlace : existingFromDatabase) {

                                        if (touristPlace.getLocation() == null) {
                                                continue;
                                        }

                                        Long locationId = touristPlace
                                                        .getLocation()
                                                        .getId();

                                        String key = buildDuplicateKey(
                                                        locationId,
                                                        touristPlace
                                                                        .getPlaceName());

                                        existingPlaces.put(
                                                        key,
                                                        touristPlace);
                                }
                        }

                        // ====================================================
                        // PREPARE INSERT / UPDATE
                        // ====================================================

                        List<TouristPlace> newPlaces = new ArrayList<>();

                        List<TouristPlace> updatedPlaces = new ArrayList<>();

                        // ====================================================
                        // PROCESS CSV
                        // ====================================================

                        for (CSVRecord record : records) {

                                long rowNumber = record.getRecordNumber();

                                try {

                                        Long locationId = Long.parseLong(
                                                        getValue(
                                                                        record,
                                                                        CSV_LOCATION_ID));

                                        String placeName = getValue(
                                                        record,
                                                        CSV_PLACE_NAME);

                                        Location location = locationCache.get(
                                                        locationId);

                                        if (location == null) {

                                                failed++;

                                                errors.add(
                                                                "Row "
                                                                                + rowNumber
                                                                                + ": Location not found with id: "
                                                                                + locationId);

                                                continue;
                                        }

                                        String duplicateKey = buildDuplicateKey(
                                                        locationId,
                                                        placeName);

                                        TouristPlace existing = existingPlaces.get(
                                                        duplicateKey);

                                        // ==========================================
                                        // INSERT
                                        // ==========================================

                                        if (existing == null) {

                                                TouristPlace touristPlace = buildTouristPlaceFromCsv(
                                                                record,
                                                                location);

                                                newPlaces.add(
                                                                touristPlace);

                                                /*
                                                 * Important:
                                                 * Same CSV file ke andar duplicate row
                                                 * aaye to second row new record nahi
                                                 * banayegi.
                                                 */
                                                existingPlaces.put(
                                                                duplicateKey,
                                                                touristPlace);

                                        }

                                        // ==========================================
                                        // UPDATE
                                        // ==========================================

                                        else {

                                                updateTouristPlaceFromCsv(
                                                                existing,
                                                                record,
                                                                location);

                                                updatedPlaces.add(
                                                                existing);
                                        }

                                } catch (Exception ex) {

                                        failed++;

                                        errors.add(
                                                        "Row "
                                                                        + rowNumber
                                                                        + ": "
                                                                        + safeMessage(ex));
                                }
                        }

                        // ====================================================
                        // BULK INSERT
                        // ====================================================

                        if (!newPlaces.isEmpty()) {

                                touristPlaceRepository.saveAll(
                                                newPlaces);

                                inserted = newPlaces.size();
                        }

                        // ====================================================
                        // BULK UPDATE
                        // ====================================================

                        if (!updatedPlaces.isEmpty()) {

                                touristPlaceRepository.saveAll(
                                                updatedPlaces);

                                updated = updatedPlaces.size();
                        }

                        // ====================================================
                        // RESPONSE
                        // ====================================================

                        return TouristPlaceCsvImportResponse
                                        .builder()
                                        .totalRows(totalRows)
                                        .inserted(inserted)
                                        .updated(updated)
                                        .skipped(skipped)
                                        .failed(failed)
                                        .errors(errors)
                                        .build();

                } catch (IllegalArgumentException ex) {

                        throw ex;

                } catch (Exception ex) {

                        throw new IllegalArgumentException(
                                        "Failed to import tourist place CSV: "
                                                        + safeMessage(ex),
                                        ex);
                }
        }

        // ============================================================
        // CSV HEADER VALIDATION
        // ============================================================

        private void validateCsvHeaders(
                        CSVParser csvParser) {

                Set<String> headers = new HashSet<>(
                                csvParser
                                                .getHeaderMap()
                                                .keySet());

                for (String requiredHeader : REQUIRED_CSV_HEADERS) {

                        if (!headers.contains(
                                        requiredHeader)) {

                                throw new IllegalArgumentException(
                                                "Missing required CSV header: "
                                                                + requiredHeader);
                        }
                }
        }

        // ============================================================
        // BUILD TOURIST PLACE FROM CSV
        // ============================================================

        private TouristPlace buildTouristPlaceFromCsv(
                        CSVRecord record,
                        Location location) {

                TouristPlace touristPlace = new TouristPlace();

                touristPlace.setLocation(
                                location);

                touristPlace.setPlaceName(
                                requiredString(
                                                record,
                                                CSV_PLACE_NAME));

                touristPlace.setDescription(
                                getValue(
                                                record,
                                                "description"));

                touristPlace.setPrice(
                                parseBigDecimal(
                                                getValue(
                                                                record,
                                                                "price"),
                                                BigDecimal.ZERO));

                touristPlace.setPlaceWeight(
                                parseInteger(
                                                getValue(
                                                                record,
                                                                "placeWeight"),
                                                1));

                touristPlace.setLatitude(
                                parseBigDecimal(
                                                getValue(
                                                                record,
                                                                "latitude"),
                                                null));

                touristPlace.setLongitude(
                                parseBigDecimal(
                                                getValue(
                                                                record,
                                                                "longitude"),
                                                null));

                touristPlace.setPopularityScore(
                                parseInteger(
                                                getValue(
                                                                record,
                                                                "popularityScore"),
                                                0));

                touristPlace.setBestVisitMonths(
                                getValue(
                                                record,
                                                "bestVisitMonths"));

                touristPlace.setGooglePlaceId(
                                getValue(
                                                record,
                                                "googlePlaceId"));

                touristPlace.setAddress(
                                getValue(
                                                record,
                                                "address"));

                touristPlace.setContactNumber(
                                getValue(
                                                record,
                                                "contactNumber"));

                touristPlace.setWebsiteUrl(
                                getValue(
                                                record,
                                                "websiteUrl"));

                touristPlace.setEstimatedVisitTimeMinutes(
                                parseInteger(
                                                getValue(
                                                                record,
                                                                "estimatedVisitTimeMinutes"),
                                                null));

                touristPlace.setCategory(
                                parseCategory(
                                                getValue(
                                                                record,
                                                                "category")));

                touristPlace.setTravelTypes(
                                parseTravelTypes(
                                                getValue(
                                                                record,
                                                                "travelTypes")));

                touristPlace.setOpeningTime(
                                parseLocalTime(
                                                getValue(
                                                                record,
                                                                "openingTime")));

                touristPlace.setClosingTime(
                                parseLocalTime(
                                                getValue(
                                                                record,
                                                                "closingTime")));

                touristPlace.setActive(
                                parseBoolean(
                                                getValue(
                                                                record,
                                                                "active"),
                                                true));

                return touristPlace;
        }

        // ============================================================
        // UPDATE TOURIST PLACE FROM CSV
        // ============================================================

        private void updateTouristPlaceFromCsv(
                        TouristPlace touristPlace,
                        CSVRecord record,
                        Location location) {

                touristPlace.setLocation(
                                location);

                // --------------------------------------------------------
                // PLACE NAME
                // --------------------------------------------------------

                String value = getValue(
                                record,
                                CSV_PLACE_NAME);

                if (!isBlank(value)) {

                        touristPlace.setPlaceName(
                                        value);
                }

                // --------------------------------------------------------
                // DESCRIPTION
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "description");

                if (!isBlank(value)) {

                        touristPlace.setDescription(
                                        value);
                }

                // --------------------------------------------------------
                // PRICE
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "price");

                if (!isBlank(value)) {

                        touristPlace.setPrice(
                                        parseBigDecimal(
                                                        value,
                                                        touristPlace.getPrice()));
                }

                // --------------------------------------------------------
                // PLACE WEIGHT
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "placeWeight");

                if (!isBlank(value)) {

                        touristPlace.setPlaceWeight(
                                        parseInteger(
                                                        value,
                                                        touristPlace.getPlaceWeight()));
                }

                // --------------------------------------------------------
                // LATITUDE
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "latitude");

                if (!isBlank(value)) {

                        touristPlace.setLatitude(
                                        parseBigDecimal(
                                                        value,
                                                        touristPlace.getLatitude()));
                }

                // --------------------------------------------------------
                // LONGITUDE
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "longitude");

                if (!isBlank(value)) {

                        touristPlace.setLongitude(
                                        parseBigDecimal(
                                                        value,
                                                        touristPlace.getLongitude()));
                }

                // --------------------------------------------------------
                // POPULARITY SCORE
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "popularityScore");

                if (!isBlank(value)) {

                        touristPlace.setPopularityScore(
                                        parseInteger(
                                                        value,
                                                        touristPlace
                                                                        .getPopularityScore()));
                }

                // --------------------------------------------------------
                // BEST VISIT MONTHS
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "bestVisitMonths");

                if (!isBlank(value)) {

                        touristPlace.setBestVisitMonths(
                                        value);
                }

                // --------------------------------------------------------
                // GOOGLE PLACE ID
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "googlePlaceId");

                if (!isBlank(value)) {

                        touristPlace.setGooglePlaceId(
                                        value);
                }

                // --------------------------------------------------------
                // ADDRESS
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "address");

                if (!isBlank(value)) {

                        touristPlace.setAddress(
                                        value);
                }

                // --------------------------------------------------------
                // CONTACT NUMBER
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "contactNumber");

                if (!isBlank(value)) {

                        touristPlace.setContactNumber(
                                        value);
                }

                // --------------------------------------------------------
                // WEBSITE
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "websiteUrl");

                if (!isBlank(value)) {

                        touristPlace.setWebsiteUrl(
                                        value);
                }

                // --------------------------------------------------------
                // ESTIMATED VISIT TIME
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "estimatedVisitTimeMinutes");

                if (!isBlank(value)) {

                        touristPlace
                                        .setEstimatedVisitTimeMinutes(
                                                        parseInteger(
                                                                        value,
                                                                        touristPlace
                                                                                        .getEstimatedVisitTimeMinutes()));
                }

                // --------------------------------------------------------
                // CATEGORY
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "category");

                if (!isBlank(value)) {

                        touristPlace.setCategory(
                                        parseCategory(value));
                }

                // --------------------------------------------------------
                // TRAVEL TYPES
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "travelTypes");

                if (!isBlank(value)) {

                        touristPlace.setTravelTypes(
                                        parseTravelTypes(value));
                }

                // --------------------------------------------------------
                // OPENING TIME
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "openingTime");

                if (!isBlank(value)) {

                        touristPlace.setOpeningTime(
                                        parseLocalTime(value));
                }

                // --------------------------------------------------------
                // CLOSING TIME
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "closingTime");

                if (!isBlank(value)) {

                        touristPlace.setClosingTime(
                                        parseLocalTime(value));
                }

                // --------------------------------------------------------
                // ACTIVE
                // --------------------------------------------------------

                value = getValue(
                                record,
                                "active");

                if (!isBlank(value)) {

                        touristPlace.setActive(
                                        parseBoolean(
                                                        value,
                                                        touristPlace.getActive()));
                }
        }

        // ============================================================
        // PAGEABLE
        // ============================================================

        private Pageable createPageable(
                        int page,
                        int size,
                        String sortBy,
                        String direction) {

                int safePage = Math.max(
                                page,
                                0);

                int safeSize = Math.min(
                                Math.max(
                                                size,
                                                1),
                                MAX_PAGE_SIZE);

                String safeSortBy = ALLOWED_SORT_FIELDS.contains(
                                sortBy)
                                                ? sortBy
                                                : DEFAULT_SORT_FIELD;

                Sort.Direction sortDirection = "asc".equalsIgnoreCase(
                                direction)
                                                ? Sort.Direction.ASC
                                                : Sort.Direction.DESC;

                /*
                 * Primary sort:
                 * user requested field
                 *
                 * Secondary sort:
                 * id ASC
                 *
                 * Isse pagination deterministic/stable
                 * rahegi jab multiple records ka
                 * placeWeight/popularity same ho.
                 */

                Sort sort = Sort.by(
                                sortDirection,
                                safeSortBy)
                                .and(
                                                Sort.by(
                                                                Sort.Direction.ASC,
                                                                "id"));

                return PageRequest.of(
                                safePage,
                                safeSize,
                                sort);
        }

        // ============================================================
        // COUNT ALL
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public long countAll() {

                return touristPlaceRepository
                                .countAllTouristPlaces();
        }

        // ============================================================
        // COUNT BY LOCATION
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public long countByLocation(
                        Long locationId) {

                return touristPlaceRepository
                                .countByLocationId(
                                                locationId);
        }

        // ============================================================
        // COUNT ACTIVE
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public long countActive() {

                return touristPlaceRepository
                                .countActiveTouristPlaces();
        }

        // ============================================================
        // COUNT ACTIVE BY LOCATION
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public long countActiveByLocation(
                        Long locationId) {

                return touristPlaceRepository
                                .countActiveByLocationId(
                                                locationId);
        }

        // ============================================================
        // CSV VALUE
        // ============================================================

        private String getValue(
                        CSVRecord record,
                        String header) {

                if (!record.isMapped(
                                header)) {

                        return null;
                }

                String value = record.get(header);

                if (value == null) {
                        return null;
                }

                return value.trim();
        }

        // ============================================================
        // REQUIRED STRING
        // ============================================================

        private String requiredString(
                        CSVRecord record,
                        String header) {

                String value = getValue(
                                record,
                                header);

                if (isBlank(value)) {

                        throw new IllegalArgumentException(
                                        header + " is required.");
                }

                return value;
        }

        // ============================================================
        // BLANK
        // ============================================================

        private boolean isBlank(
                        String value) {

                return value == null
                                || value.trim().isEmpty();
        }

        // ============================================================
        // INTEGER
        // ============================================================

        private Integer parseInteger(
                        String value,
                        Integer defaultValue) {

                if (isBlank(value)) {
                        return defaultValue;
                }

                try {

                        return Integer.parseInt(
                                        value.trim());

                } catch (NumberFormatException ex) {

                        throw new IllegalArgumentException(
                                        "Invalid integer value: "
                                                        + value);
                }
        }

        // ============================================================
        // BIG DECIMAL
        // ============================================================

        private BigDecimal parseBigDecimal(
                        String value,
                        BigDecimal defaultValue) {

                if (isBlank(value)) {
                        return defaultValue;
                }

                try {

                        return new BigDecimal(
                                        value.trim());

                } catch (NumberFormatException ex) {

                        throw new IllegalArgumentException(
                                        "Invalid decimal value: "
                                                        + value);
                }
        }

        // ============================================================
        // LOCAL TIME
        // ============================================================

        private LocalTime parseLocalTime(
                        String value) {

                if (isBlank(value)) {
                        return null;
                }

                try {

                        return LocalTime.parse(
                                        value.trim());

                } catch (Exception ex) {

                        throw new IllegalArgumentException(
                                        "Invalid time value: "
                                                        + value
                                                        + ". Expected HH:mm.");
                }
        }

        // ============================================================
        // BOOLEAN
        // ============================================================

        private Boolean parseBoolean(
                        String value,
                        Boolean defaultValue) {

                if (isBlank(value)) {
                        return defaultValue;
                }

                String normalized = value.trim()
                                .toLowerCase();

                return switch (normalized) {

                        case "true",
                                        "1",
                                        "yes",
                                        "y" ->
                                true;

                        case "false",
                                        "0",
                                        "no",
                                        "n" ->
                                false;

                        default -> defaultValue;
                };
        }

        // ============================================================
        // LONG
        // ============================================================

        // private Long parseLong(
        //                 String value) {

        //         if (isBlank(value)) {
        //                 return null;
        //         }

        //         try {

        //                 return Long.parseLong(
        //                                 value.trim());

        //         } catch (NumberFormatException ex) {

        //                 throw new IllegalArgumentException(
        //                                 "Invalid long value: "
        //                                                 + value);
        //         }
        // }

        // ============================================================
        // CATEGORY
        // ============================================================

        private PlaceCategory parseCategory(
                        String value) {

                if (isBlank(value)) {
                        return null;
                }

                try {

                        return PlaceCategory.valueOf(
                                        value.trim()
                                                        .toUpperCase());

                } catch (IllegalArgumentException ex) {

                        throw new IllegalArgumentException(
                                        "Invalid category: "
                                                        + value);
                }
        }

        // ============================================================
        // TRAVEL TYPES
        // ============================================================

        private List<String> parseTravelTypes(
                        String value) {

                if (isBlank(value)) {

                        return new ArrayList<>();
                }

                String normalized = value.trim()
                                .replace(
                                                "[",
                                                "")
                                .replace(
                                                "]",
                                                "")
                                .replace(
                                                "\"",
                                                "");

                String[] values = normalized.split(
                                "[,;|]");

                List<String> result = new ArrayList<>(
                                values.length);

                for (String item : values) {

                        String trimmed = item.trim();

                        if (!trimmed.isEmpty()) {

                                result.add(
                                                trimmed);
                        }
                }

                return result;
        }

        // ============================================================
        // DUPLICATE KEY
        // ============================================================

        private String buildDuplicateKey(
                        Long locationId,
                        String placeName) {

                return locationId
                                + ":"
                                + normalizeName(
                                                placeName);
        }

        // ============================================================
        // NORMALIZE PLACE NAME
        // ============================================================

        private String normalizeName(
                        String value) {

                if (value == null) {

                        return "";
                }

                return value
                                .trim()
                                .replaceAll(
                                                "\\s+",
                                                " ")
                                .toLowerCase();
        }

        // ============================================================
        // SAFE EXCEPTION MESSAGE
        // ============================================================

        private String safeMessage(
                        Exception ex) {

                String message = ex.getMessage();

                if (message == null
                                || message.isBlank()) {

                        return ex.getClass()
                                        .getSimpleName();
                }

                return message;
        }
}