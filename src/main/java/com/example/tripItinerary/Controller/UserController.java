package com.example.tripItinerary.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.tripItinerary.DTO.request.ResetPasswordRequest;
import com.example.tripItinerary.DTO.request.UserRequest;
import com.example.tripItinerary.DTO.request.userUpdateRequest;
import com.example.tripItinerary.DTO.response.ApiResponse;
import com.example.tripItinerary.DTO.response.UserResponse;
import com.example.tripItinerary.Service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @PostMapping("/create_user")
    public ResponseEntity<ApiResponse<UserResponse>> create(
            @Valid @RequestBody UserRequest request) {

                System.out.println("Creating user with email: " + request.getEmail() + ", full name: " + request.getFullName());
        UserResponse response = userService.create(request);

        System.out.println("User created with ID: " + response.getId() + ", email: " + response.getEmail() + ", full name: " + response.getFullName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User created successfully.", response));
    }

    @PutMapping("/updateById/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody userUpdateRequest request) {

        UserResponse response = userService.update(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("User updated successfully.", response));
    }

    @GetMapping("/getById/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success(userService.getById(id)));
    }

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {

        return ResponseEntity.ok(
                ApiResponse.success(userService.getAll()));
    }

    @DeleteMapping("/deleteById/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id) {

        userService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully.", null));
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        userService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password reset successfully.", null));
    }

}