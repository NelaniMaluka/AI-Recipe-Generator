package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.model.MealType;
import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.repository.RecipeRepository;
import com.nelani.recipe_search_backend.service.serviceImpl.RecipeGenerator;
import com.nelani.recipe_search_backend.sockets.RecipeSocket;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
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
        recipeList = new ArrayList<>();
        recipeList.add(createRecipe("publicId", "recipe0", "imgUrl", 10));
        recipeList.add(createRecipe("publicId1", "recipe1", "imgUrl", 10));
        recipeList.add(createRecipe("publicId2", "recipe2", "imgUrl", 10));
        recipeList.add(createRecipe("publicId3", "recipe3", "imgUrl", 10));
        recipeList.add(createRecipe("publicId4", "recipe4", "imgUrl", 10));
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

    }

    private Recipe createRecipe(String publicId, String name, String imgUrl, int cookTimeMinutes) {
        return Recipe.builder()
                .publicId(publicId)
                .name(name)
                .imageUrl(imgUrl)
                .mealType(MealType.APPETIZER)
                .cookTimeMinutes(cookTimeMinutes)
                .build();
    }

}