package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

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
    @Mapping(target = "remainingBudget", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "itineraryDays", ignore = true)
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    Itinerary toEntity(ItineraryRequest request);

    // ✅ FIX: Add locationName mapping
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "location.id", target = "locationId")
    @Mapping(source = "location", target = "locationName", qualifiedByName = "getLocationName")
    @Mapping(source = "itineraryStatus", target = "itineraryStatus")
    @Mapping(source = "remainingBudget", target = "remainingBudget")
    ItineraryResponse toResponse(Itinerary entity);

    // ✅ Custom method for location name
    @Named("getLocationName")
    default String getLocationName(com.example.tripItinerary.Entity.Location location) {
        if (location == null) {
            return null;
        }
        String city = location.getCityName();
        String state = location.getStateName();
        if (city != null && state != null) {
            return city + ", " + state;
        }
        return city != null ? city : state;
    }
}