package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.TouristPlaceReview;
import com.example.tripItinerary.DTO.request.TouristPlaceReviewRequest;
import com.example.tripItinerary.DTO.response.TouristPlaceReviewResponse;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface TouristPlaceReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "touristPlace", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    TouristPlaceReview toEntity(TouristPlaceReviewRequest request);

    @Mapping(source = "touristPlace.id", target = "touristPlaceId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    TouristPlaceReviewResponse toResponse(TouristPlaceReview entity);

}