package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;

import com.example.tripItinerary.Entity.Amenity;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;
import com.example.tripItinerary.DTO.request.AmenityRequest;
import com.example.tripItinerary.DTO.response.AmenityResponse;

@Mapper(config = MapperConfiguration.class)
public interface AmenityMapper {

    Amenity toEntity(AmenityRequest request);

    AmenityResponse toResponse(Amenity entity);

}