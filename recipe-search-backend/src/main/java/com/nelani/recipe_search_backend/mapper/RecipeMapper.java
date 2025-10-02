package com.nelani.recipe_search_backend.mapper;

import com.nelani.recipe_search_backend.dto.IngredientDto;
import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.dto.StepDto;
import com.nelani.recipe_search_backend.model.Ingredient;
import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.model.Step;

import java.util.List;

public class RecipeMapper {

    public static RecipeDto mapRecipeWithMinimalDetails(Recipe recipe) {
        return RecipeDto.builder()
                .publicId(recipe.getPublicId())
                .name(recipe.getName())
                .imageUrl(recipe.getImageUrl())
                .mealType(recipe.getMealType())
                .cookTimeMinutes(recipe.getCookTimeMinutes())
                .build();
    }

    public static RecipeDto mapRecipeWithAllDetails(Recipe recipe) {
        List<IngredientDto> ingredientDtos = recipe.getIngredients().stream()
                .map(RecipeMapper::mapIngredient)
                .toList();
        List<StepDto> stepDtos = recipe.getSteps().stream()
                .map(RecipeMapper::mapStep)
                .toList();

        return RecipeDto.builder()
                .publicId(recipe.getPublicId())
                .name(recipe.getName())
                .imageUrl(recipe.getImageUrl())
                .mealType(recipe.getMealType())
                .cookTimeMinutes(recipe.getCookTimeMinutes())
                .ingredients(ingredientDtos)
                .steps(stepDtos)
                .build();
    }

    private static IngredientDto mapIngredient(Ingredient ingredient) {
        return IngredientDto.builder()
                .name(ingredient.getName())
                .quantity(ingredient.getQuantity())
                .build();
    }

    private static StepDto mapStep(Step step) {
        return StepDto.builder()
                .description(step.getDescription())
                .estimatedMinutes(step.getEstimatedMinutes())
                .build();
    }
}
