package com.nelani.recipe_search_backend.service;

public interface RecipeViewsService {
    long getRecipeViews(String publicId);

    void addView(String publicId);
}
