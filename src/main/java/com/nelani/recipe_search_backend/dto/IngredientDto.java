package com.nelani.recipe_search_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record IngredientDto(

                @NotBlank(message = "Ingredient name cannot be blank") String name,

                @NotBlank(message = "Quantity cannot be blank") String quantity) {
}
