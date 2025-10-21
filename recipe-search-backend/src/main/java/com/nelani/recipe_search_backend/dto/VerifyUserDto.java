package com.nelani.recipe_search_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyUserDto {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Token must not be blank")
    @Pattern(regexp = "\\d{6,7}", message = "Token must be a 6 or 7-digit number")
    private String token;
}
