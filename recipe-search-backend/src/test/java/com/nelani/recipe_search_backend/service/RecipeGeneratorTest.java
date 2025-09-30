package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.model.Ingredient;
import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.model.Step;
import com.nelani.recipe_search_backend.repository.RecipeRepository;
import com.nelani.recipe_search_backend.service.serviceImpl.RecipeGenerator;
import com.nelani.recipe_search_backend.sockets.RecipeSocket;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RecipeGeneratorTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeSocket recipeSocket;

    @Spy
    @InjectMocks
    private RecipeGenerator recipeGenerator;

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
    public void RecipeGenerator_SaveAll_ReturnVoid() {

        // Act
        ArgumentCaptor<Recipe> recipeCaptor = ArgumentCaptor.forClass(Recipe.class);
        doReturn(recipeList).when(recipeGenerator).fetchRecipesFromAi(Mockito.anyString());

        recipeGenerator.generateAndSaveRecipes("searchWord");

        // Assert
        verify(recipeRepository, times(5)).save(recipeCaptor.capture());
        List<Recipe> savedRecipes = recipeCaptor.getAllValues();

        Assertions.assertThat(savedRecipes).isNotNull();
        Assertions.assertThat(savedRecipes)
                .hasSize(5)
                .extracting(Recipe::getName)
                .contains("recipe0", "recipe1", "recipe2", "recipe3", "recipe4");
        savedRecipes.forEach(recipe -> {
            Assertions.assertThat(recipe.getIngredients())
                    .extracting(Ingredient::getName)
                    .contains("ingredient");
            Assertions.assertThat(recipe.getSteps())
                    .extracting(Step::getDescription)
                    .contains("description");
        });
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
