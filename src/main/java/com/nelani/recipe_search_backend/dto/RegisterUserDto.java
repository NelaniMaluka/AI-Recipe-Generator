package com.nelani.recipe_search_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record RegisterUserDto(

                @NotBlank(message = "First name is required") String firstname,

                @NotBlank(message = "Last name is required") String lastname,

                @Email(message = "Email should be valid") @NotBlank(message = "Email is required") String email,

                @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Password must be at least 8 characters long and include at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character") String password) {
}
