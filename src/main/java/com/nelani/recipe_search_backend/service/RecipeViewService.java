package com.nelani.recipe_search_backend.service;

public interface RecipeViewService {
    long getRecipeViews(String publicId);

    void addView(String publicId);
}
