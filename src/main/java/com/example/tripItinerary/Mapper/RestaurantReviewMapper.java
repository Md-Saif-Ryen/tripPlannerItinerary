package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.RestaurantReview;
import com.example.tripItinerary.DTO.request.RestaurantReviewRequest;
import com.example.tripItinerary.DTO.response.RestaurantReviewResponse;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface RestaurantReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    RestaurantReview toEntity(RestaurantReviewRequest request);

    @Mapping(source = "restaurant.id", target = "restaurantId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    RestaurantReviewResponse toResponse(RestaurantReview entity);

}