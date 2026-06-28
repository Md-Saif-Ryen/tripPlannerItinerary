package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.Hotel;
import com.example.tripItinerary.DTO.request.HotelRequest;
import com.example.tripItinerary.DTO.response.HotelResponse;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;

@Mapper(config = MapperConfiguration.class, uses = {
        AmenityMapper.class,
        HotelImageMapper.class,
        HotelReviewMapper.class
})
public interface HotelMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Hotel toEntity(HotelRequest request);

    @Mapping(source = "location.id", target = "locationId")
    HotelResponse toResponse(Hotel entity);

}