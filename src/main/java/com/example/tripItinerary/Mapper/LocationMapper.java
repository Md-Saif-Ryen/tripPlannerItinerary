package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.DTO.request.LocationRequest;
import com.example.tripItinerary.DTO.response.LocationResponse;
import com.example.tripItinerary.Entity.Location;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface LocationMapper {

    // ============================================================
    // REQUEST -> ENTITY
    // ============================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)

    @Mapping(target = "touristPlaces", ignore = true)
    @Mapping(target = "restaurants", ignore = true)
    @Mapping(target = "hotels", ignore = true)
    @Mapping(target = "itineraries", ignore = true)

    Location toEntity(LocationRequest request);

    // ============================================================
    // ENTITY -> RESPONSE
    // ============================================================

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.cityName")

    LocationResponse toResponse(Location entity);
}