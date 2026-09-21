// package com.example.tripItinerary.Service;

// import java.util.List;

// import org.springframework.web.multipart.MultipartFile;

// import com.example.tripItinerary.DTO.request.RestaurantRequest;
// import com.example.tripItinerary.DTO.response.RestaurantCsvImportResponse;
// import com.example.tripItinerary.DTO.response.RestaurantResponse;

// public interface RestaurantService {

//     RestaurantResponse create(RestaurantRequest request);

//     RestaurantResponse update(Long id, RestaurantRequest request);

//     RestaurantResponse getById(Long id);

//     List<RestaurantResponse> getAll();

//     List<RestaurantResponse> getByLocation(Long locationId);

//     void delete(Long id);

//       RestaurantCsvImportResponse importCsv(
//             MultipartFile file,
//             Long locationId
//     );

// }


package com.example.tripItinerary.Service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.example.tripItinerary.DTO.request.RestaurantRequest;
import com.example.tripItinerary.DTO.response.PageResponse;
import com.example.tripItinerary.DTO.response.RestaurantCsvImportResponse;
import com.example.tripItinerary.DTO.response.RestaurantResponse;

public interface RestaurantService {

    RestaurantResponse create(
            RestaurantRequest request);

    RestaurantResponse update(
            Long id,
            RestaurantRequest request);

    RestaurantResponse getById(
            Long id);

    PageResponse<RestaurantResponse> getAll(
            Pageable pageable);

    PageResponse<RestaurantResponse> getByLocation(
            Long locationId,
            Pageable pageable);

    PageResponse<RestaurantResponse> search(
            Long locationId,
            String keyword,
            Pageable pageable);

    void delete(
            Long id);

    RestaurantCsvImportResponse importCsv(
            MultipartFile file,
            Long locationId);


            long countAll();

    long countByLocation(Long locationId);

    long countActive();

    long countActiveByLocation(Long locationId);
}