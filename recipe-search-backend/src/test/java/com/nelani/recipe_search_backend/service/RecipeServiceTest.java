package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.dto.IngredientDto;
import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.dto.StepDto;
import com.nelani.recipe_search_backend.model.Ingredient;
import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.model.Step;
import com.nelani.recipe_search_backend.repository.RecipeRepository;
import com.nelani.recipe_search_backend.service.serviceImpl.RecipeGenerator;
import com.nelani.recipe_search_backend.service.serviceImpl.RecipeServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeGenerator recipeGenerator;

    @InjectMocks
    private RecipeServiceImpl recipeService;

    private List<Recipe> recipeList;

    @BeforeEach
    public void init() {
        List<Ingredient> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
        List<Step> stepsList = List.of(createStep("description", 10));

        recipeList = new ArrayList<>();  // <-- initialize here
        recipeList.add(createRecipe("recipe0", "imgUrl", 10, ingredientsList, stepsList));
        recipeList.add(createRecipe("recipe1", "imgUrl", 10, ingredientsList, stepsList));
        recipeList.add(createRecipe("recipe2", "imgUrl", 10, ingredientsList, stepsList));
        recipeList.add(createRecipe("recipe3", "imgUrl", 10, ingredientsList, stepsList));
        recipeList.add(createRecipe("recipe4", "imgUrl", 10, ingredientsList, stepsList));
    }

    @Test
    public void RecipeService_getRecipes_ReturnRecipesDto() {
        // Act
        Pageable pageable = PageRequest.of(0, 5);
        when(recipeRepository.searchRecipes("recipe", pageable)).thenReturn(recipeList);
        doNothing().when(recipeGenerator).generateAndSaveRecipes(any(String.class));

        // Assert
        List<RecipeDto> retrievedRecipeDtoList = recipeService.getRecipes("recipe", 0, 5);
        Assertions.assertThat(retrievedRecipeDtoList).isNotNull();
        Assertions.assertThat(retrievedRecipeDtoList)
                .hasSize(5)
                .extracting(RecipeDto::getName)
                .contains("recipe0", "recipe1", "recipe2", "recipe3", "recipe4");
        retrievedRecipeDtoList.forEach(recipe -> {
            Assertions.assertThat(recipe.getIngredients())
                    .extracting(IngredientDto::getName)
                    .contains("ingredient");
            Assertions.assertThat(recipe.getSteps())
                    .extracting(StepDto::getDescription)
                    .contains("description");
        });
    }

    @Test
    public void RecipeService_getRecipes_ReturnEmptyList() {
        // Arrange
        List<Recipe> recipeList = new ArrayList<>();

        // Act
        Pageable pageable = PageRequest.of(0, 5);
        when(recipeRepository.searchRecipes("recipe", pageable)).thenReturn(recipeList);
        doNothing().when(recipeGenerator).generateAndSaveRecipes(any(String.class));

        // Assert
        List<RecipeDto> retrievedRecipeDtoList = recipeService.getRecipes("recipe", 0, 5);
        Assertions.assertThat(retrievedRecipeDtoList).isNotNull();
        Assertions.assertThat(retrievedRecipeDtoList).isEmpty();
    }

    private Ingredient createIngredient(String name, String quantity) {
        return Ingredient.builder()
                .name(name)
                .quantity(quantity)
                .build();
    }

    private Step createStep(String description, int minutes) {
        return Step.builder()
                .description(description)
                .estimatedMinutes(minutes)
                .build();
    }

    private Recipe createRecipe(String name, String imgUrl, int cookTimeMinutes, List<Ingredient> ingredients, List<Step> steps) {
        return Recipe.builder()
                .name(name)
                .imageUrl(imgUrl)
                .cookTimeMinutes(cookTimeMinutes)
                .ingredients(ingredients)
                .steps(steps)
                .build();
    }
}
