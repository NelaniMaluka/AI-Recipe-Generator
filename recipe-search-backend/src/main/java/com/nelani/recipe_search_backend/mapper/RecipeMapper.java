package com.nelani.recipe_search_backend.mapper;

import com.nelani.recipe_search_backend.response.IngredientResponse;
import com.nelani.recipe_search_backend.response.RecipeResponse;
import com.nelani.recipe_search_backend.response.StepResponse;
import com.nelani.recipe_search_backend.model.Ingredient;
import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.model.Step;

import java.util.List;

public class RecipeMapper {

        public static RecipeResponse mapRecipeWithMinimalDetails(Recipe recipe) {
                return RecipeResponse.builder()
                                .publicId(recipe.getPublicId())
                                .name(recipe.getName())
                                .imageUrl(recipe.getImageUrl())
                                .mealType(recipe.getMealType())
                                .cookTimeMinutes(recipe.getCookTimeMinutes())
                                .build();
        }

        public static RecipeResponse mapRecipeWithAllDetails(Recipe recipe) {
                List<IngredientResponse> ingredientResponses = recipe.getIngredients().stream()
                                .map(RecipeMapper::mapIngredient)
                                .toList();
                List<StepResponse> stepResponses = recipe.getSteps().stream()
                                .map(RecipeMapper::mapStep)
                                .toList();

                return RecipeResponse.builder()
                                .publicId(recipe.getPublicId())
                                .name(recipe.getName())
                                .imageUrl(recipe.getImageUrl())
                                .mealType(recipe.getMealType())
                                .cookTimeMinutes(recipe.getCookTimeMinutes())
                                .ingredients(ingredientResponses)
                                .steps(stepResponses)
                                .build();
        }

        private static IngredientResponse mapIngredient(Ingredient ingredient) {
                return IngredientResponse.builder()
                                .name(ingredient.getName())
                                .quantity(ingredient.getQuantity())
                                .build();
        }

        private static StepResponse mapStep(Step step) {
                return StepResponse.builder()
                                .description(step.getDescription())
                                .estimatedMinutes(step.getEstimatedMinutes())
                                .build();
        }
}
