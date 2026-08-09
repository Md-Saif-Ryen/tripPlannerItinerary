package com.example.tripItinerary.Service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.TouristPlaceRequest;
import com.example.tripItinerary.DTO.response.TouristPlaceResponse;
import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Entity.TouristPlace;
import com.example.tripItinerary.Mapper.TouristPlaceMapper;
import com.example.tripItinerary.Repo.LocationRepository;
import com.example.tripItinerary.Repo.TouristPlaceRepository;
import com.example.tripItinerary.Service.TouristPlaceService;
import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TouristPlaceServiceImpl implements TouristPlaceService {

    private final TouristPlaceRepository touristPlaceRepository;
    private final LocationRepository locationRepository;
    private final TouristPlaceMapper touristPlaceMapper;

    @Override
    public TouristPlaceResponse create(TouristPlaceRequest request) {

        @SuppressWarnings("null")
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id : " + request.getLocationId()));

        TouristPlace touristPlace = touristPlaceMapper.toEntity(request);

        touristPlace.setLocation(location);

        touristPlace = touristPlaceRepository.save(touristPlace);

        return touristPlaceMapper.toResponse(touristPlace);
    }

    @Override
    public TouristPlaceResponse update(@NonNull Long id, TouristPlaceRequest request) {

        TouristPlace touristPlace = touristPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place not found with id : " + id));

        @SuppressWarnings("null")
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Location not found with id : " + request.getLocationId()));

        touristPlace.setLocation(location);
        touristPlace.setPlaceName(request.getPlaceName());
        touristPlace.setDescription(request.getDescription());
        touristPlace.setPrice(request.getPrice());
        touristPlace.setPlaceWeight(request.getPlaceWeight());
        touristPlace.setLatitude(request.getLatitude());
        touristPlace.setLongitude(request.getLongitude());
        touristPlace.setPopularityScore(request.getPopularityScore());
        touristPlace.setBestVisitMonths(request.getBestVisitMonths());
        touristPlace.setGooglePlaceId(request.getGooglePlaceId());
        touristPlace.setAddress(request.getAddress());
        touristPlace.setContactNumber(request.getContactNumber());
        touristPlace.setWebsiteUrl(request.getWebsiteUrl());
        touristPlace.setEstimatedVisitTimeMinutes(request.getEstimatedVisitTimeMinutes());
        touristPlace.setCategory(request.getCategory());
        touristPlace.setOpeningTime(request.getOpeningTime());
        touristPlace.setClosingTime(request.getClosingTime());
        touristPlace.setActive(request.getActive());

        touristPlace = touristPlaceRepository.save(touristPlace);

        return touristPlaceMapper.toResponse(touristPlace);
    }

    @Override
    @Transactional(readOnly = true)
    public TouristPlaceResponse getById(Long id) {

        System.out.println("Fetching tourist place with ID: " + id);
        TouristPlace touristPlace = touristPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place not found with id : " + id));

        return touristPlaceMapper.toResponse(touristPlace);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TouristPlaceResponse> getAll() {

        return touristPlaceRepository.findAll()
                .stream()
                .map(touristPlaceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TouristPlaceResponse> getByLocation(Long locationId) {

        return touristPlaceRepository.findByLocationId(locationId)
                .stream()
                .map(touristPlaceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Override
    public void delete(@NonNull Long id) {

        TouristPlace touristPlace = touristPlaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place not found with id : " + id));

        touristPlaceRepository.delete(touristPlace);
    }

}