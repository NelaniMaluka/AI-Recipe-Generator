package com.nelani.recipe_search_backend.service.serviceImpl;

import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.mapper.RecipeMapper;
import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.notifications.EmailService;
import com.nelani.recipe_search_backend.repository.RecipeRepository;
import com.nelani.recipe_search_backend.service.RecipeService;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class RecipeServiceImpl implements RecipeService {

    private final RecipeGenerator recipeGenerator;
    private final RecipeRepository recipeRepository;
    private final EmailService emailService;

    public RecipeServiceImpl(RecipeGenerator recipeGenerator, RecipeRepository recipeRepository, EmailService emailService) {
        this.recipeGenerator = recipeGenerator;
        this.recipeRepository = recipeRepository;
        this.emailService = emailService;
    }

    @Override
    @Cacheable(value = "recipe", key = "#publicId")
    public RecipeDto getRecipe(String publicId) {
        // Fetch the Recipe
        Recipe recipe = recipeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recipe Id."));

        // Return a RecipeDto to the user
        return RecipeMapper.mapRecipeWithAllDetails(recipe);
    }

    @Override
    @Cacheable(value = "recipes", key = "#searchWord")
    public List<RecipeDto> getRecipes(String searchWord, int page, int size) {
        // Fetch fallback immediately
        Pageable pageable = PageRequest.of(page, size);
        List<Recipe> fallbackRecipes = recipeRepository.searchRecipes(searchWord, pageable);
        List<RecipeDto> fallbackRecipesDto = fallbackRecipes.stream()
                .map(RecipeMapper::mapRecipeWithMinimalDetails)
                        .toList();

        // Trigger async AI generation for DB population
        recipeGenerator.generateAndSaveRecipes(searchWord);

        //  ️Return fallback instantly
        return fallbackRecipesDto;
    }

    @Override
    public void emailRecipe(String email, String publicId) {
        // Fetch the Recipe
        recipeRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recipe Id."));

        // Email the recipe to the provided email
        emailService.prepareAndSendEmail(email, publicId);
    }

}
