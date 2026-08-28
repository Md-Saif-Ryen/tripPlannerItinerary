package com.example.tripItinerary.Service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.TouristPlaceImageRequest;
import com.example.tripItinerary.DTO.response.TouristPlaceImageResponse;
import com.example.tripItinerary.Entity.TouristPlace;
import com.example.tripItinerary.Entity.TouristPlaceImage;
import com.example.tripItinerary.Mapper.TouristPlaceImageMapper;
import com.example.tripItinerary.Repo.TouristPlaceImageRepository;
import com.example.tripItinerary.Repo.TouristPlaceRepository;
import com.example.tripItinerary.Service.TouristPlaceImageService;
import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TouristPlaceImageServiceImpl
        implements TouristPlaceImageService {

    private final TouristPlaceImageRepository touristPlaceImageRepository;

    private final TouristPlaceRepository touristPlaceRepository;

    private final TouristPlaceImageMapper touristPlaceImageMapper;

    // ============================================================
    // CREATE SINGLE IMAGE
    // ============================================================

    @Override
    public TouristPlaceImageResponse create(
            TouristPlaceImageRequest request) {

        TouristPlace touristPlace = touristPlaceRepository
                .findById(request.getTouristPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place not found with id : "
                                + request.getTouristPlaceId()));

        // If new image is primary,
        // remove primary from existing image
        if (Boolean.TRUE.equals(request.getPrimary())) {

            removeExistingPrimary(
                    touristPlace.getId());
        }

        TouristPlaceImage image = touristPlaceImageMapper.toEntity(request);

        image.setTouristPlace(touristPlace);

        TouristPlaceImage savedImage = touristPlaceImageRepository.save(image);

        return touristPlaceImageMapper.toResponse(
                savedImage);
    }

    // ============================================================
    // CREATE BULK IMAGES
    // ============================================================

    @Override
    public List<TouristPlaceImageResponse> createBulk(
            List<TouristPlaceImageRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            return List.of();
        }

        return requests.stream()
                .map(this::create)
                .toList();
    }

    // ============================================================
    // GET IMAGE BY ID
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public TouristPlaceImageResponse getById(Long id) {

        TouristPlaceImage image = touristPlaceImageRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place image not found with id : "
                                + id));

        return touristPlaceImageMapper.toResponse(image);
    }

    // ============================================================
    // GET ALL IMAGES BY TOURIST PLACE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<TouristPlaceImageResponse> getByTouristPlaceId(
            Long touristPlaceId) {

        if (!touristPlaceRepository.existsById(
                touristPlaceId)) {

            throw new ResourceNotFoundException(
                    "Tourist place not found with id : "
                            + touristPlaceId);
        }

        return touristPlaceImageRepository
                .findByTouristPlaceId(touristPlaceId)
                .stream()
                .map(touristPlaceImageMapper::toResponse)
                .toList();
    }

    // ============================================================
    // UPDATE IMAGE
    // ============================================================

    @Override
    public TouristPlaceImageResponse update(
            Long id,
            TouristPlaceImageRequest request) {

        TouristPlaceImage image = touristPlaceImageRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place image not found with id : "
                                + id));

        /*
         * Ensure that the requested tourist place
         * exists.
         */
        TouristPlace touristPlace = touristPlaceRepository
                .findById(request.getTouristPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place not found with id : "
                                + request.getTouristPlaceId()));

        /*
         * If tourist place is changed,
         * image will be associated with new place.
         */
        image.setTouristPlace(touristPlace);

        // If image is being made primary
        if (Boolean.TRUE.equals(request.getPrimary())) {

            removeExistingPrimary(
                    touristPlace.getId());
        }

        image.setImageUrl(
                request.getImageUrl());

        image.setPrimary(
                Boolean.TRUE.equals(
                        request.getPrimary()));

        TouristPlaceImage updatedImage = touristPlaceImageRepository
                .save(image);

        return touristPlaceImageMapper.toResponse(
                updatedImage);
    }

    // ============================================================
    // SET PRIMARY IMAGE
    // ============================================================

    @Override
    public TouristPlaceImageResponse setPrimary(
            Long id) {

        TouristPlaceImage image = touristPlaceImageRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place image not found with id : "
                                + id));

        Long touristPlaceId = image.getTouristPlace().getId();

        // Remove primary from all other images
        removeExistingPrimary(
                touristPlaceId);

        // Set selected image as primary
        image.setPrimary(true);

        TouristPlaceImage savedImage = touristPlaceImageRepository.save(image);

        return touristPlaceImageMapper.toResponse(
                savedImage);
    }

    // ============================================================
    // DELETE IMAGE
    // ============================================================

    @Override
    public void delete(Long id) {

        TouristPlaceImage image = touristPlaceImageRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place image not found with id : "
                                + id));

        touristPlaceImageRepository.delete(image);
    }

    // ============================================================
    // REMOVE EXISTING PRIMARY IMAGE
    // ============================================================

    private void removeExistingPrimary(
            Long touristPlaceId) {

        List<TouristPlaceImage> images = touristPlaceImageRepository
                .findByTouristPlaceId(
                        touristPlaceId);

        for (TouristPlaceImage image : images) {

            if (Boolean.TRUE.equals(
                    image.getPrimary())) {

                image.setPrimary(false);
            }
        }

        touristPlaceImageRepository.saveAll(images);
    }
}