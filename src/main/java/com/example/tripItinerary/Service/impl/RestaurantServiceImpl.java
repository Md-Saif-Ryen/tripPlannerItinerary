// package com.example.tripItinerary.Service.impl;

// import java.time.format.DateTimeFormatter;
// import java.util.List;
// import java.util.stream.Collectors;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.example.tripItinerary.DTO.request.RestaurantRequest;
// import com.example.tripItinerary.DTO.response.RestaurantResponse;
// import com.example.tripItinerary.Entity.Location;
// import com.example.tripItinerary.Entity.Restaurant;
// import com.example.tripItinerary.Mapper.RestaurantMapper;
// import com.example.tripItinerary.Repo.LocationRepository;
// import com.example.tripItinerary.Repo.RestaurantRepository;
// import com.example.tripItinerary.Service.RestaurantService;
// import com.example.tripItinerary.exception.ResourceNotFoundException;

// import lombok.NonNull;
// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// @Transactional
// public class RestaurantServiceImpl implements RestaurantService {

//     private final RestaurantRepository restaurantRepository;
//     private final LocationRepository locationRepository;
//     private final RestaurantMapper restaurantMapper;

//      private static final DateTimeFormatter TIME_FORMATTER =
//             DateTimeFormatter.ofPattern("HH:mm:ss");

//     private static final Pattern RATING_PATTERN =
//             Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:/5)?");

//     private static final Pattern PRICE_PATTERN =
//             Pattern.compile("₹\\s*([\\d,]+)\\s*[–\\-]\\s*([\\d,]+)");

//     private static final Pattern SINGLE_PRICE_PATTERN =
//             Pattern.compile("₹\\s*([\\d,]+)");

//     @Override
//     public RestaurantResponse create(RestaurantRequest request) {

//         Location location = locationRepository.findById(request.getLocationId())
//                 .orElseThrow(() -> new ResourceNotFoundException(
//                         "Location not found with id : " + request.getLocationId()));

//         Restaurant restaurant = restaurantMapper.toEntity(request);

//         restaurant.setLocation(location);

//         restaurant = restaurantRepository.save(restaurant);

//         return restaurantMapper.toResponse(restaurant);
//     }

//     @Override
//     public RestaurantResponse update(Long id, RestaurantRequest request) {

//         Restaurant restaurant = restaurantRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException(
//                         "Restaurant not found with id : " + id));

//         Location location = locationRepository.findById(request.getLocationId())
//                 .orElseThrow(() -> new ResourceNotFoundException(
//                         "Location not found with id : " + request.getLocationId()));

//         restaurant.setLocation(location);
//         restaurant.setRestaurantName(request.getRestaurantName());
//         restaurant.setDescription(request.getDescription());
//         restaurant.setAddress(request.getAddress());
//         restaurant.setAverageCostPerPerson(request.getAverageCostPerPerson());
//         restaurant.setRestaurantWeight(request.getRestaurantWeight());
//         restaurant.setLatitude(request.getLatitude());
//         restaurant.setLongitude(request.getLongitude());
//         restaurant.setCuisineType(request.getCuisineType());
//         restaurant.setOpeningTime(request.getOpeningTime());
//         restaurant.setClosingTime(request.getClosingTime());
//         restaurant.setVeg(request.getVeg());
//         restaurant.setActive(request.getActive());
//         restaurant.setContactNumber(request.getContactNumber());
//         restaurant.setWebsiteUrl(request.getWebsiteUrl());

//         restaurant = restaurantRepository.save(restaurant);

//         return restaurantMapper.toResponse(restaurant);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public RestaurantResponse getById(Long id) {

//         Restaurant restaurant = restaurantRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException(
//                         "Restaurant not found with id : " + id));

//         return restaurantMapper.toResponse(restaurant);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public List<RestaurantResponse> getAll() {

//         return restaurantRepository.findAll()
//                 .stream()
//                 .map(restaurantMapper::toResponse)
//                 .collect(Collectors.toList());
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public List<RestaurantResponse> getByLocation(Long locationId) {

//         return restaurantRepository.findByLocationId(locationId)
//                 .stream()
//                 .map(restaurantMapper::toResponse)
//                 .collect(Collectors.toList());
//     }

//     @Override
//     public void delete(@NonNull Long id) {

//         Restaurant restaurant = restaurantRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException(
//                         "Restaurant not found with id : " + id));

//         restaurantRepository.delete(restaurant);
//     }

// }

package com.example.tripItinerary.Service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import com.example.tripItinerary.DTO.request.RestaurantRequest;
import com.example.tripItinerary.DTO.response.PageResponse;
import com.example.tripItinerary.DTO.response.RestaurantCsvImportResponse;
import com.example.tripItinerary.DTO.response.RestaurantResponse;

import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Entity.Restaurant;

import com.example.tripItinerary.Mapper.RestaurantMapper;

import com.example.tripItinerary.Repo.LocationRepository;
import com.example.tripItinerary.Repo.RestaurantRepository;

import com.example.tripItinerary.Service.RestaurantService;

import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final LocationRepository locationRepository;
    private final RestaurantMapper restaurantMapper;

    private static final Pattern RATING_PATTERN = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(?:/5)?");

    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "₹\\s*([\\d,]+)\\s*[–\\-]\\s*([\\d,]+)");

    private static final Pattern SINGLE_PRICE_PATTERN = Pattern.compile(
            "₹\\s*([\\d,]+)");

    // ============================================================
    // CREATE
    // ============================================================

    @Override
    public RestaurantResponse create(
            RestaurantRequest request) {

        /*
         * getReferenceById() does NOT immediately hit DB.
         * Hibernate creates a lazy FK reference.
         *
         * This avoids:
         *
         * locationRepository.findById(...)
         *
         * for every create request.
         */

        Location location = locationRepository.getReferenceById(
                request.getLocationId());

        Restaurant restaurant = restaurantMapper.toEntity(request);

        restaurant.setLocation(location);
        restaurant.setRestaurantWeight(
                calculateRestaurantWeight(restaurant));

        Restaurant saved = restaurantRepository.save(restaurant);

        return restaurantMapper.toResponse(saved);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @Override
    public RestaurantResponse update(
            Long id,
            RestaurantRequest request) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id : " + id));

        /*
         * Only create a reference.
         * No unnecessary SELECT for location.
         */

        if (!restaurant.getLocation()
                .getId()
                .equals(request.getLocationId())) {

            restaurant.setLocation(
                    locationRepository.getReferenceById(
                            request.getLocationId()));
        }

        restaurant.setRestaurantName(
                request.getRestaurantName());

        restaurant.setDescription(
                request.getDescription());

        restaurant.setAddress(
                request.getAddress());

        restaurant.setAverageCostPerPerson(
                request.getAverageCostPerPerson());

        restaurant.setRestaurantWeight(
                calculateRestaurantWeight(restaurant));

        restaurant.setLatitude(
                request.getLatitude());

        restaurant.setLongitude(
                request.getLongitude());

        restaurant.setCuisineType(
                request.getCuisineType());

        restaurant.setOpeningTime(
                request.getOpeningTime());

        restaurant.setClosingTime(
                request.getClosingTime());

        restaurant.setVeg(
                request.getVeg());

        restaurant.setActive(
                request.getActive());

        restaurant.setContactNumber(
                request.getContactNumber());

        restaurant.setWebsiteUrl(
                request.getWebsiteUrl());

        /*
         * NO save() required.
         *
         * Entity is managed by Hibernate.
         * Dirty checking automatically updates it.
         */

        return restaurantMapper.toResponse(
                restaurant);
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getById(
            Long id) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id : " + id));

        return restaurantMapper.toResponse(
                restaurant);
    }

    // ============================================================
    // GET ALL - PAGINATED
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RestaurantResponse> getAll(
            Pageable pageable) {

        Page<Restaurant> page = restaurantRepository
                .findAllByOrderByIdDesc(
                        pageable);

        return toPageResponse(page);
    }

    // ============================================================
    // GET BY LOCATION - PAGINATED
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RestaurantResponse> getByLocation(
            Long locationId,
            Pageable pageable) {

        Page<Restaurant> page = restaurantRepository
                .findByLocationId(
                        locationId,
                        pageable);

        return toPageResponse(page);
    }

    // ============================================================
    // SEARCH
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RestaurantResponse> search(
            Long locationId,
            String keyword,
            Pageable pageable) {

        if (keyword == null ||
                keyword.trim().isEmpty()) {

            return getByLocation(
                    locationId,
                    pageable);
        }

        Page<Restaurant> page = restaurantRepository.searchByLocation(
                locationId,
                keyword.trim(),
                pageable);

        return toPageResponse(page);
    }

    // ============================================================
    // DELETE
    // ============================================================

    @Override
    public void delete(
            @NonNull Long id) {

        /*
         * deleteById() avoids loading the entire entity.
         *
         * Much faster than:
         *
         * findById()
         * +
         * delete(entity)
         */

        if (!restaurantRepository.existsById(id)) {

            throw new ResourceNotFoundException(
                    "Restaurant not found with id : " + id);
        }

        restaurantRepository.deleteById(id);
    }

    // ============================================================
    // PAGE MAPPER
    // ============================================================

    private PageResponse<RestaurantResponse> toPageResponse(
            Page<Restaurant> page) {

        List<RestaurantResponse> content = page.getContent()
                .stream()
                .map(restaurantMapper::toResponse)
                .toList();

        return PageResponse.<RestaurantResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    // ============================================================
    // CSV IMPORT - BULK OPTIMIZED
    // ============================================================

    @Override
    public RestaurantCsvImportResponse importCsv(
            MultipartFile file,
            Long locationId) {

        validateCsvFile(file);

        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id : "
                                + locationId));

        RestaurantCsvImportResponse result = RestaurantCsvImportResponse.builder()
                .totalRows(0)
                .inserted(0)
                .updated(0)
                .skipped(0)
                .failed(0)
                .errors(new ArrayList<>())
                .build();

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                file.getInputStream(),
                                StandardCharsets.UTF_8));

                CSVParser parser = CSVFormat.DEFAULT
                        .builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreEmptyLines(true)
                        .setTrim(true)
                        .build()
                        .parse(reader)) {

            /*
             * STEP 1
             *
             * Read existing names ONCE.
             *
             * Instead of:
             *
             * 500 rows = 500 SELECT queries
             *
             * we do:
             *
             * 1 SELECT
             */

            List<String> existingNames = restaurantRepository
                    .findRestaurantNamesByLocationId(
                            locationId);

            Map<String, Restaurant> existingMap = new HashMap<>();

            for (Restaurant restaurant : restaurantRepository
                    .findByLocationId(
                            locationId,
                            Pageable.unpaged())
                    .getContent()) {

                existingMap.put(
                        normalizeName(
                                restaurant.getRestaurantName()),
                        restaurant);
            }

            /*
             * STEP 2
             *
             * Parse CSV.
             */

            List<Restaurant> newRestaurants = new ArrayList<>();

            List<Restaurant> updatedRestaurants = new ArrayList<>();

            Set<String> csvNames = new HashSet<>();

            for (CSVRecord record : parser) {

                result.setTotalRows(
                        result.getTotalRows() + 1);

                try {

                    Restaurant imported = parseRestaurant(
                            record,
                            location);

                    if (imported == null) {

                        result.setSkipped(
                                result.getSkipped() + 1);

                        continue;
                    }

                    String normalizedName = normalizeName(
                            imported.getRestaurantName());

                    /*
                     * Prevent duplicate rows
                     * inside same CSV.
                     */

                    if (!csvNames.add(normalizedName)) {

                        result.setSkipped(
                                result.getSkipped() + 1);

                        continue;
                    }

                    Restaurant existing = existingMap.get(
                            normalizedName);

                    if (existing != null) {

                        updateFromCsv(
                                existing,
                                imported);

                        updatedRestaurants.add(
                                existing);

                        result.setUpdated(
                                result.getUpdated() + 1);

                    } else {

                        newRestaurants.add(
                                imported);

                        result.setInserted(
                                result.getInserted() + 1);
                    }

                } catch (Exception e) {

                    result.setFailed(
                            result.getFailed() + 1);

                    result.getErrors().add(
                            "Row "
                                    + record.getRecordNumber()
                                    + ": "
                                    + e.getMessage());
                }
            }

            /*
             * STEP 3
             *
             * BULK INSERT.
             *
             * Not save() for every restaurant.
             */

            if (!newRestaurants.isEmpty()) {

                restaurantRepository.saveAll(
                        newRestaurants);
            }

            /*
             * STEP 4
             *
             * Bulk update.
             */

            if (!updatedRestaurants.isEmpty()) {

                restaurantRepository.saveAll(
                        updatedRestaurants);
            }

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Unable to process CSV: "
                            + e.getMessage(),
                    e);
        }

        return result;
    }

    // ============================================================
    // CSV PARSER
    // ============================================================

    private Restaurant parseRestaurant(
            CSVRecord record,
            Location location) {

        String name = getValue(record, "restaurantName");

        if (!hasText(name)) {
            return null;
        }

        String description = getValue(record, "description");
        String address = getValue(record, "address");

        BigDecimal averageCost = parseBigDecimal(
                getValue(record, "averageCostPerPerson"));

        BigDecimal latitude = parseBigDecimal(
                getValue(record, "latitude"));

        BigDecimal longitude = parseBigDecimal(
                getValue(record, "longitude"));

        String cuisineType = getValue(record, "cuisineType");

        LocalTime openingTime = parseLocalTime(
                getValue(record, "openingTime"));

        LocalTime closingTime = parseLocalTime(
                getValue(record, "closingTime"));

        Boolean veg = parseBoolean(
                getValue(record, "veg"));

        Boolean active = parseBoolean(
                getValue(record, "active"));

        String contactNumber = cleanPhone(
                getValue(record, "contactNumber"));

        String website = cleanWebsite(
                getValue(record, "websiteUrl"));

        /*
         * Rating can come from CSV if Rating column exists.
         *
         * Example:
         * 4.8/5 (18 reviews)
         * 4.2 (3,057)
         * 5.0/5
         */
        String ratingText = getValue(record, "Rating");

        BigDecimal averageRating = parseRating(ratingText);

        Restaurant restaurant = Restaurant.builder()
                .location(location)
                .restaurantName(cleanText(name))
                .description(cleanText(description))
                .address(cleanText(address))
                .averageCostPerPerson(averageCost)
                .averageRating(
                        averageRating != null
                                ? averageRating
                                : BigDecimal.ZERO)
                .latitude(latitude)
                .longitude(longitude)
                .cuisineType(cleanText(cuisineType))
                .openingTime(openingTime)
                .closingTime(closingTime)
                .veg(veg)
                .active(active)
                .contactNumber(contactNumber)
                .websiteUrl(website)
                .build();

        /*
         * IMPORTANT:
         * Weight is ALWAYS calculated by backend.
         *
         * CSV restaurantWeight is ignored.
         */
        restaurant.setRestaurantWeight(
                calculateRestaurantWeight(
                        restaurant));

        return restaurant;
    }

    private BigDecimal parseBigDecimal(String value) {

        if (!hasText(value)) {
            return null;
        }

        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Boolean parseBoolean(String value) {

        if (!hasText(value)) {
            return null;
        }

        String normalized = value.trim()
                .toLowerCase(Locale.ENGLISH);

        if ("true".equals(normalized)) {
            return true;
        }

        if ("false".equals(normalized)) {
            return false;
        }

        return null;
    }

    private LocalTime parseLocalTime(String value) {

        if (!hasText(value)) {
            return null;
        }

        try {
            return LocalTime.parse(
                    value.trim(),
                    DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ============================================================
    // CSV UPDATE
    // ============================================================

    private void updateFromCsv(
            Restaurant current,
            Restaurant imported) {

        if (hasText(imported.getDescription())) {
            current.setDescription(
                    imported.getDescription());
        }

        if (hasText(imported.getAddress())) {
            current.setAddress(
                    imported.getAddress());
        }

        if (imported.getAverageCostPerPerson() != null) {
            current.setAverageCostPerPerson(
                    imported.getAverageCostPerPerson());
        }

        if (imported.getAverageRating() != null) {
            current.setAverageRating(
                    imported.getAverageRating());
        }

        if (hasText(imported.getCuisineType())) {
            current.setCuisineType(
                    imported.getCuisineType());
        }

        if (imported.getOpeningTime() != null) {
            current.setOpeningTime(
                    imported.getOpeningTime());
        }

        if (imported.getClosingTime() != null) {
            current.setClosingTime(
                    imported.getClosingTime());
        }

        if (hasText(imported.getContactNumber())) {
            current.setContactNumber(
                    imported.getContactNumber());
        }

        if (hasText(imported.getWebsiteUrl())) {
            current.setWebsiteUrl(
                    imported.getWebsiteUrl());
        }

        if (imported.getVeg() != null) {
            current.setVeg(imported.getVeg());
        }

        if (imported.getActive() != null) {
            current.setActive(imported.getActive());
        }
        if (imported.getLatitude() != null) {
            current.setLatitude(imported.getLatitude());
        }

        if (imported.getLongitude() != null) {
            current.setLongitude(imported.getLongitude());
        }

        current.setRestaurantWeight(
                calculateRestaurantWeight(current));

    }

    // ============================================================
    // HELPERS
    // ============================================================

    private String getValue(
            CSVRecord record,
            String column) {

        try {

            if (!record.isMapped(column)) {
                return null;
            }

            return cleanText(
                    record.get(column));

        } catch (Exception e) {

            return null;
        }
    }

    private BigDecimal parseRating(
            String value) {

        if (!hasText(value)) {
            return null;
        }

        Matcher matcher = RATING_PATTERN.matcher(value);

        if (!matcher.find()) {
            return null;
        }

        try {

            BigDecimal rating = new BigDecimal(
                    matcher.group(1));

            if (rating.compareTo(
                    BigDecimal.ZERO) < 0) {
                return null;
            }

            if (rating.compareTo(
                    BigDecimal.valueOf(5)) > 0) {
                return null;
            }

            return rating;

        } catch (Exception e) {

            return null;
        }
    }

    private BigDecimal parseAverageCost(
            String value) {

        if (!hasText(value)) {
            return null;
        }

        Matcher range = PRICE_PATTERN.matcher(value);

        if (range.find()) {

            try {

                BigDecimal min = new BigDecimal(
                        range.group(1)
                                .replace(",", ""));

                BigDecimal max = new BigDecimal(
                        range.group(2)
                                .replace(",", ""));

                return min
                        .add(max)
                        .divide(
                                BigDecimal.valueOf(2),
                                2,
                                java.math.RoundingMode.HALF_UP);

            } catch (Exception ignored) {
            }
        }

        Matcher single = SINGLE_PRICE_PATTERN.matcher(value);

        if (single.find()) {

            try {

                return new BigDecimal(
                        single.group(1)
                                .replace(",", ""));

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private Integer calculateRestaurantWeight(
            Restaurant restaurant) {

        /*
         * Lower weight = better restaurant.
         *
         * Maximum possible weight = 100
         * Minimum possible weight = 1
         */

        double weight = 100.0;

        // ==========================================================
        // 1. AVERAGE RATING - MAX 40 POINT IMPROVEMENT
        // ==========================================================

        BigDecimal rating = restaurant.getAverageRating();

        if (rating != null
                && rating.compareTo(BigDecimal.ZERO) > 0) {

            double ratingValue = rating.doubleValue();

            // Keep rating between 1 and 5
            ratingValue = Math.max(
                    1.0,
                    Math.min(5.0, ratingValue));

            /*
             * 5.0 -> -40
             * 4.5 -> -35
             * 4.0 -> -30
             * 3.0 -> -20
             * 2.0 -> -10
             * 1.0 -> 0
             */

            double ratingScore = ((ratingValue - 1.0) / 4.0) * 40.0;

            weight -= ratingScore;

            int finalWeight = (int) Math.round(weight / 10.0);

            return Math.max(
                    1,
                    Math.min(10, finalWeight));
        }

        // ==========================================================
        // 2. AVERAGE COST PER PERSON - MAX 20 POINT IMPROVEMENT
        // ==========================================================

        BigDecimal cost = restaurant.getAverageCostPerPerson();

        if (cost != null
                && cost.compareTo(BigDecimal.ZERO) > 0) {

            double costValue = cost.doubleValue();

            /*
             * Ideal range = ₹200 - ₹500
             *
             * Very expensive restaurants get less benefit.
             */

            if (costValue <= 200) {

                weight -= 20;

            } else if (costValue <= 300) {

                weight -= 20;

            } else if (costValue <= 500) {

                weight -= 18;

            } else if (costValue <= 750) {

                weight -= 12;

            } else if (costValue <= 1000) {

                weight -= 7;

            } else if (costValue <= 1500) {

                weight -= 3;
            }

            // > ₹1500 => no cost benefit
        }

        // ==========================================================
        // 3. CUISINE TYPE - MAX 10 POINT IMPROVEMENT
        // ==========================================================

        String cuisine = restaurant.getCuisineType();

        if (hasText(cuisine)) {

            /*
             * One cuisine
             */
            String[] cuisines = cuisine.split(",");

            int cuisineCount = cuisines.length;

            if (cuisineCount >= 3) {

                weight -= 10;

            } else if (cuisineCount == 2) {

                weight -= 8;

            } else {

                weight -= 6;
            }
        }

        // ==========================================================
        // 4. OPENING / CLOSING TIME - MAX 10
        // ==========================================================

        LocalTime opening = restaurant.getOpeningTime();

        LocalTime closing = restaurant.getClosingTime();

        if (opening != null && closing != null) {

            /*
             * 00:00 -> 00:00
             * means 24 hours
             */
            if (opening.equals(LocalTime.MIDNIGHT)
                    && closing.equals(LocalTime.MIDNIGHT)) {

                weight -= 10;

            } else {

                int minutesOpen = calculateOpeningDuration(
                        opening,
                        closing);

                /*
                 * 8-16 hours = excellent
                 */
                if (minutesOpen >= 8 * 60
                        && minutesOpen <= 16 * 60) {

                    weight -= 10;

                } else if (minutesOpen >= 6 * 60) {

                    weight -= 7;

                } else if (minutesOpen >= 4 * 60) {

                    weight -= 4;

                } else {

                    weight -= 2;
                }
            }
        }

        // ==========================================================
        // 5. VEG AVAILABLE - MAX 5
        // ==========================================================

        if (Boolean.TRUE.equals(
                restaurant.getVeg())) {

            weight -= 5;
        }

        // ==========================================================
        // 6. CONTACT NUMBER - MAX 5
        // ==========================================================

        if (hasText(
                restaurant.getContactNumber())) {

            weight -= 5;
        }

        // ==========================================================
        // 7. WEBSITE - MAX 10
        // ==========================================================

        if (hasText(
                restaurant.getWebsiteUrl())) {

            weight -= 10;
        }

        // ==========================================================
        // FINAL
        // ==========================================================

        int finalWeight = (int) Math.round(weight);

        return Math.max(
                1,
                Math.min(100, finalWeight));
    }

    private int calculateOpeningDuration(
            LocalTime opening,
            LocalTime closing) {

        int openingMinutes = opening.getHour() * 60
                + opening.getMinute();

        int closingMinutes = closing.getHour() * 60
                + closing.getMinute();

        /*
         * Restaurant closes after midnight.
         *
         * Example:
         * 10:00 PM -> 02:00 AM
         */
        if (closingMinutes <= openingMinutes) {
            closingMinutes += 24 * 60;
        }

        return closingMinutes - openingMinutes;
    }

    private TimeRange parseHours(
            String value) {

        if (!hasText(value)) {
            return null;
        }

        String text = value
                .toLowerCase(Locale.ENGLISH)
                .trim();

        if (text.contains("open 24") ||
                text.contains("24 hours")) {

            return new TimeRange(
                    LocalTime.MIDNIGHT,
                    LocalTime.MIDNIGHT);
        }

        Pattern pattern = Pattern.compile(
                "(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)"
                        + "\\s*[-–to]+\\s*"
                        + "(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)");

        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {

            try {

                LocalTime opening = parse12Hour(
                        matcher.group(1),
                        matcher.group(2),
                        matcher.group(3));

                LocalTime closing = parse12Hour(
                        matcher.group(4),
                        matcher.group(5),
                        matcher.group(6));

                return new TimeRange(
                        opening,
                        closing);

            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private LocalTime parse12Hour(
            String hour,
            String minute,
            String period) {

        int h = Integer.parseInt(hour);

        int m = minute == null
                ? 0
                : Integer.parseInt(minute);

        if ("pm".equals(period) && h != 12) {
            h += 12;
        }

        if ("am".equals(period) && h == 12) {
            h = 0;
        }

        return LocalTime.of(h, m);
    }

    private String cleanPhone(
            String value) {

        if (!hasText(value)) {
            return null;
        }

        String phone = value.replaceAll(
                "[^0-9+]",
                "");

        return phone.isBlank()
                ? null
                : phone;
    }

    private String cleanWebsite(
            String value) {

        if (!hasText(value)) {
            return null;
        }

        String website = value.trim();

        if (!website.startsWith("http://") &&
                !website.startsWith("https://")) {

            return null;
        }

        return website;
    }

    private String cleanText(
            String value) {

        if (value == null) {
            return null;
        }

        String cleaned = value
                .replace("\uFEFF", "")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isBlank() ||
                cleaned.equalsIgnoreCase("null") ||
                cleaned.equalsIgnoreCase("n/a") ||
                cleaned.equals("-")) {

            return null;
        }

        return cleaned;
    }

    private String normalizeName(
            String name) {

        if (name == null) {
            return "";
        }

        return name
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9]", "");
    }

    private boolean hasText(
            String value) {

        return value != null &&
                !value.trim().isEmpty();
    }

    private void validateCsvFile(
            MultipartFile file) {

        if (file == null ||
                file.isEmpty()) {

            throw new IllegalArgumentException(
                    "CSV file is required.");
        }

        String filename = file.getOriginalFilename();

        if (filename == null ||
                !filename
                        .toLowerCase(Locale.ENGLISH)
                        .endsWith(".csv")) {

            throw new IllegalArgumentException(
                    "Only CSV files are allowed.");
        }
    }

    private static class TimeRange {

        private final LocalTime opening;
        private final LocalTime closing;

        private TimeRange(
                LocalTime opening,
                LocalTime closing) {

            this.opening = opening;
            this.closing = closing;
        }
    }
}