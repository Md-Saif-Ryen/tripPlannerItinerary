package com.example.tripItinerary.Service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.DTO.request.RestaurantReviewRequest;
import com.example.tripItinerary.DTO.response.RestaurantReviewResponse;
import com.example.tripItinerary.Entity.Restaurant;
import com.example.tripItinerary.Entity.RestaurantReview;
import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.Mapper.RestaurantReviewMapper;
import com.example.tripItinerary.Repo.RestaurantRepository;
import com.example.tripItinerary.Repo.RestaurantReviewRepository;
import com.example.tripItinerary.Repo.UserRepository;
import com.example.tripItinerary.Service.RestaurantReviewService;
import com.example.tripItinerary.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantReviewServiceImpl
        implements RestaurantReviewService {

    private final RestaurantReviewRepository restaurantReviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final RestaurantReviewMapper restaurantReviewMapper;

    @Override
    public RestaurantReviewResponse create(
            RestaurantReviewRequest request) {

        Restaurant restaurant = restaurantRepository
                .findById(request.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with id : "
                                + request.getRestaurantId()));

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id : "
                                + request.getUserId()));

        RestaurantReview review = restaurantReviewMapper.toEntity(request);

        review.setRestaurant(restaurant);
        review.setUser(user);

        RestaurantReview savedReview = restaurantReviewRepository.save(review);

        updateRestaurantRating(restaurant);

        return restaurantReviewMapper.toResponse(savedReview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantReviewResponse> getByRestaurant(
            Long restaurantId) {

        if (!restaurantRepository.existsById(restaurantId)) {
            throw new ResourceNotFoundException(
                    "Restaurant not found with id : " + restaurantId);
        }

        return restaurantReviewRepository
                .findByRestaurantId(restaurantId)
                .stream()
                .map(restaurantReviewMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {

        RestaurantReview review = restaurantReviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant review not found with id : "
                                + id));

        Restaurant restaurant = review.getRestaurant();

        restaurantReviewRepository.delete(review);

        updateRestaurantRating(restaurant);
    }

    private void updateRestaurantRating(Restaurant restaurant) {

        List<RestaurantReview> reviews = restaurantReviewRepository
                .findByRestaurantId(restaurant.getId());

        if (reviews.isEmpty()) {
            restaurant.setAverageRating(BigDecimal.ZERO);
        } else {

            BigDecimal total = reviews.stream()
                    .map(review -> BigDecimal.valueOf(review.getRating()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal average = total.divide(
                    BigDecimal.valueOf(reviews.size()),
                    2,
                    RoundingMode.HALF_UP);

            restaurant.setAverageRating(average);
        }

        restaurantRepository.save(restaurant);
    }
}