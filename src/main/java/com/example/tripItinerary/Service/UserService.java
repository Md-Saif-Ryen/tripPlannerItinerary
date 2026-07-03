package com.example.tripItinerary.Service;

import java.util.List;

import com.example.tripItinerary.DTO.request.UserRequest;
import com.example.tripItinerary.DTO.response.UserResponse;

public interface UserService {

    UserResponse create(UserRequest request);

    UserResponse update(Long id, UserRequest request);

    UserResponse getById(Long id);

    List<UserResponse> getAll();

    void delete(Long id);

}