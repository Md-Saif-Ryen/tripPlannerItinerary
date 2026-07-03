package com.example.tripItinerary.Service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.RestaurantRequest;
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

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final LocationRepository locationRepository;
    private final RestaurantMapper restaurantMapper;

    @Override
    public RestaurantResponse create(RestaurantRequest request) {

        @SuppressWarnings("null")
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id : " + request.getLocationId()));

        Restaurant restaurant = restaurantMapper.toEntity(request);

        restaurant.setLocation(location);

        restaurant = restaurantRepository.save(restaurant);

        return restaurantMapper.toResponse(restaurant);
    }

    @Override
    public RestaurantResponse update(Long id, RestaurantRequest request) {

        @SuppressWarnings("null")
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id : " + id));

        @SuppressWarnings("null")
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id : " + request.getLocationId()));

        restaurant.setLocation(location);
        restaurant.setRestaurantName(request.getRestaurantName());
        restaurant.setDescription(request.getDescription());
        restaurant.setAddress(request.getAddress());
        restaurant.setAverageCostPerPerson(request.getAverageCostPerPerson());
        restaurant.setRestaurantWeight(request.getRestaurantWeight());
        restaurant.setLatitude(request.getLatitude());
        restaurant.setLongitude(request.getLongitude());
        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setOpeningTime(request.getOpeningTime());
        restaurant.setClosingTime(request.getClosingTime());
        restaurant.setVeg(request.getVeg());
        restaurant.setActive(request.getActive());
        restaurant.setContactNumber(request.getContactNumber());
        restaurant.setWebsiteUrl(request.getWebsiteUrl());

        restaurant = restaurantRepository.save(restaurant);

        return restaurantMapper.toResponse(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getById(Long id) {

        @SuppressWarnings("null")
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id : " + id));

        return restaurantMapper.toResponse(restaurant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAll() {

        return restaurantRepository.findAll()
                .stream()
                .map(restaurantMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getByLocation(Long locationId) {

        return restaurantRepository.findByLocationId(locationId)
                .stream()
                .map(restaurantMapper::toResponse)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Override
    public void delete(@NonNull Long id) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id : " + id));

        restaurantRepository.delete(restaurant);
    }

}