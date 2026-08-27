package com.example.tripItinerary.Service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.HotelImageRequest;
import com.example.tripItinerary.DTO.response.HotelImageResponse;
import com.example.tripItinerary.Entity.Hotel;
import com.example.tripItinerary.Entity.HotelImage;
import com.example.tripItinerary.Mapper.HotelImageMapper;
import com.example.tripItinerary.Repo.HotelImageRepository;
import com.example.tripItinerary.Repo.HotelRepository;
import com.example.tripItinerary.Service.hotelImageService;
import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelImageServiceImpl
                implements hotelImageService {

        private final HotelImageRepository imageRepository;
        private final HotelRepository hotelRepository;
        private final HotelImageMapper imageMapper;

        // ============================================================
        // CREATE
        // ============================================================

        @Override
        public HotelImageResponse create(
                        HotelImageRequest request) {

                Hotel hotel = hotelRepository.findById(
                                request.getHotelId()).orElseThrow(
                                                () -> new ResourceNotFoundException(
                                                                "Hotel not found with id : "
                                                                                + request.getHotelId()));

                HotelImage image = imageMapper.toEntity(request);

                image.setHotel(hotel);

                // --------------------------------------------------------
                // If this is the first image, make it primary automatically
                // --------------------------------------------------------

                List<HotelImage> existing = imageRepository.findByHotelId(
                                hotel.getId());

                if (existing.isEmpty()) {
                        image.setPrimary(true);
                } else if (Boolean.TRUE.equals(
                                request.getPrimary())) {

                        removePrimary(
                                        existing);
                }

                image = imageRepository.save(image);

                return imageMapper.toResponse(image);
        }

        @Override
        public List<HotelImageResponse> createBulk(
                        List<HotelImageRequest> requests) {

                if (requests == null || requests.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "At least one image is required.");
                }

                Long hotelId = requests.get(0).getHotelId();

                if (hotelId == null) {
                        throw new IllegalArgumentException(
                                        "Hotel id is required.");
                }

                // ============================================================
                // ENSURE ALL IMAGES BELONG TO SAME HOTEL
                // ============================================================

                boolean differentHotel = requests.stream()
                                .anyMatch(request -> !hotelId.equals(
                                                request.getHotelId()));

                if (differentHotel) {
                        throw new IllegalArgumentException(
                                        "All images must belong to the same hotel.");
                }

                // ============================================================
                // ONLY ONE PRIMARY IMAGE
                // ============================================================

                long primaryCount = requests.stream()
                                .filter(request -> Boolean.TRUE.equals(
                                                request.getPrimary()))
                                .count();

                if (primaryCount > 1) {
                        throw new IllegalArgumentException(
                                        "Only one primary image is allowed.");
                }

                // ============================================================
                // FIND HOTEL
                // ============================================================

                Hotel hotel = hotelRepository.findById(hotelId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Hotel not found with id : "
                                                                + hotelId));

                // ============================================================
                // CLEAR EXISTING PRIMARY
                // ============================================================

                boolean hasPrimary = primaryCount == 1;

                if (hasPrimary) {
                        imageRepository.clearPrimaryByHotelId(
                                        hotelId);
                }

                // ============================================================
                // CREATE IMAGES
                // ============================================================

                List<HotelImage> images = requests.stream()
                                .map(request -> {

                                        HotelImage image = imageMapper.toEntity(
                                                        request);

                                        image.setHotel(hotel);

                                        image.setImageUrl(
                                                        request.getImageUrl()
                                                                        .trim());

                                        image.setPrimary(
                                                        Boolean.TRUE.equals(
                                                                        request.getPrimary()));

                                        return image;
                                })
                                .toList();

                // ============================================================
                // SAVE ALL
                // ============================================================

                return imageRepository
                                .saveAll(images)
                                .stream()
                                .map(imageMapper::toResponse)
                                .toList();
        }
        // ============================================================
        // GET BY ID
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public HotelImageResponse getById(
                        Long id) {

                HotelImage image = imageRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Hotel image not found with id : "
                                                                + id));

                return imageMapper.toResponse(image);
        }

        // ============================================================
        // GET BY HOTEL
        // ============================================================

        @Override
        @Transactional(readOnly = true)
        public List<HotelImageResponse> getByHotelId(
                        Long hotelId) {

                if (!hotelRepository.existsById(hotelId)) {
                        throw new ResourceNotFoundException(
                                        "Hotel not found with id : "
                                                        + hotelId);
                }

                return imageRepository
                                .findByHotelId(hotelId)
                                .stream()
                                .map(imageMapper::toResponse)
                                .toList();
        }

        // ============================================================
        // UPDATE
        // ============================================================

        @Override
        public HotelImageResponse update(
                        Long id,
                        HotelImageRequest request) {

                HotelImage image = imageRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Hotel image not found with id : "
                                                                + id));

                Hotel hotel = image.getHotel();

                // --------------------------------------------------------
                // Update hotel only if changed
                // --------------------------------------------------------

                if (request.getHotelId() != null &&
                                !request.getHotelId()
                                                .equals(hotel.getId())) {

                        hotel = hotelRepository.findById(
                                        request.getHotelId()).orElseThrow(
                                                        () -> new ResourceNotFoundException(
                                                                        "Hotel not found with id : "
                                                                                        + request.getHotelId()));

                        image.setHotel(hotel);
                }

                image.setImageUrl(
                                request.getImageUrl().trim());

                if (Boolean.TRUE.equals(
                                request.getPrimary())) {

                        removePrimary(
                                        imageRepository.findByHotelId(
                                                        hotel.getId()));

                        image.setPrimary(true);

                } else {

                        image.setPrimary(false);
                }

                image = imageRepository.save(image);

                return imageMapper.toResponse(image);
        }

        // ============================================================
        // SET PRIMARY
        // ============================================================

        @Override
        public HotelImageResponse setPrimary(
                        Long id) {

                HotelImage image = imageRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Hotel image not found with id : "
                                                                + id));

                List<HotelImage> images = imageRepository.findByHotelId(
                                image.getHotel().getId());

                removePrimary(images);

                image.setPrimary(true);

                image = imageRepository.save(image);

                return imageMapper.toResponse(image);
        }

        // ============================================================
        // DELETE
        // ============================================================

        @Override
        public void delete(Long id) {

                HotelImage image = imageRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Hotel image not found with id : "
                                                                + id));

                Hotel hotel = image.getHotel();

                boolean wasPrimary = Boolean.TRUE.equals(
                                image.getPrimary());

                imageRepository.delete(image);

                // --------------------------------------------------------
                // If primary image deleted, promote another image
                // --------------------------------------------------------

                if (wasPrimary) {

                        List<HotelImage> remaining = imageRepository.findByHotelId(
                                        hotel.getId());

                        if (!remaining.isEmpty()) {

                                HotelImage newPrimary = remaining.get(0);

                                newPrimary.setPrimary(true);

                                imageRepository.save(
                                                newPrimary);
                        }
                }
        }

        // ============================================================
        // REMOVE PRIMARY
        // ============================================================

        private void removePrimary(
                        List<HotelImage> images) {

                for (HotelImage item : images) {

                        if (Boolean.TRUE.equals(
                                        item.getPrimary())) {

                                item.setPrimary(false);
                        }
                }

                if (!images.isEmpty()) {
                        imageRepository.saveAll(images);
                }
        }
}