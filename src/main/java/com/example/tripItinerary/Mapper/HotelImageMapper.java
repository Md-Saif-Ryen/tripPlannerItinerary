package com.example.tripItinerary.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.tripItinerary.Entity.HotelImage;
import com.example.tripItinerary.DTO.request.HotelImageRequest;
import com.example.tripItinerary.DTO.response.HotelImageResponse;
import com.example.tripItinerary.Mapper.config.MapperConfiguration;

@Mapper(config = MapperConfiguration.class)
public interface HotelImageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hotel", ignore = true)
    HotelImage toEntity(HotelImageRequest request);

    HotelImageResponse toResponse(HotelImage entity);

}