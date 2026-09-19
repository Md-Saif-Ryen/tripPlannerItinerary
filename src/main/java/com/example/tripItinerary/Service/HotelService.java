// package com.example.tripItinerary.Service;

// import java.util.List;

// import com.example.tripItinerary.DTO.request.HotelRequest;
// import com.example.tripItinerary.DTO.response.HotelResponse;

// public interface HotelService {

//     HotelResponse create(HotelRequest request);

//     List<HotelResponse> bulkCreate(List<HotelRequest> requests);

//     HotelResponse update(Long id, HotelRequest request);

//     HotelResponse getById(Long id);

//     List<HotelResponse> getAll();

//     List<HotelResponse> getByLocation(Long locationId);

//     void delete(Long id);
// }

package com.example.tripItinerary.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.tripItinerary.DTO.request.HotelRequest;
import com.example.tripItinerary.DTO.response.HotelResponse;

import java.util.List;

public interface HotelService {

    HotelResponse create(HotelRequest request);

    List<HotelResponse> bulkCreate(List<HotelRequest> requests);

    HotelResponse update(Long id, HotelRequest request);

    HotelResponse getById(Long id);

    Page<HotelResponse> getAll(Pageable pageable);

    Page<HotelResponse> getByLocation(Long locationId, Pageable pageable);

    void delete(Long id);
}