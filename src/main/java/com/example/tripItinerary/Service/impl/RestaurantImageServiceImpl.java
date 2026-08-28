package com.example.tripItinerary.Service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.RestaurantImageRequest;
import com.example.tripItinerary.DTO.response.RestaurantImageResponse;
import com.example.tripItinerary.Entity.Restaurant;
import com.example.tripItinerary.Entity.RestaurantImage;
import com.example.tripItinerary.Mapper.RestaurantImageMapper;
import com.example.tripItinerary.Repo.RestaurantImageRepository;
import com.example.tripItinerary.Repo.RestaurantRepository;
import com.example.tripItinerary.Service.RestaurantImageService;
import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantImageServiceImpl
        implements RestaurantImageService {

    private final RestaurantImageRepository restaurantImageRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantImageMapper restaurantImageMapper;

    // ============================================================
    // CREATE SINGLE IMAGE
    // ============================================================

    @Override
    public RestaurantImageResponse create(
            RestaurantImageRequest request) {

        Restaurant restaurant = restaurantRepository
                .findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id : "
                                + request.getRestaurantId()));

        // If this image is primary,
        // remove primary status from existing image
        if (Boolean.TRUE.equals(request.getPrimary())) {
            removeExistingPrimary(restaurant.getId());
        }

        RestaurantImage image = restaurantImageMapper.toEntity(request);

        image.setRestaurant(restaurant);

        RestaurantImage savedImage = restaurantImageRepository.save(image);

        return restaurantImageMapper.toResponse(savedImage);
    }

    // ============================================================
    // CREATE BULK IMAGES
    // ============================================================

    @Override
    public List<RestaurantImageResponse> createBulk(
            List<RestaurantImageRequest> requests) {

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
    public RestaurantImageResponse getById(Long id) {

        RestaurantImage image = restaurantImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant image not found with id : "
                                + id));

        return restaurantImageMapper.toResponse(image);
    }

    // ============================================================
    // GET ALL IMAGES BY RESTAURANT
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantImageResponse> getByRestaurantId(
            Long restaurantId) {

        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException(
                    "Restaurant not found with id : "
                            + restaurantId);
        }

        return restaurantImageRepository
                .findByRestaurantId(restaurantId)
                .stream()
                .map(restaurantImageMapper::toResponse)
                .toList();
    }

    // ============================================================
    // UPDATE IMAGE
    // ============================================================

    @Override
    public RestaurantImageResponse update(
            Long id,
            RestaurantImageRequest request) {

        RestaurantImage image = restaurantImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant image not found with id : "
                                + id));

        // If changing this image to primary
        if (Boolean.TRUE.equals(request.getPrimary())) {
            removeExistingPrimary(
                    image.getRestaurant().getId());
        }

        image.setImageUrl(request.getImageUrl());
        image.setPrimary(
                Boolean.TRUE.equals(request.getPrimary()));

        RestaurantImage updatedImage = restaurantImageRepository.save(image);

        return restaurantImageMapper.toResponse(updatedImage);
    }

    // ============================================================
    // SET PRIMARY IMAGE
    // ============================================================

    @Override
    public RestaurantImageResponse setPrimary(Long id) {

        RestaurantImage image = restaurantImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant image not found with id : "
                                + id));

        Long restaurantId = image.getRestaurant().getId();

        // Remove primary from all existing images
        removeExistingPrimary(restaurantId);

        // Set selected image as primary
        image.setPrimary(true);

        RestaurantImage savedImage = restaurantImageRepository.save(image);

        return restaurantImageMapper.toResponse(savedImage);
    }

    // ============================================================
    // DELETE IMAGE
    // ============================================================

    @Override
    public void delete(Long id) {

        RestaurantImage image = restaurantImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant image not found with id : "
                                + id));

        restaurantImageRepository.delete(image);
    }

    // ============================================================
    // REMOVE EXISTING PRIMARY IMAGE
    // ============================================================

    private void removeExistingPrimary(
            Long restaurantId) {

        List<RestaurantImage> images = restaurantImageRepository
                .findByRestaurantId(restaurantId);

        for (RestaurantImage image : images) {

            if (Boolean.TRUE.equals(image.getPrimary())) {
                image.setPrimary(false);
            }
        }

        restaurantImageRepository.saveAll(images);
    }
}