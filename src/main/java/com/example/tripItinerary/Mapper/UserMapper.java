package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.User;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;
import com.example.tripItinerary.DTO.request.RegisterRequest;
import com.example.tripItinerary.DTO.request.UserRequest;
import com.example.tripItinerary.DTO.response.UserResponse;

@Mapper(config = MapperConfiguration.class)
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "itineraries", ignore = true)
    @Mapping(target = "touristPlaceReviews", ignore = true)
    @Mapping(target = "restaurantReviews", ignore = true)
    @Mapping(target = "hotelReviews", ignore = true)
    User toEntity(UserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "itineraries", ignore = true)
    @Mapping(target = "touristPlaceReviews", ignore = true)
    @Mapping(target = "restaurantReviews", ignore = true)
    @Mapping(target = "hotelReviews", ignore = true)
    User toEntity(RegisterRequest request);

    UserResponse toResponse(User user);

}