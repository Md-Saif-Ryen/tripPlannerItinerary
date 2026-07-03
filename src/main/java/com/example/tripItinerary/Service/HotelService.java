package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.HotelRequest;
import com.example.tripItinerary.DTO.response.HotelResponse;

public interface HotelService {

    HotelResponse create(HotelRequest request);

    HotelResponse update(Long id, HotelRequest request);

    HotelResponse getById(Long id);

    List<HotelResponse> getAll();

    List<HotelResponse> getByLocation(Long locationId);

    void delete(Long id);

}