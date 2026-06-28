package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.Itinerary;
import com.example.tripItinerary.DTO.request.ItineraryRequest;
import com.example.tripItinerary.DTO.response.ItineraryResponse;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;

@Mapper(config = MapperConfiguration.class, uses = {
        ItineraryDayMapper.class
})
public interface ItineraryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "itineraryStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "itineraryDays", ignore = true)
    Itinerary toEntity(ItineraryRequest request);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "location.id", target = "locationId")
    ItineraryResponse toResponse(Itinerary entity);

}