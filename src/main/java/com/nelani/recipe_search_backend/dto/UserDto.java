package com.nelani.recipe_search_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.util.List;

@Builder
public record UserDto(

                @NotBlank(message = "First name must not be blank") String firstname,

                @NotBlank(message = "Last name must not be blank") String lastname,

                List<String> allergies) {
}
