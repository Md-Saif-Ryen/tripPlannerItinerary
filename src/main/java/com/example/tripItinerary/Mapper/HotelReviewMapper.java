package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.HotelReview;
import com.example.tripItinerary.DTO.request.HotelReviewRequest;
import com.example.tripItinerary.DTO.response.HotelReviewResponse;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface HotelReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    HotelReview toEntity(HotelReviewRequest request);

    @Mapping(source = "hotel.id", target = "hotelId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userName")
    HotelReviewResponse toResponse(HotelReview entity);

}