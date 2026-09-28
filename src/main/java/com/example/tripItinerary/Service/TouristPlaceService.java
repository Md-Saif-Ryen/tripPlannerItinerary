package com.example.tripItinerary.Service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.example.tripItinerary.DTO.request.TouristPlaceRequest;
import com.example.tripItinerary.DTO.response.TouristPlaceCsvImportResponse;
import com.example.tripItinerary.DTO.response.TouristPlaceResponse;

public interface TouristPlaceService {

        // ============================================================
        // CREATE
        // ============================================================

        TouristPlaceResponse create(
                        TouristPlaceRequest request);

        // ============================================================
        // UPDATE
        // ============================================================

        TouristPlaceResponse update(
                        Long id,
                        TouristPlaceRequest request);

        // ============================================================
        // GET BY ID
        // ============================================================

        TouristPlaceResponse getById(
                        Long id);

        // ============================================================
        // GET ALL - PAGINATED
        // ============================================================

        Page<TouristPlaceResponse> getAll(
                        int page,
                        int size,
                        String sortBy,
                        String direction);

        // ============================================================
        // GET BY LOCATION - PAGINATED
        // ============================================================

        Page<TouristPlaceResponse> getByLocation(
                        Long locationId,
                        int page,
                        int size,
                        String sortBy,
                        String direction);

        // ============================================================
        // GET ACTIVE - PAGINATED
        // ============================================================

        Page<TouristPlaceResponse> getActive(
                        int page,
                        int size,
                        String sortBy,
                        String direction);

        // ============================================================
        // GET ACTIVE BY LOCATION - PAGINATED
        // ============================================================

        Page<TouristPlaceResponse> getActiveByLocation(
                        Long locationId,
                        int page,
                        int size,
                        String sortBy,
                        String direction);

        // ============================================================
        // DELETE
        // ============================================================

        void delete(Long id);

        // ============================================================
        // CSV IMPORT
        // ============================================================

        TouristPlaceCsvImportResponse importCsv(
                        MultipartFile file);

        // ============================================================
        // COUNTS
        // ============================================================

        long countAll();

        long countByLocation(
                        Long locationId);

        long countActive();

        long countActiveByLocation(
                        Long locationId);
}