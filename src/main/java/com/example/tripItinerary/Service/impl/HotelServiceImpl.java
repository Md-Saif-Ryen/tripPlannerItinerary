// package com.example.tripItinerary.Service.impl;

// import java.util.ArrayList;
// import java.util.Collections;
// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.LinkedHashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
// import java.util.function.Function;
// import java.util.stream.Collectors;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.example.tripItinerary.DTO.request.HotelRequest;
// import com.example.tripItinerary.DTO.response.HotelResponse;
// import com.example.tripItinerary.Entity.Amenity;
// import com.example.tripItinerary.Entity.Hotel;
// import com.example.tripItinerary.Entity.Location;
// import com.example.tripItinerary.Mapper.HotelMapper;
// import com.example.tripItinerary.Repo.AmenityRepository;
// import com.example.tripItinerary.Repo.HotelRepository;
// import com.example.tripItinerary.Repo.LocationRepository;
// import com.example.tripItinerary.Service.HotelService;
// import com.example.tripItinerary.exception.ResourceNotFoundException;

// import lombok.RequiredArgsConstructor;

// @Service
// @RequiredArgsConstructor
// @Transactional
// public class HotelServiceImpl implements HotelService {

//     private final HotelRepository hotelRepository;
//     private final LocationRepository locationRepository;
//     private final AmenityRepository amenityRepository;
//     private final HotelMapper hotelMapper;

//     // ============================================================
//     // CREATE SINGLE
//     // ============================================================

//     @Override
//     public HotelResponse create(HotelRequest request) {

//         if (request == null) {
//             throw new IllegalArgumentException("Hotel request cannot be null");
//         }

//         // Fetch location only once
//         Location location = locationRepository.findById(request.getLocationId())
//                 .orElseThrow(() -> new ResourceNotFoundException(
//                         "Location not found with id : " + request.getLocationId()));

//         Hotel hotel = hotelMapper.toEntity(request);

//         hotel.setLocation(location);

//         // Fetch amenities in one query
//         if (request.getAmenityIds() != null && !request.getAmenityIds().isEmpty()) {

//             List<Long> amenityIds = request.getAmenityIds()
//                     .stream()
//                     .filter(id -> id != null && id > 0)
//                     .distinct()
//                     .toList();

//             if (!amenityIds.isEmpty()) {
//                 List<Amenity> amenities = amenityRepository.findAllById(amenityIds);

//                 hotel.setAmenities(amenities);
//             } else {
//                 hotel.setAmenities(new ArrayList<>());
//             }

//         } else {
//             hotel.setAmenities(new ArrayList<>());
//         }

//         Hotel savedHotel = hotelRepository.save(hotel);

//         return hotelMapper.toResponse(savedHotel);
//     }

//     // ============================================================
//     // BULK CREATE - OPTIMIZED
//     // ============================================================

//     @Override
//     public List<HotelResponse> bulkCreate(List<HotelRequest> requests) {

//         if (requests == null || requests.isEmpty()) {
//             return Collections.emptyList();
//         }

//         /*
//          * --------------------------------------------------------
//          * STEP 1
//          * Collect all location IDs
//          *
//          * Instead of:
//          *
//          * request 1 -> DB
//          * request 2 -> DB
//          * request 3 -> DB
//          *
//          * We do:
//          *
//          * all locations -> ONE DB query
//          * --------------------------------------------------------
//          */

//         Set<Long> locationIds = requests.stream()
//                 .map(HotelRequest::getLocationId)
//                 .filter(id -> id != null && id > 0)
//                 .collect(Collectors.toSet());

//         if (locationIds.isEmpty()) {
//             throw new IllegalArgumentException(
//                     "At least one valid location ID is required");
//         }

//         List<Location> locations = locationRepository.findAllById(locationIds);

//         Map<Long, Location> locationMap = locations.stream()
//                 .collect(Collectors.toMap(
//                         Location::getId,
//                         Function.identity()));

//         /*
//          * Validate all locations before creating entities.
//          */

//         for (Long locationId : locationIds) {

//             if (!locationMap.containsKey(locationId)) {
//                 throw new ResourceNotFoundException(
//                         "Location not found with id : " + locationId);
//             }
//         }

//         /*
//          * --------------------------------------------------------
//          * STEP 2
//          * Collect ALL amenity IDs from ALL hotels.
//          *
//          * Again, only ONE DB query.
//          * --------------------------------------------------------
//          */

//         Set<Long> amenityIds = requests.stream()
//                 .filter(request -> request.getAmenityIds() != null)
//                 .flatMap(request -> request.getAmenityIds().stream())
//                 .filter(id -> id != null && id > 0)
//                 .collect(Collectors.toSet());

//         Map<Long, Amenity> amenityMap = new HashMap<>();

//         if (!amenityIds.isEmpty()) {

//             List<Amenity> amenities = amenityRepository.findAllById(amenityIds);

//             amenityMap = amenities.stream()
//                     .collect(Collectors.toMap(
//                             Amenity::getId,
//                             Function.identity()));
//         }

//         /*
//          * --------------------------------------------------------
//          * STEP 3
//          * Convert requests -> entities in memory.
//          * No DB calls here.
//          * --------------------------------------------------------
//          */

//         List<Hotel> hotels = new ArrayList<>(requests.size());

//         for (HotelRequest request : requests) {

//             if (request == null) {
//                 continue;
//             }

//             Location location = locationMap.get(
//                     request.getLocationId());

//             if (location == null) {
//                 throw new ResourceNotFoundException(
//                         "Location not found with id : "
//                                 + request.getLocationId());
//             }

//             Hotel hotel = hotelMapper.toEntity(request);

//             hotel.setLocation(location);

//             /*
//              * Build amenity list from already fetched map.
//              */

//             if (request.getAmenityIds() != null
//                     && !request.getAmenityIds().isEmpty()) {

//                 List<Amenity> hotelAmenities = request.getAmenityIds()
//                         .stream()
//                         .filter(id -> id != null && id > 0)
//                         .distinct()
//                         .map(amenityMap::get)
//                         .filter(amenity -> amenity != null)
//                         .collect(Collectors.toCollection(
//                                 ArrayList::new));

//                 hotel.setAmenities(hotelAmenities);

//             } else {
//                 hotel.setAmenities(new ArrayList<>());
//             }

//             hotels.add(hotel);
//         }

//         if (hotels.isEmpty()) {
//             return Collections.emptyList();
//         }

//         /*
//          * --------------------------------------------------------
//          * STEP 4
//          * ONE bulk save operation.
//          *
//          * Hibernate JDBC batching will split this internally
//          * according to batch_size.
//          * --------------------------------------------------------
//          */

//         List<Hotel> savedHotels = hotelRepository.saveAll(hotels);

//         /*
//          * --------------------------------------------------------
//          * STEP 5
//          * Convert entities -> response.
//          * --------------------------------------------------------
//          */

//         return savedHotels.stream()
//                 .map(hotelMapper::toResponse)
//                 .toList();
//     }

//     // ============================================================
//     // UPDATE
//     // ============================================================

//     @Override
//     public HotelResponse update(Long id, HotelRequest request) {

//         if (request == null) {
//             throw new IllegalArgumentException(
//                     "Hotel request cannot be null");
//         }

//         Hotel hotel = hotelRepository.findById(id)
//                 .orElseThrow(() -> new ResourceNotFoundException(
//                         "Hotel not found with id : " + id));

//         /*
//          * Fetch location.
//          */

//         Location location = locationRepository.findById(
//                 request.getLocationId()).orElseThrow(
//                         () -> new ResourceNotFoundException(
//                                 "Location not found with id : "
//                                         + request.getLocationId()));

//         hotel.setLocation(location);

//         /*
//          * Update fields.
//          */

//         hotel.setHotelName(request.getHotelName());
//         hotel.setDescription(request.getDescription());
//         hotel.setAddress(request.getAddress());
//         hotel.setPricePerNight(request.getPricePerNight());
//         hotel.setHotelWeight(request.getHotelWeight());
//         hotel.setLatitude(request.getLatitude());
//         hotel.setLongitude(request.getLongitude());
//         hotel.setStarRating(request.getStarRating());
//         hotel.setTotalRooms(request.getTotalRooms());
//         hotel.setCheckInTime(request.getCheckInTime());
//         hotel.setCheckOutTime(request.getCheckOutTime());
//         hotel.setContactNumber(request.getContactNumber());
//         hotel.setWebsiteUrl(request.getWebsiteUrl());
//         hotel.setActive(request.getActive());

//         /*
//          * Amenities.
//          */

//         if (request.getAmenityIds() != null) {

//             List<Long> amenityIds = request.getAmenityIds()
//                     .stream()
//                     .filter(amenityId -> amenityId != null && amenityId > 0)
//                     .distinct()
//                     .toList();

//             List<Amenity> amenities = amenityIds.isEmpty()
//                     ? new ArrayList<>()
//                     : amenityRepository.findAllById(amenityIds);

//             hotel.setAmenities(amenities);
//         }

//         /*
//          * IMPORTANT:
//          *
//          * No hotelRepository.save(hotel) needed here.
//          *
//          * hotel is already a managed entity because it came from
//          * findById() and we're inside @Transactional.
//          *
//          * Hibernate dirty checking automatically generates UPDATE.
//          */

//         return hotelMapper.toResponse(hotel);
//     }

//     // ============================================================
//     // GET BY ID
//     // ============================================================

//     @Override
//     @Transactional(readOnly = true)
//     public HotelResponse getById(Long id) {

//         Hotel hotel = hotelRepository.findByIdWithDetails(id)
//                 .orElseThrow(() -> new ResourceNotFoundException(
//                         "Hotel not found with id : " + id));

//         return hotelMapper.toResponse(hotel);
//     }

//     // ============================================================
//     // GET ALL
//     // ============================================================

//     @Override
//     @Transactional(readOnly = true)
//     public List<HotelResponse> getAll() {

//         List<Hotel> hotels = hotelRepository.findAllWithDetails();

//         if (hotels.isEmpty()) {
//             return Collections.emptyList();
//         }

//         return hotels.stream()
//                 .map(hotelMapper::toResponse)
//                 .toList();
//     }

//     // ============================================================
//     // GET BY LOCATION
//     // ============================================================

//     @Override
//     @Transactional(readOnly = true)
//     public List<HotelResponse> getByLocation(Long locationId) {

//         if (locationId == null || locationId <= 0) {
//             throw new IllegalArgumentException(
//                     "Invalid location ID");
//         }

//         List<Hotel> hotels = hotelRepository.findAllByLocationIdWithDetails(
//                 locationId);

//         if (hotels.isEmpty()) {
//             return Collections.emptyList();
//         }

//         return hotels.stream()
//                 .map(hotelMapper::toResponse)
//                 .toList();
//     }

//     // ============================================================
//     // DELETE
//     // ============================================================

//     @Override
//     public void delete(Long id) {

//         if (id == null || id <= 0) {
//             throw new IllegalArgumentException(
//                     "Invalid hotel ID");
//         }

//         /*
//          * If you don't need a custom "not found" message,
//          * deleteById() is cheaper than findById() + delete().
//          *
//          * But if you need your current exception behavior,
//          * keep existence check.
//          */

//         if (!hotelRepository.existsById(id)) {
//             throw new ResourceNotFoundException(
//                     "Hotel not found with id : " + id);
//         }

//         hotelRepository.deleteById(id);
//     }
// }




package com.example.tripItinerary.Service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.HotelRequest;
import com.example.tripItinerary.DTO.response.HotelResponse;
import com.example.tripItinerary.Entity.Amenity;
import com.example.tripItinerary.Entity.Hotel;
import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Mapper.HotelMapper;
import com.example.tripItinerary.Repo.AmenityRepository;
import com.example.tripItinerary.Repo.HotelRepository;
import com.example.tripItinerary.Repo.LocationRepository;
import com.example.tripItinerary.Service.HotelService;
import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final LocationRepository locationRepository;
    private final AmenityRepository amenityRepository;
    private final HotelMapper hotelMapper;

    // =========================================================
    // CREATE
    // =========================================================

    @Override
    public HotelResponse create(HotelRequest request) {

        validateRequest(request);

        Location location = locationRepository.findById(
                request.getLocationId()).orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Location not found with id : "
                                        + request.getLocationId()));

        Hotel hotel = hotelMapper.toEntity(request);

        hotel.setLocation(location);

        List<Amenity> amenities = resolveAmenities(request.getAmenityIds());

        hotel.setAmenities(amenities);

        Hotel saved = hotelRepository.save(hotel);

        return hotelMapper.toResponse(saved);
    }

    // =========================================================
    // BULK CREATE
    // =========================================================

    @Override
    public List<HotelResponse> bulkCreate(List<HotelRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        // ---------------------------------------------------------
        // STEP 1: Validate requests first
        // ---------------------------------------------------------

        for (HotelRequest request : requests) {
            validateRequest(request);
        }

        // ---------------------------------------------------------
        // STEP 2: Collect location IDs
        // ---------------------------------------------------------

        Set<Long> locationIds = requests.stream()
                .map(HotelRequest::getLocationId)
                .collect(Collectors.toSet());

        // ---------------------------------------------------------
        // STEP 3: Fetch all locations in ONE query
        // ---------------------------------------------------------

        Map<Long, Location> locationMap = locationRepository
                .findAllById(locationIds)
                .stream()
                .collect(Collectors.toMap(
                        Location::getId,
                        Function.identity()));

        if (locationMap.size() != locationIds.size()) {

            Long missingLocationId = locationIds.stream()
                    .filter(id -> !locationMap.containsKey(id))
                    .findFirst()
                    .orElse(null);

            throw new ResourceNotFoundException(
                    "Location not found with id : " + missingLocationId);
        }

        // ---------------------------------------------------------
        // STEP 4: Collect all amenity IDs
        // ---------------------------------------------------------

        Set<Long> amenityIds = requests.stream()
                .map(HotelRequest::getAmenityIds)
                .filter(ids -> ids != null && !ids.isEmpty())
                .flatMap(List::stream)
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toSet());

        // IMPORTANT:
        // Declare once and never reassign.
        // This fixes the "effectively final" error.
        final Map<Long, Amenity> amenityMap;

        if (amenityIds.isEmpty()) {

            amenityMap = Collections.emptyMap();

        } else {

            List<Amenity> amenities = amenityRepository.findAllById(amenityIds);

            amenityMap = amenities.stream()
                    .collect(Collectors.toMap(
                            Amenity::getId,
                            Function.identity()));

            if (amenityMap.size() != amenityIds.size()) {

                Long missingAmenityId = amenityIds.stream()
                        .filter(id -> !amenityMap.containsKey(id))
                        .findFirst()
                        .orElse(null);

                throw new ResourceNotFoundException(
                        "Amenity not found with id : " + missingAmenityId);
            }
        }

        // ---------------------------------------------------------
        // STEP 5: Build hotels completely in memory
        // ---------------------------------------------------------

        List<Hotel> hotels = new ArrayList<>(requests.size());

        for (HotelRequest request : requests) {

            Hotel hotel = hotelMapper.toEntity(request);

            // Location already loaded
            hotel.setLocation(
                    locationMap.get(request.getLocationId()));

            // Resolve amenities from memory
            List<Amenity> hotelAmenities = new ArrayList<>();

            if (request.getAmenityIds() != null) {

                for (Long amenityId : request.getAmenityIds().stream()
                        .filter(id -> id != null && id > 0)
                        .distinct()
                        .toList()) {

                    Amenity amenity = amenityMap.get(amenityId);

                    if (amenity != null) {
                        hotelAmenities.add(amenity);
                    }
                }
            }

            hotel.setAmenities(hotelAmenities);

            hotels.add(hotel);
        }

        // ---------------------------------------------------------
        // STEP 6: ONE bulk save
        // ---------------------------------------------------------

        List<Hotel> savedHotels = hotelRepository.saveAll(hotels);

        // ---------------------------------------------------------
        // STEP 7: Map response
        // ---------------------------------------------------------

        return savedHotels.stream()
                .map(hotelMapper::toResponse)
                .toList();
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @Override
    public HotelResponse update(
            Long id,
            HotelRequest request) {

        validateRequest(request);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel not found with id : " + id));

        /*
         * Location.
         */

        Location location = locationRepository.findById(
                request.getLocationId()).orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Location not found with id : "
                                        + request.getLocationId()));

        /*
         * Update basic fields.
         */

        hotel.setLocation(location);

        hotel.setHotelName(
                request.getHotelName());

        hotel.setDescription(
                request.getDescription());

        hotel.setAddress(
                request.getAddress());

        hotel.setPricePerNight(
                request.getPricePerNight());

        hotel.setHotelWeight(
                request.getHotelWeight());

        hotel.setLatitude(
                request.getLatitude());

        hotel.setLongitude(
                request.getLongitude());

        hotel.setStarRating(
                request.getStarRating());

        hotel.setTotalRooms(
                request.getTotalRooms());

        hotel.setCheckInTime(
                request.getCheckInTime());

        hotel.setCheckOutTime(
                request.getCheckOutTime());

        hotel.setContactNumber(
                request.getContactNumber());

        hotel.setWebsiteUrl(
                request.getWebsiteUrl());

        hotel.setActive(
                request.getActive());

        /*
         * Amenities.
         */

        if (request.getAmenityIds() != null) {

            hotel.setAmenities(
                    resolveAmenities(
                            request.getAmenityIds()));
        }

        /*
         * IMPORTANT:
         *
         * No save() required.
         *
         * Entity is managed inside @Transactional.
         *
         * Hibernate dirty checking will execute UPDATE.
         */

        return hotelMapper.toResponse(hotel);
    }

    // =========================================================
    // GET BY ID
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public HotelResponse getById(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Invalid hotel ID.");
        }

        Hotel hotel = hotelRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel not found with id : "
                                + id));

        return hotelMapper.toResponse(hotel);
    }

    // =========================================================
    // GET ALL - PAGEABLE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponse> getAll(
            Pageable pageable) {

        return hotelRepository
                .findAllOptimized(pageable)
                .map(hotelMapper::toResponse);
    }

    // =========================================================
    // GET BY LOCATION - PAGEABLE
    // =========================================================

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponse> getByLocation(
            Long locationId,
            Pageable pageable) {

        if (locationId == null || locationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid location ID.");
        }

        return hotelRepository
                .findByLocationOptimized(
                        locationId,
                        pageable)
                .map(hotelMapper::toResponse);
    }

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    public void delete(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "Invalid hotel ID.");
        }

        /*
         * One operation instead of:
         *
         * existsById()
         * +
         * deleteById()
         *
         */

        if (!hotelRepository.existsById(id)) {
            throw new ResourceNotFoundException(
                    "Hotel not found with id : " + id);
        }

        hotelRepository.deleteById(id);
    }

    // =========================================================
    // AMENITIES HELPER
    // =========================================================

    private List<Amenity> resolveAmenities(
            List<Long> amenityIds) {

        if (amenityIds == null || amenityIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> ids = amenityIds.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .toList();

        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        List<Amenity> amenities = amenityRepository.findAllById(ids);

        /*
         * Make sure invalid IDs don't silently disappear.
         */

        if (amenities.size() != ids.size()) {

            Set<Long> foundIds = amenities.stream()
                    .map(Amenity::getId)
                    .collect(Collectors.toSet());

            Long missingId = ids.stream()
                    .filter(id -> !foundIds.contains(id))
                    .findFirst()
                    .orElse(null);

            throw new ResourceNotFoundException(
                    "Amenity not found with id : " + missingId);
        }

        return amenities;
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateRequest(
            HotelRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Hotel request cannot be null.");
        }

        if (request.getLocationId() == null
                || request.getLocationId() <= 0) {

            throw new IllegalArgumentException(
                    "Valid location ID is required.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
            return hotelRepository.countAllHotels();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByLocation(Long locationId) {
            return hotelRepository.countByLocationId(locationId);
    }

  
}


