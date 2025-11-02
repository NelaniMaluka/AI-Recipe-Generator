package com.nelani.recipe_search_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ChangePasswordDto(

                @NotBlank(message = "Old password is required") @Size(min = 8, message = "Password must be at least 8 characters") String oldPassword,

                @NotBlank(message = "New password is required") @Size(min = 8, message = "Password must be at least 8 characters") String newPassword,

                @NotBlank(message = "Repeat password is required") String repeatPassword) {
}
