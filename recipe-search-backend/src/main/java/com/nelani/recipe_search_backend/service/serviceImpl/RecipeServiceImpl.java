package com.nelani.recipe_search_backend.service.serviceImpl;

import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.mapper.RecipeMapper;
import com.nelani.recipe_search_backend.model.Recipe;
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

    public RecipeServiceImpl(RecipeGenerator recipeGenerator, RecipeRepository recipeRepository) {
        this.recipeGenerator = recipeGenerator;
        this.recipeRepository = recipeRepository;
    }

    @Override
    @Cacheable(value = "recipes", key = "#searchWord")
    public List<RecipeDto> getRecipes(String searchWord, int page, int size) {
        // Fetch fallback immediately
        Pageable pageable = PageRequest.of(page, size);
        List<Recipe> fallbackRecipes = recipeRepository.searchRecipes(searchWord, pageable);
        List<RecipeDto> fallbackRecipesDto = fallbackRecipes.stream()
                .map(RecipeMapper::mapRecipe)
                        .toList();

        // Trigger async AI generation for DB population
        recipeGenerator.generateAndSaveRecipes(searchWord);

        //  ️Return fallback instantly
        return fallbackRecipesDto;
    }

}
