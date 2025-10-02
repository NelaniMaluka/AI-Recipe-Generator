package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.dto.IngredientDto;
import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.dto.StepDto;
import com.nelani.recipe_search_backend.model.MealType;
import com.nelani.recipe_search_backend.model.Recipe;
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
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
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
        recipeList = new ArrayList<>();
        recipeList.add(createRecipe("publicId","recipe0", "imgUrl", 10));
        recipeList.add(createRecipe("publicId1","recipe1", "imgUrl", 10));
        recipeList.add(createRecipe("publicId2","recipe2", "imgUrl", 10));
        recipeList.add(createRecipe("publicId3","recipe3", "imgUrl", 10));
        recipeList.add(createRecipe("publicId4","recipe4", "imgUrl", 10));
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
