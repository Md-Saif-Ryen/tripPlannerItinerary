package com.example.tripItinerary.Service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.LocationRequest;
import com.example.tripItinerary.DTO.response.LocationResponse;
import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Mapper.LocationMapper;
import com.example.tripItinerary.Repo.LocationRepository;
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

    @SuppressWarnings("null")
    @Override
    public LocationResponse create(LocationRequest request) {

        Location location = locationMapper.toEntity(request);

        location = locationRepository.save(location);

        return locationMapper.toResponse(location);
    }

    @Override
    public LocationResponse update(Long id, LocationRequest request) {

        @SuppressWarnings("null")
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id : " + id));

        location.setStateName(request.getStateName());
        location.setCityName(request.getCityName());
        location.setAddress(request.getAddress());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());

        location = locationRepository.save(location);

        return locationMapper.toResponse(location);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getById(Long id) {

        @SuppressWarnings("null")
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id : " + id));

        return locationMapper.toResponse(location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getAll() {

        return locationRepository.findAll()
                .stream()
                .map(locationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Override
    public void delete(@NonNull Long id) {

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id : " + id));

        locationRepository.delete(location);
    }

}