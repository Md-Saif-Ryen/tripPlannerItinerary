package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.RestaurantImage;
import com.example.tripItinerary.DTO.request.RestaurantImageRequest;
import com.example.tripItinerary.DTO.response.RestaurantImageResponse;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface RestaurantImageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    RestaurantImage toEntity(RestaurantImageRequest request);

    RestaurantImageResponse toResponse(RestaurantImage entity);

}