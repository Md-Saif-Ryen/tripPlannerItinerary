package com.example.tripItinerary.Service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.HotelReviewRequest;
import com.example.tripItinerary.DTO.response.HotelReviewResponse;
import com.example.tripItinerary.Entity.Hotel;
import com.example.tripItinerary.Entity.HotelReview;
import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.Mapper.HotelReviewMapper;
import com.example.tripItinerary.Repo.HotelRepository;
import com.example.tripItinerary.Repo.HotelReviewRepository;
import com.example.tripItinerary.Repo.UserRepository;
import com.example.tripItinerary.Service.HotelReviewService;
import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelReviewServiceImpl
        implements HotelReviewService {

    private final HotelReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final HotelReviewMapper reviewMapper;

    // ============================================================
    // CREATE
    // ============================================================

    @Override
    public HotelReviewResponse create(
            HotelReviewRequest request) {

        Hotel hotel = hotelRepository.findById(
                request.getHotelId()).orElseThrow(
                        () -> new ResourceNotFoundException(
                                "Hotel not found with id : "
                                        + request.getHotelId()));

        User user = userRepository.findById(
                request.getUserId()).orElseThrow(
                        () -> new ResourceNotFoundException(
                                "User not found with id : "
                                        + request.getUserId()));

        HotelReview review = HotelReview.builder()
                .hotel(hotel)
                .user(user)
                .rating(request.getRating())
                .reviewText(
                        request.getReviewText())
                .build();

        review = reviewRepository.save(review);

        updateAverageRating(hotel);

        return reviewMapper.toResponse(review);
    }

    // ============================================================
    // GET BY ID
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public HotelReviewResponse getById(
            Long id) {

        HotelReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found with id : "
                                + id));

        return reviewMapper.toResponse(review);
    }

    // ============================================================
    // GET HOTEL REVIEWS
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<HotelReviewResponse> getByHotelId(
            Long hotelId) {

        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException(
                    "Hotel not found with id : "
                            + hotelId);
        }

        return reviewRepository
                .findByHotelIdOrderByCreatedAtDesc(
                        hotelId)
                .stream()
                .map(reviewMapper::toResponse)
                .toList();
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @Override
    public HotelReviewResponse update(
            Long id,
            HotelReviewRequest request) {

        HotelReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found with id : "
                                + id));

        Hotel hotel = review.getHotel();

        review.setRating(
                request.getRating());

        review.setReviewText(
                request.getReviewText());

        review = reviewRepository.save(review);

        updateAverageRating(hotel);

        return reviewMapper.toResponse(review);
    }

    // ============================================================
    // DELETE
    // ============================================================

    @Override
    public void delete(Long id) {

        HotelReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Review not found with id : "
                                + id));

        Hotel hotel = review.getHotel();

        reviewRepository.delete(review);

        updateAverageRating(hotel);
    }

    // ============================================================
    // UPDATE HOTEL AVERAGE
    // ============================================================

    private void updateAverageRating(
            Hotel hotel) {

        Double average = reviewRepository
                .findAverageRatingByHotelId(
                        hotel.getId());

        if (average == null) {

            hotel.setAverageRating(
                    BigDecimal.ZERO);

        } else {

            hotel.setAverageRating(
                    BigDecimal.valueOf(
                            average));
        }

        hotelRepository.save(hotel);
    }
}