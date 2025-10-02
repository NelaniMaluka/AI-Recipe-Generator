package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.dto.RecipeDto;

import java.util.List;

public interface RecipeService {
    RecipeDto getRecipe (String publicId);
    List<RecipeDto> getRecipes (String searchWord, int page, int size);
    void emailRecipe(String email, String publicId);
}
