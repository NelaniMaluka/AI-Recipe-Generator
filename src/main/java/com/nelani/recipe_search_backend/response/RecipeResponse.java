package com.nelani.recipe_search_backend.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nelani.recipe_search_backend.model.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Detailed information about a recipe")
public class RecipeResponse {

        @Schema(description = "Publicly accessible unique identifier for the recipe", example = "abc123xyz")
        private String publicId;

        @Schema(description = "Name of the recipe", example = "Spaghetti Bolognese")
        private String name;

        @Schema(description = "URL of the recipe's image", example = "https://example.com/images/spaghetti.jpg")
        private String imageUrl;

        @Schema(description = "Type of meal for the recipe", example = "DINNER")
        private MealType mealType;

        @Schema(description = "Time required to cook the recipe in minutes", example = "45")
        private Integer cookTimeMinutes;

        @Schema(description = "Total views for the recipe", example = "1000")
        private Long views;

        @Schema(description = "List of ingredients required for the recipe")
        private List<IngredientResponse> ingredients;

        @Schema(description = "Step-by-step instructions to prepare the recipe")
        private List<StepResponse> steps;
}
