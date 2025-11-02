package com.nelani.recipe_search_backend.dto;

import com.nelani.recipe_search_backend.model.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record RecipeDto(

                @NotBlank(message = "Recipe name cannot be blank") @Size(max = 100, message = "Recipe name cannot exceed 100 characters") String name,

                @NotBlank(message = "Image URL cannot be blank") String imageUrl,

                @NotNull(message = "Meal type must be specified") MealType mealType,

                @NotNull(message = "Cook time must be specified") @Min(value = 1, message = "Cook time must be at least 1 minute") Integer cookTimeMinutes,

                @NotEmpty(message = "Recipe must have at least one ingredient") @Valid List<@NotNull(message = "Ingredient cannot be null") IngredientDto> ingredients,

                @NotEmpty(message = "Recipe must have at least one step") @Valid List<@NotNull(message = "Step cannot be null") StepDto> steps) {
}
