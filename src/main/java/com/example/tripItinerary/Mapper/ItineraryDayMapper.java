package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.ItineraryDay;
import com.example.tripItinerary.DTO.request.ItineraryDayRequest;
import com.example.tripItinerary.DTO.response.ItineraryDayResponse;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;

@Mapper(config = MapperConfiguration.class, uses = {
        ItineraryPlaceMapper.class
})
public interface ItineraryDayMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itinerary", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "itineraryPlaces", ignore = true)
    ItineraryDay toEntity(ItineraryDayRequest request);

    ItineraryDayResponse toResponse(ItineraryDay entity);

}