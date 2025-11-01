package com.nelani.recipe_search_backend.service.serviceImpl;

import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.model.RecipeView;
import com.nelani.recipe_search_backend.repository.RecipeRepository;
import com.nelani.recipe_search_backend.repository.RecipeViewRepository;
import com.nelani.recipe_search_backend.repository.UserRepository;
import com.nelani.recipe_search_backend.service.RecipeViewService;
import com.nelani.recipe_search_backend.sockets.RecipeSocket;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecipeViewServiceImpl implements RecipeViewService {

    private final RecipeViewRepository recipeViewRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final RecipeSocket recipeSocket;

    public RecipeViewServiceImpl(RecipeViewRepository recipeViewRepository, UserRepository userRepository,
            RecipeRepository recipeRepository, RecipeSocket recipeSocket) {
        this.recipeViewRepository = recipeViewRepository;
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.recipeSocket = recipeSocket;
    }

    /**
     * Retrieves the total view count for a given recipe.
     * <p>
     * Uses caching to minimize database access for frequently requested recipes.
     *
     * @param publicId the recipe's public identifier
     * @return the total number of views
     * @throws IllegalArgumentException if the recipe is not found
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "recipe-views", key = "#publicId")
    public long getRecipeViews(String publicId) {
        // Fetch recipe or throw if not found
        Recipe recipe = recipeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Recipe not found with ID: " + publicId));

        // Return current total view count
        return recipe.getViews();
    }

    /**
     * Increments a recipe’s total view count and records a user view if
     * authenticated.
     *
     * @param publicId the recipe's public identifier
     * @throws IllegalArgumentException if the recipe is not found
     * @throws ResponseStatusException  if the user is not authenticated
     */
    @Override
    @Transactional
    @CacheEvict(value = "recipe-views", key = "#publicId")
    public void addView(String publicId) {
        // Fetch recipe or throw if invalid
        Recipe recipe = recipeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Recipe not found with ID: " + publicId));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Fetch the authenticated user
        String username = auth.getName();
        var user = userRepository.findByEmail(username);

        // Record a user view only if it doesn’t already exist
        if (user.isPresent()) {
            if (recipeViewRepository.findByUserAndRecipe(user.get(), recipe).isEmpty()) {
                recipeViewRepository.save(RecipeView.builder()
                        .user(user.get())
                        .recipe(recipe)
                        .build());
                recipeRepository.save(recipe);
            }
        }

        // Increment global recipe views
        recipe.setViews(recipe.getViews() + 1);

        recipeSocket.sendUpdatedRecipeViews(recipe);
    }

}
