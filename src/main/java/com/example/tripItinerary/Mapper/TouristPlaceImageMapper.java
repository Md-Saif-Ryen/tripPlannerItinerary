package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.TouristPlaceImage;
import com.example.tripItinerary.DTO.request.TouristPlaceImageRequest;
import com.example.tripItinerary.DTO.response.TouristPlaceImageResponse;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface TouristPlaceImageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "touristPlace", ignore = true)
    TouristPlaceImage toEntity(TouristPlaceImageRequest request);

    TouristPlaceImageResponse toResponse(TouristPlaceImage entity);

}