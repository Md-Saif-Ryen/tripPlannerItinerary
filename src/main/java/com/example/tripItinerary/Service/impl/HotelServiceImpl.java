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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.tripItinerary.DTO.request.HotelRequest;
import com.example.tripItinerary.DTO.response.HotelCsvImportResponse;
import com.example.tripItinerary.DTO.response.HotelResponse;
import com.example.tripItinerary.Entity.Hotel;
import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Mapper.HotelMapper;
import com.example.tripItinerary.Repo.HotelRepository;
import com.example.tripItinerary.Repo.LocationRepository;
import com.example.tripItinerary.Service.HotelService;
import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HotelServiceImpl implements HotelService {

        private final HotelRepository hotelRepository;
        private final LocationRepository locationRepository;
        private final HotelMapper hotelMapper;

        // ============================================================
        // CREATE
        // ============================================================

        @Override
        public HotelResponse create(HotelRequest request) {

                Location location = locationRepository.findById(
                                request.getLocationId()).orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "Location not found with id : "
                                                                                + request.getLocationId()));

                Hotel hotel = hotelMapper.toEntity(request);
                hotel.setLocation(location);

                /*
                 * If your Hotel entity already calculates hotelWeight
                 * somewhere else, keep that logic there.
                 */
                if (hotel.getHotelWeight() == null) {
                        hotel.setHotelWeight(1);
                }

                Hotel saved = hotelRepository.save(hotel);

                return hotelMapper.toResponse(saved);
        }

        // ============================================================
        // BULK CREATE
        // ============================================================

        @Override
        public List<HotelResponse> bulkCreate(
                        List<HotelRequest> requests) {

                List<Hotel> hotels = new ArrayList<>();

                /*
                 * Cache locations so the same locationId is not fetched
                 * repeatedly.
                 */
                Map<Long, Location> locationCache = new HashMap<>();

                for (HotelRequest request : requests) {

                        Location location = locationCache.get(
                                        request.getLocationId());

                        if (location == null) {

                                location = locationRepository.findById(
                                                request.getLocationId())
                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                "Location not found with id : "
                                                                                + request.getLocationId()));

                                locationCache.put(
                                                request.getLocationId(),
                                                location);
                        }

                        Hotel hotel = hotelMapper.toEntity(request);
                        hotel.setLocation(location);

                        if (hotel.getHotelWeight() == null) {
                                hotel.setHotelWeight(1);
                        }

                        hotels.add(hotel);
                }

                List<Hotel> savedHotels = hotelRepository.saveAll(hotels);

                return savedHotels.stream()
                                .map(hotelMapper::toResponse)
                                .toList();
        }

        // ============================================================
        // UPDATE
        // ============================================================

        @Override
        public HotelResponse update(
                        Long id,
                        HotelRequest request) {

                Hotel hotel = hotelRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Hotel not found with id : " + id));

                Long currentLocationId = hotel.getLocation() != null
                                ? hotel.getLocation().getId()
                                : null;

                if (!request.getLocationId().equals(currentLocationId)) {

                        Location location = locationRepository.findById(
                                        request.getLocationId()).orElseThrow(
                                                        () -> new ResourceNotFoundException(
                                                                        "Location not found with id : "
                                                                                        + request.getLocationId()));

                        hotel.setLocation(location);
                }

                hotel.setHotelName(request.getHotelName());
                hotel.setDescription(request.getDescription());
                hotel.setAddress(request.getAddress());
                hotel.setPricePerNight(request.getPricePerNight());
                hotel.setHotelWeight(request.getHotelWeight());
                hotel.setLatitude(request.getLatitude());
                hotel.setLongitude(request.getLongitude());
                hotel.setStarRating(request.getStarRating());
                hotel.setTotalRooms(request.getTotalRooms());
                hotel.setCheckInTime(request.getCheckInTime());
                hotel.setCheckOutTime(request.getCheckOutTime());
                hotel.setContactNumber(request.getContactNumber());
                hotel.setWebsiteUrl(request.getWebsiteUrl());
                hotel.setActive(request.getActive());

                /*
                 * Amenity/image synchronization depends on the existing
                 * HotelMapper/entity relationship implementation.
                 * Keep your existing mapper/relationship logic for those
                 * collections if already implemented in the project.
                 */

                return hotelMapper.toResponse(hotel);
        }

        // ============================================================
        // GET BY ID
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public HotelResponse getById(Long id) {

                Hotel hotel = hotelRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Hotel not found with id : " + id));

                return hotelMapper.toResponse(hotel);
        }

        // ============================================================
        // GET ALL
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public Page<HotelResponse> getAll(
                        Pageable pageable) {

                return hotelRepository.findAll(pageable)
                                .map(hotelMapper::toResponse);
        }

        // ============================================================
        // GET BY LOCATION
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public Page<HotelResponse> getByLocation(
                        Long locationId,
                        Pageable pageable) {

                return hotelRepository
                                .findByLocationOptimized(
                                                locationId,
                                                pageable)
                                .map(hotelMapper::toResponse);
        }

        // ============================================================
        // DELETE
        // ============================================================

        @Override
        public void delete(@NonNull Long id) {

                if (!hotelRepository.existsById(id)) {

                        throw new ResourceNotFoundException(
                                        "Hotel not found with id : " + id);
                }

                hotelRepository.deleteById(id);
        }

        // ============================================================
        // CSV IMPORT
        //
        // locationId comes from EACH CSV ROW.
        //
        // CSV:
        // locationId,hotelName,description,...
        //
        // There is NO locationId in the URL.
        // ============================================================

        @Override
        public HotelCsvImportResponse importCsv(
                        MultipartFile file) {

                validateCsvFile(file);

                HotelCsvImportResponse result = HotelCsvImportResponse.builder()
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

                        validateRequiredCsvHeaders(parser);

                        /*
                         * locationId -> Location
                         */
                        Map<Long, Location> locationCache = new HashMap<>();

                        /*
                         * locationId -> normalized hotelName -> Hotel
                         */
                        Map<Long, Map<String, Hotel>> existingCache = new HashMap<>();

                        List<Hotel> newHotels = new ArrayList<>();

                        List<Hotel> updatedHotels = new ArrayList<>();

                        /*
                         * Prevent duplicate rows in the same CSV.
                         *
                         * locationId + hotelName
                         */
                        Set<String> csvHotelKeys = new HashSet<>();

                        // --------------------------------------------------------
                        // READ EVERY CSV ROW
                        // --------------------------------------------------------

                        for (CSVRecord record : parser) {

                                result.setTotalRows(
                                                result.getTotalRows() + 1);

                                try {

                                        // ------------------------------------------------
                                        // 1. READ LOCATION ID
                                        // ------------------------------------------------

                                        Long locationId = parseLocationId(record);

                                        // ------------------------------------------------
                                        // 2. GET LOCATION FROM CACHE / DATABASE
                                        // ------------------------------------------------

                                        Location location = locationCache.get(locationId);

                                        if (location == null) {

                                                location = locationRepository.findById(
                                                                locationId)
                                                                .orElseThrow(() -> new ResourceNotFoundException(
                                                                                "Location not found with id : "
                                                                                                + locationId));

                                                locationCache.put(
                                                                locationId,
                                                                location);
                                        }

                                        // ------------------------------------------------
                                        // 3. LOAD EXISTING HOTELS FOR THIS LOCATION
                                        // ONLY ON FIRST USE
                                        // ------------------------------------------------

                                        Map<String, Hotel> existingMap = existingCache.get(locationId);

                                        if (existingMap == null) {

                                                existingMap = loadExistingHotels(
                                                                locationId);

                                                existingCache.put(
                                                                locationId,
                                                                existingMap);
                                        }

                                        // ------------------------------------------------
                                        // 4. PARSE HOTEL
                                        // ------------------------------------------------

                                        Hotel imported = parseHotel(
                                                        record,
                                                        location);

                                        if (imported == null) {

                                                result.setSkipped(
                                                                result.getSkipped() + 1);

                                                continue;
                                        }

                                        String normalizedName = normalizeName(
                                                        imported.getHotelName());

                                        String uniqueKey = buildHotelKey(
                                                        locationId,
                                                        normalizedName);

                                        // ------------------------------------------------
                                        // 5. DUPLICATE INSIDE SAME CSV
                                        // ------------------------------------------------

                                        if (!csvHotelKeys.add(uniqueKey)) {

                                                result.setSkipped(
                                                                result.getSkipped() + 1);

                                                result.getErrors().add(
                                                                "Row "
                                                                                + record.getRecordNumber()
                                                                                + ": Duplicate hotel '"
                                                                                + imported.getHotelName()
                                                                                + "' for locationId "
                                                                                + locationId
                                                                                + " skipped.");

                                                continue;
                                        }

                                        // ------------------------------------------------
                                        // 6. UPDATE OR INSERT
                                        // ------------------------------------------------

                                        Hotel existing = existingMap.get(
                                                        normalizedName);

                                        if (existing != null) {

                                                updateFromCsv(
                                                                existing,
                                                                imported);

                                                updatedHotels.add(
                                                                existing);

                                                result.setUpdated(
                                                                result.getUpdated() + 1);

                                        } else {

                                                newHotels.add(imported);

                                                /*
                                                 * Add to cache immediately so if another
                                                 * duplicate row appears later, it will not
                                                 * become another INSERT.
                                                 */
                                                existingMap.put(
                                                                normalizedName,
                                                                imported);

                                                result.setInserted(
                                                                result.getInserted() + 1);
                                        }

                                } catch (Exception e) {

                                        result.setFailed(
                                                        result.getFailed() + 1);

                                        String message = e.getMessage();

                                        if (message == null ||
                                                        message.isBlank()) {

                                                message = e.getClass()
                                                                .getSimpleName();
                                        }

                                        result.getErrors().add(
                                                        "Row "
                                                                        + record.getRecordNumber()
                                                                        + ": "
                                                                        + message);
                                }
                        }

                        // --------------------------------------------------------
                        // BULK INSERT
                        // --------------------------------------------------------

                        if (!newHotels.isEmpty()) {

                                hotelRepository.saveAll(
                                                newHotels);
                        }

                        // --------------------------------------------------------
                        // BULK UPDATE
                        // --------------------------------------------------------

                        if (!updatedHotels.isEmpty()) {

                                hotelRepository.saveAll(
                                                updatedHotels);
                        }

                } catch (IllegalArgumentException e) {

                        throw e;

                } catch (Exception e) {

                        throw new IllegalArgumentException(
                                        "Unable to process hotel CSV: "
                                                        + e.getMessage(),
                                        e);
                }

                return result;
        }

        // ============================================================
        // LOAD EXISTING HOTELS
        // ============================================================

        private Map<String, Hotel> loadExistingHotels(
                        Long locationId) {

                Map<String, Hotel> existingMap = new HashMap<>();

                Page<Hotel> page = hotelRepository.findByLocationOptimized(
                                locationId,
                                Pageable.unpaged());

                for (Hotel hotel : page.getContent()) {

                        String normalizedName = normalizeName(
                                        hotel.getHotelName());

                        if (!normalizedName.isBlank()) {

                                existingMap.put(
                                                normalizedName,
                                                hotel);
                        }
                }

                return existingMap;
        }

        // ============================================================
        // PARSE HOTEL
        // ============================================================

        private Hotel parseHotel(
                        CSVRecord record,
                        Location location) {

                if (location == null) {

                        throw new IllegalArgumentException(
                                        "Location is required for CSV row.");
                }

                String hotelName = getValue(
                                record,
                                "hotelName");

                if (!hasText(hotelName)) {
                        return null;
                }

                BigDecimal pricePerNight = parseBigDecimal(
                                getValue(
                                                record,
                                                "pricePerNight"));

                Integer hotelWeight = parseInteger(
                                getValue(
                                                record,
                                                "hotelWeight"));

                BigDecimal latitude = parseBigDecimal(
                                getValue(
                                                record,
                                                "latitude"));

                BigDecimal longitude = parseBigDecimal(
                                getValue(
                                                record,
                                                "longitude"));

                Integer starRating = parseInteger(
                                getValue(
                                                record,
                                                "starRating"));

                Integer totalRooms = parseInteger(
                                getValue(
                                                record,
                                                "totalRooms"));

                LocalTime checkInTime = parseLocalTime(
                                getValue(
                                                record,
                                                "checkInTime"));

                LocalTime checkOutTime = parseLocalTime(
                                getValue(
                                                record,
                                                "checkOutTime"));

                Boolean active = parseBoolean(
                                getValue(
                                                record,
                                                "active"));

                String contactNumber = cleanPhone(
                                getValue(
                                                record,
                                                "contactNumber"));

                String websiteUrl = cleanWebsite(
                                getValue(
                                                record,
                                                "websiteUrl"));

                /*
                 * Defaults match HotelRequest:
                 *
                 * pricePerNight -> 0
                 * hotelWeight -> 1
                 * active -> true
                 */
                if (pricePerNight == null) {
                        pricePerNight = BigDecimal.ZERO;
                }

                if (hotelWeight == null) {
                        hotelWeight = 1;
                }

                if (active == null) {
                        active = true;
                }

                Hotel hotel = Hotel.builder()
                                .location(location)
                                .hotelName(
                                                cleanText(hotelName))
                                .description(
                                                cleanText(
                                                                getValue(
                                                                                record,
                                                                                "description")))
                                .address(
                                                cleanText(
                                                                getValue(
                                                                                record,
                                                                                "address")))
                                .pricePerNight(
                                                pricePerNight)
                                .hotelWeight(
                                                hotelWeight)
                                .latitude(latitude)
                                .longitude(longitude)
                                .starRating(starRating)
                                .totalRooms(totalRooms)
                                .checkInTime(checkInTime)
                                .checkOutTime(checkOutTime)
                                .contactNumber(contactNumber)
                                .websiteUrl(websiteUrl)
                                .active(active)
                                .build();

                return hotel;
        }

        // ============================================================
        // UPDATE FROM CSV
        // ============================================================

        private void updateFromCsv(
                        Hotel current,
                        Hotel imported) {

                if (hasText(
                                imported.getHotelName())) {

                        current.setHotelName(
                                        imported.getHotelName());
                }

                if (hasText(
                                imported.getDescription())) {

                        current.setDescription(
                                        imported.getDescription());
                }

                if (hasText(
                                imported.getAddress())) {

                        current.setAddress(
                                        imported.getAddress());
                }

                if (imported.getPricePerNight() != null) {

                        current.setPricePerNight(
                                        imported.getPricePerNight());
                }

                if (imported.getHotelWeight() != null) {

                        current.setHotelWeight(
                                        imported.getHotelWeight());
                }

                if (imported.getLatitude() != null) {

                        current.setLatitude(
                                        imported.getLatitude());
                }

                if (imported.getLongitude() != null) {

                        current.setLongitude(
                                        imported.getLongitude());
                }

                if (imported.getStarRating() != null) {

                        current.setStarRating(
                                        imported.getStarRating());
                }

                if (imported.getTotalRooms() != null) {

                        current.setTotalRooms(
                                        imported.getTotalRooms());
                }

                if (imported.getCheckInTime() != null) {

                        current.setCheckInTime(
                                        imported.getCheckInTime());
                }

                if (imported.getCheckOutTime() != null) {

                        current.setCheckOutTime(
                                        imported.getCheckOutTime());
                }

                if (hasText(
                                imported.getContactNumber())) {

                        current.setContactNumber(
                                        imported.getContactNumber());
                }

                if (hasText(
                                imported.getWebsiteUrl())) {

                        current.setWebsiteUrl(
                                        imported.getWebsiteUrl());
                }

                if (imported.getActive() != null) {

                        current.setActive(
                                        imported.getActive());
                }
        }

        // ============================================================
        // LOCATION ID
        // ============================================================

        private Long parseLocationId(
                        CSVRecord record) {

                String value = getValue(
                                record,
                                "locationId");

                if (!hasText(value)) {

                        throw new IllegalArgumentException(
                                        "locationId is required.");
                }

                try {

                        Long locationId = Long.parseLong(
                                        value.trim());

                        if (locationId <= 0) {

                                throw new IllegalArgumentException(
                                                "locationId must be greater than 0.");
                        }

                        return locationId;

                } catch (NumberFormatException e) {

                        throw new IllegalArgumentException(
                                        "Invalid locationId: "
                                                        + value);
                }
        }

        // ============================================================
        // CSV HEADERS
        // ============================================================

        private void validateRequiredCsvHeaders(
                        CSVParser parser) {

                Set<String> headers = parser.getHeaderMap().keySet();

                if (!headers.contains("locationId")) {

                        throw new IllegalArgumentException(
                                        "CSV header 'locationId' is required.");
                }

                if (!headers.contains("hotelName")) {

                        throw new IllegalArgumentException(
                                        "CSV header 'hotelName' is required.");
                }
        }

        // ============================================================
        // CSV VALUE
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

        // ============================================================
        // BUILD UNIQUE KEY
        // ============================================================

        private String buildHotelKey(
                        Long locationId,
                        String normalizedName) {

                return locationId
                                + ":"
                                + normalizedName;
        }

        // ============================================================
        // BIG DECIMAL
        // ============================================================

        private BigDecimal parseBigDecimal(
                        String value) {

                if (!hasText(value)) {
                        return null;
                }

                try {

                        return new BigDecimal(
                                        value.trim());

                } catch (NumberFormatException e) {

                        return null;
                }
        }

        // ============================================================
        // INTEGER
        // ============================================================

        private Integer parseInteger(
                        String value) {

                if (!hasText(value)) {
                        return null;
                }

                try {

                        return Integer.parseInt(
                                        value.trim());

                } catch (NumberFormatException e) {

                        return null;
                }
        }

        // ============================================================
        // BOOLEAN
        // ============================================================

        private Boolean parseBoolean(
                        String value) {

                if (!hasText(value)) {
                        return null;
                }

                String normalized = value.trim()
                                .toLowerCase(
                                                Locale.ENGLISH);

                if ("true".equals(normalized) ||
                                "1".equals(normalized) ||
                                "yes".equals(normalized)) {

                        return true;
                }

                if ("false".equals(normalized) ||
                                "0".equals(normalized) ||
                                "no".equals(normalized)) {

                        return false;
                }

                return null;
        }

        // ============================================================
        // TIME
        // ============================================================

        private LocalTime parseLocalTime(
                        String value) {

                if (!hasText(value)) {
                        return null;
                }

                String cleaned = value.trim();

                List<DateTimeFormatter> formatters = List.of(
                                DateTimeFormatter.ofPattern(
                                                "HH:mm"),
                                DateTimeFormatter.ofPattern(
                                                "H:mm"),
                                DateTimeFormatter.ofPattern(
                                                "hh:mm a",
                                                Locale.ENGLISH),
                                DateTimeFormatter.ofPattern(
                                                "h:mm a",
                                                Locale.ENGLISH));

                for (DateTimeFormatter formatter : formatters) {

                        try {

                                return LocalTime.parse(
                                                cleaned,
                                                formatter);

                        } catch (DateTimeParseException ignored) {
                                // Try next format.
                        }
                }

                return null;
        }

        // ============================================================
        // PHONE
        // ============================================================

        private String cleanPhone(
                        String value) {

                if (!hasText(value)) {
                        return null;
                }

                String phone = value.replaceAll(
                                "[^0-9+()\\- ]",
                                "").trim();

                return phone.isBlank()
                                ? null
                                : phone;
        }

        // ============================================================
        // WEBSITE
        // ============================================================

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

        // ============================================================
        // TEXT
        // ============================================================

        private String cleanText(
                        String value) {

                if (value == null) {
                        return null;
                }

                String cleaned = value.replace(
                                "\uFEFF",
                                "")
                                .replaceAll(
                                                "\\s+",
                                                " ")
                                .trim();

                if (cleaned.isBlank() ||
                                cleaned.equalsIgnoreCase("null") ||
                                cleaned.equalsIgnoreCase("n/a") ||
                                cleaned.equals("-")) {

                        return null;
                }

                return cleaned;
        }

        // ============================================================
        // NORMALIZE NAME
        // ============================================================

        private String normalizeName(
                        String name) {

                if (name == null) {
                        return "";
                }

                return name
                                .toLowerCase(Locale.ENGLISH)
                                .replaceAll(
                                                "[^a-z0-9]",
                                                "");
        }

        // ============================================================
        // HAS TEXT
        // ============================================================

        private boolean hasText(
                        String value) {

                return value != null &&
                                !value.trim().isEmpty();
        }

        // ============================================================
        // CSV VALIDATION
        // ============================================================

        private void validateCsvFile(
                        MultipartFile file) {

                if (file == null ||
                                file.isEmpty()) {

                        throw new IllegalArgumentException(
                                        "CSV file is required.");
                }

                String filename = file.getOriginalFilename();

                if (filename == null ||
                                !filename.toLowerCase(
                                                Locale.ENGLISH).endsWith(".csv")) {

                        throw new IllegalArgumentException(
                                        "Only CSV files are allowed.");
                }
        }

        // ============================================================
        // COUNT
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public long countAll() {

                return hotelRepository.count();
        }

        @Override
        @Transactional(readOnly = true)
        public long countByLocation(
                        Long locationId) {

                return hotelRepository.countByLocationId(
                                locationId);
        }
}
