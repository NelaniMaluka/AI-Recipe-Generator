package com.nelani.recipe_search_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordResetDto {

    @NotBlank(message = "Token must not be blank")
    @Pattern(regexp = "\\d{6,7}", message = "Token must be a 6 or 7-digit number")
    private String token;

    @Email(message = "Must be a valid email")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String newPassword;

    @NotBlank(message = "Repeat password is required")
    private String repeatPassword;
}
