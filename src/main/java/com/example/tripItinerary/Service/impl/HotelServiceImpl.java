package com.example.tripItinerary.Service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final LocationRepository locationRepository;
    private final AmenityRepository amenityRepository;
    private final HotelMapper hotelMapper;

    @Override
    public HotelResponse create(HotelRequest request) {

        @SuppressWarnings("null")
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id : " + request.getLocationId()));

        Hotel hotel = hotelMapper.toEntity(request);

        hotel.setLocation(location);

        if (request.getAmenityIds() != null && !request.getAmenityIds().isEmpty()) {

            @SuppressWarnings("null")
            List<Amenity> amenities = amenityRepository.findAllById(request.getAmenityIds());

            hotel.setAmenities(amenities);

        } else {

            hotel.setAmenities(Collections.emptyList());

        }

        hotel = hotelRepository.save(hotel);

        return hotelMapper.toResponse(hotel);
    }

    @Override
    public HotelResponse update(Long id, HotelRequest request) {

        @SuppressWarnings("null")
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel not found with id : " + id));

        @SuppressWarnings("null")
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id : " + request.getLocationId()));

        hotel.setLocation(location);
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
        
        if (request.getAmenityIds() != null) {

            @SuppressWarnings("null")
            List<Amenity> amenities = amenityRepository.findAllById(request.getAmenityIds());

            hotel.setAmenities(amenities);
        }

        hotel = hotelRepository.save(hotel);

        return hotelMapper.toResponse(hotel);
    }

     @Override
    @Transactional(readOnly = true)
    public HotelResponse getById(Long id) {

        @SuppressWarnings("null")
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel not found with id : " + id));

        return hotelMapper.toResponse(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponse> getAll() {

        return hotelRepository.findAll()
                .stream()
                .map(hotelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponse> getByLocation(Long locationId) {

        return hotelRepository.findByLocationId(locationId)
                .stream()
                .map(hotelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Override
    public void delete(@NonNull Long id) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Hotel not found with id : " + id));

        hotelRepository.delete(hotel);
    }
}