package com.example.tripItinerary.Service;


import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.tripItinerary.Repo.TemporaryItineraryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemporaryItineraryCleanupService {

    private final TemporaryItineraryRepository temporaryItineraryRepository;

    // ======================================================
    // RUN EVERY 10 MINUTES
    // ======================================================

    @Scheduled(fixedRate = 600000)
    @Transactional
    public void deleteExpiredTemporaryItineraries() {

        LocalDateTime now = LocalDateTime.now();

        int deleted = temporaryItineraryRepository
                .deleteExpired(
                        now);

        if (deleted > 0) {

            log.info(
                    "Deleted {} expired temporary itineraries.",
                    deleted);
        }
    }
}