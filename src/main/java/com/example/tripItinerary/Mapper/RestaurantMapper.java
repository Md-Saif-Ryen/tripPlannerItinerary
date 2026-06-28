package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.Restaurant;
import com.example.tripItinerary.DTO.request.RestaurantRequest;
import com.example.tripItinerary.DTO.response.RestaurantResponse;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;

@Mapper(config = MapperConfiguration.class, uses = {
        RestaurantImageMapper.class,
        RestaurantReviewMapper.class
})
public interface RestaurantMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Restaurant toEntity(RestaurantRequest request);

    @Mapping(source = "location.id", target = "locationId")
    RestaurantResponse toResponse(Restaurant entity);

}