package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.TouristPlace;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;
import com.example.tripItinerary.DTO.request.TouristPlaceRequest;
import com.example.tripItinerary.DTO.response.TouristPlaceResponse;

@Mapper(config = MapperConfiguration.class, uses = {
        TouristPlaceImageMapper.class,
        TouristPlaceReviewMapper.class
})
public interface TouristPlaceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    TouristPlace toEntity(TouristPlaceRequest request);

    @Mapping(source = "location.id", target = "locationId")
    TouristPlaceResponse toResponse(TouristPlace entity);

}