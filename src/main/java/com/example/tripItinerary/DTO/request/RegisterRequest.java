package com.example.tripItinerary.DTO.request;



import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Full name is required.")
    @Size(max = 255)
    private String fullName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 100)
    private String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must contain exactly 10 digits.")
    private String phoneNumber;

    @Size(max = 1000)
    private String profileImage;

}