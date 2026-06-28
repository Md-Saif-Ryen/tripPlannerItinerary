package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.ItineraryPlace;
import com.example.tripItinerary.DTO.request.ItineraryPlaceRequest;
import com.example.tripItinerary.DTO.response.ItineraryPlaceResponse;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface ItineraryPlaceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itineraryDay", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ItineraryPlace toEntity(ItineraryPlaceRequest request);

    ItineraryPlaceResponse toResponse(ItineraryPlace entity);

}