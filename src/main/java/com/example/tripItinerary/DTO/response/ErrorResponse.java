package com.example.tripItinerary.DTO.response;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private boolean success;

    private int status;

    private String error;

    private String message;

    private String path;

    private LocalDateTime timestamp;

    private Map<String, String> validationErrors;

}