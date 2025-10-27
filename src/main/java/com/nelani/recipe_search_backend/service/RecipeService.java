package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.response.RecipeResponse;
import com.nelani.recipe_search_backend.model.DateFilter;
import com.nelani.recipe_search_backend.model.MealType;

import java.util.List;

public interface RecipeService {
    RecipeResponse getRecipe(String publicId);

    List<RecipeResponse> getRecipes(String searchWord, int page, int size);

    List<RecipeResponse> getRecipesByTimeAndMealType(int startTime, int endTime, MealType mealType,
            DateFilter dateFilter, int page, int size);

    void emailRecipe(String email, String publicId);

    RecipeResponse updateRecipe(RecipeDto dto);

    void deleteRecipe(String publicId);
}
