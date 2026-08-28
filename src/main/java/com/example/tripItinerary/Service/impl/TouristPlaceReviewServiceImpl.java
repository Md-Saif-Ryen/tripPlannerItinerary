package com.example.tripItinerary.Service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.TouristPlaceReviewRequest;
import com.example.tripItinerary.DTO.response.TouristPlaceReviewResponse;
import com.example.tripItinerary.Entity.TouristPlace;
import com.example.tripItinerary.Entity.TouristPlaceReview;
import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.Mapper.TouristPlaceReviewMapper;
import com.example.tripItinerary.Repo.TouristPlaceRepository;
import com.example.tripItinerary.Repo.TouristPlaceReviewRepository;
import com.example.tripItinerary.Repo.UserRepository;
import com.example.tripItinerary.Service.TouristPlaceReviewService;
import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TouristPlaceReviewServiceImpl
        implements TouristPlaceReviewService {

    private final TouristPlaceReviewRepository reviewRepository;

    private final TouristPlaceRepository touristPlaceRepository;

    private final UserRepository userRepository;

    private final TouristPlaceReviewMapper reviewMapper;

    // ============================================================
    // CREATE REVIEW
    // ============================================================

    @Override
    public TouristPlaceReviewResponse create(
            TouristPlaceReviewRequest request) {

        TouristPlace touristPlace = touristPlaceRepository
                .findById(request.getTouristPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place not found with id : "
                                + request.getTouristPlaceId()));

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id : "
                                + request.getUserId()));

        TouristPlaceReview review = reviewMapper.toEntity(request);

        review.setTouristPlace(touristPlace);
        review.setUser(user);

        TouristPlaceReview savedReview = reviewRepository.save(review);

        // Recalculate average rating
        updateAverageRating(touristPlace);

        return reviewMapper.toResponse(savedReview);
    }

    // ============================================================
    // GET REVIEW BY ID
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public TouristPlaceReviewResponse getById(Long id) {

        TouristPlaceReview review = reviewRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place review not found with id : "
                                + id));

        return reviewMapper.toResponse(review);
    }

    // ============================================================
    // GET REVIEWS BY TOURIST PLACE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<TouristPlaceReviewResponse> getByTouristPlaceId(
            Long touristPlaceId) {

        if (!touristPlaceRepository.existsById(
                touristPlaceId)) {

            throw new ResourceNotFoundException(
                    "Tourist place not found with id : "
                            + touristPlaceId);
        }

        return reviewRepository
                .findByTouristPlaceId(touristPlaceId)
                .stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    // ============================================================
    // GET REVIEWS BY USER
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<TouristPlaceReviewResponse> getByUserId(
            Long userId) {

        if (!userRepository.existsById(userId)) {

            throw new ResourceNotFoundException(
                    "User not found with id : " + userId);
        }

        return reviewRepository
                .findByUserId(userId)
                .stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    // ============================================================
    // UPDATE REVIEW
    // ============================================================

    @Override
    public TouristPlaceReviewResponse update(
            Long id,
            TouristPlaceReviewRequest request) {

        TouristPlaceReview review = reviewRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place review not found with id : "
                                + id));

        TouristPlace touristPlace = touristPlaceRepository
                .findById(request.getTouristPlaceId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place not found with id : "
                                + request.getTouristPlaceId()));

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id : "
                                + request.getUserId()));

        review.setTouristPlace(touristPlace);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());

        TouristPlaceReview updatedReview = reviewRepository.save(review);

        // Recalculate rating
        updateAverageRating(touristPlace);

        return reviewMapper.toResponse(updatedReview);
    }

    // ============================================================
    // DELETE REVIEW
    // ============================================================

    @Override
    public void delete(Long id) {

        TouristPlaceReview review = reviewRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tourist place review not found with id : "
                                + id));

        TouristPlace touristPlace = review.getTouristPlace();

        reviewRepository.delete(review);

        // Recalculate after delete
        updateAverageRating(touristPlace);
    }

    // ============================================================
    // UPDATE AVERAGE RATING
    // ============================================================

    private void updateAverageRating(
            TouristPlace touristPlace) {

        List<TouristPlaceReview> reviews = reviewRepository
                .findByTouristPlaceId(
                        touristPlace.getId());

        if (reviews.isEmpty()) {

            touristPlace.setAverageRating(
                    BigDecimal.ZERO);

        } else {

            BigDecimal totalRating = reviews.stream()
                    .map(review -> BigDecimal.valueOf(
                            review.getRating()))
                    .reduce(
                            BigDecimal.ZERO,
                            BigDecimal::add);

            BigDecimal averageRating = totalRating.divide(
                    BigDecimal.valueOf(
                            reviews.size()),
                    2,
                    RoundingMode.HALF_UP);

            touristPlace.setAverageRating(
                    averageRating);
        }

        touristPlaceRepository.save(touristPlace);
    }
}