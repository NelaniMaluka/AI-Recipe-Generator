package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.dto.IngredientDto;
import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.dto.StepDto;
import com.nelani.recipe_search_backend.response.RecipeResponse;
import com.nelani.recipe_search_backend.model.*;
import com.nelani.recipe_search_backend.repository.RecipeRepository;
import com.nelani.recipe_search_backend.service.serviceImpl.RecipeGenerator;
import com.nelani.recipe_search_backend.service.serviceImpl.RecipeServiceImpl;
import com.nelani.recipe_search_backend.sockets.RecipeSocket;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RecipeServiceImplTest {

        @Mock
        private RecipeRepository recipeRepository;

        @Mock
        private RecipeGenerator recipeGenerator;

        @Mock
        private RecipeSocket recipeSocket;

        @InjectMocks
        private RecipeServiceImpl recipeService;

        private List<Recipe> recipeList;

        @BeforeEach
        public void init() {
                recipeList = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                        List<Ingredient> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
                        List<Step> stepsList = List.of(createStep("description", 10));
                        recipeList.add(createRecipe("publicId" + i, "recipe" + i, "imgUrl", 10, ingredientsList,
                                        stepsList));
                }
        }

        @Test
        public void RecipeService_GetRecipe_ReturnsRecipeDto() {
                List<Ingredient> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
                List<Step> stepsList = List.of(createStep("description", 10));
                Optional<Recipe> savedRecipe = Optional
                                .ofNullable(createRecipe("publicId", "recipe0", "imgUrl", 10, ingredientsList,
                                                stepsList));

                // Act
                when(recipeRepository.findByPublicId("publicId")).thenReturn(savedRecipe);

                // Asserts
                RecipeResponse retrievedRecipe = recipeService.getRecipe("publicId");
                Assertions.assertThat(retrievedRecipe).isNotNull();
                Assertions.assertThat(retrievedRecipe.getName()).isEqualTo("recipe0");
        }

        @Test
        public void RecipeService_GetRecipe_ReturnsException() {
                IllegalArgumentException exception = assertThrows(
                                IllegalArgumentException.class,
                                () -> recipeService.getRecipe("publicId"));

                // Asserts
                assertEquals("Invalid recipe Id.", exception.getMessage());
        }

        @Test
        public void RecipeService_getRecipes_ReturnRecipesDto() {
                // Act
                Pageable pageable = PageRequest.of(0, 5);
                when(recipeRepository.searchRecipes("recipe", pageable)).thenReturn(recipeList);
                doNothing().when(recipeGenerator).generateAndSaveRecipes(any(String.class));

                // Assert
                List<RecipeResponse> retrievedRecipeResponseList = recipeService.getRecipes("recipe", 0, 5);
                Assertions.assertThat(retrievedRecipeResponseList).isNotNull();
                Assertions.assertThat(retrievedRecipeResponseList)
                                .hasSize(5)
                                .extracting(RecipeResponse::getName)
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
                List<RecipeResponse> retrievedRecipeResponseList = recipeService.getRecipes("recipe", 0, 5);
                Assertions.assertThat(retrievedRecipeResponseList).isNotNull();
                Assertions.assertThat(retrievedRecipeResponseList).isEmpty();
        }

        @Test
        public void RecipeService_GetRecipesByTimeAndMealType_ReturnRecipeList() {
                // Arrange
                int startTime = 0;
                int endTime = 180;
                MealType mealType = MealType.APPETIZER;
                DateFilter dateFilter = DateFilter.TODAY;
                Pageable pageable = PageRequest.of(0, 5);

                // Create mock recipe list
                List<Recipe> recipeList = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                        Recipe recipe = new Recipe();
                        recipe.setName("recipe" + i);
                        recipeList.add(recipe);
                }

                // Wrap list in a Page
                Page<Recipe> recipePage = new PageImpl<>(recipeList, pageable, recipeList.size());

                // Mock the repository
                when(recipeRepository.getRecipesByTimeAndMealType(
                                eq(startTime),
                                eq(endTime),
                                eq(mealType),
                                any(LocalDateTime.class),
                                any(LocalDateTime.class),
                                any(Pageable.class))).thenReturn(recipePage);

                // Act
                List<RecipeResponse> retrievedRecipesList = recipeService.getRecipesByTimeAndMealType(startTime,
                                endTime,
                                mealType, dateFilter, 0, 5);

                // Assert
                Assertions.assertThat(retrievedRecipesList).isNotNull();
                Assertions.assertThat(retrievedRecipesList)
                                .hasSize(5)
                                .extracting(RecipeResponse::getName)
                                .contains("recipe0", "recipe1", "recipe2", "recipe3", "recipe4");
        }

        @Test
        public void RecipeService_GetRecipesByTimeAndMealType_ReturnEmptyList() {
                // Arrange
                int startTime = 0;
                int endTime = 180;
                MealType mealType = MealType.APPETIZER;
                DateFilter dateFilter = DateFilter.TODAY;

                when(recipeRepository.getRecipesByTimeAndMealType(
                                eq(startTime),
                                eq(endTime),
                                eq(mealType),
                                any(LocalDateTime.class),
                                any(LocalDateTime.class),
                                any(Pageable.class))).thenReturn(Page.empty());

                // Act
                List<RecipeResponse> retrievedRecipesList = recipeService.getRecipesByTimeAndMealType(
                                startTime, endTime, mealType, dateFilter, 0, 5);

                // Assert
                Assertions.assertThat(retrievedRecipesList).isNotNull();
                Assertions.assertThat(retrievedRecipesList).isEmpty();
        }

        @Test
        public void RecipeService_UpdateRecipe_ShouldUpdateRecipeAndSendSocket() {
                // Arrange
                String publicId = "recipe123";

                IngredientDto ingredientDto = new IngredientDto("Sugar", "2 tbsp");
                StepDto stepDto = new StepDto("Mix ingredients", 5);

                RecipeDto dto = RecipeDto.builder()
                                .name("Cake")
                                .imageUrl("image.jpg")
                                .mealType(MealType.APPETIZER)
                                .cookTimeMinutes(30)
                                .ingredients(List.of(ingredientDto))
                                .steps(List.of(stepDto))
                                .build();

                List<Ingredient> existingIngredients = List.of(createIngredient("OldSugar", "1 tsp"));
                List<Step> existingSteps = List.of(createStep("Old step", 3));

                Recipe existingRecipe = createRecipe(publicId, "Old Cake", "oldImage.jpg", 25,
                                existingIngredients, existingSteps);

                when(recipeRepository.findByPublicId(publicId)).thenReturn(Optional.of(existingRecipe));
                when(recipeRepository.save(any(Recipe.class))).thenAnswer(i -> i.getArgument(0));

                // Act
                RecipeResponse response = recipeService.updateRecipe(publicId, dto);

                // Assert
                Assertions.assertThat(response).isNotNull();
                Assertions.assertThat(response.getName()).isEqualTo("Cake");
                Assertions.assertThat(response.getMealType()).isEqualTo(MealType.APPETIZER);
                Assertions.assertThat(response.getIngredients()).hasSize(1)
                                .extracting("name").containsExactly("Sugar");
                Assertions.assertThat(response.getSteps()).hasSize(1)
                                .extracting("description").containsExactly("Mix ingredients");

                // Verify repository save called
                verify(recipeRepository, times(1)).save(existingRecipe);

                // Verify socket sent
                verify(recipeSocket, times(1)).sendUpdatedRecipe(any(RecipeResponse.class));
        }

        @Test
        void RecipeService_deleteRecipe_ShouldDeleteRecipe() {
                String publicId = "abc123";
                Recipe recipe = new Recipe();
                recipe.setPublicId(publicId);

                // Mock repository to return the recipe
                when(recipeRepository.findByPublicId(publicId)).thenReturn(Optional.of(recipe));

                // Call the method
                recipeService.deleteRecipe(publicId);

                // Verify that delete was called once with the correct recipe
                verify(recipeRepository, times(1)).delete(recipe);
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

        private Recipe createRecipe(String publicId, String name, String imgUrl, int cookTimeMinutes,
                        List<Ingredient> ingredients, List<Step> steps) {
                Recipe recipe = Recipe.builder()
                                .publicId(publicId)
                                .name(name)
                                .imageUrl(imgUrl)
                                .mealType(MealType.APPETIZER)
                                .cookTimeMinutes(cookTimeMinutes)
                                .ingredients(ingredients)
                                .steps(steps)
                                .build();

                ingredients.forEach(ingredient -> ingredient.setRecipe(recipe));
                steps.forEach(step -> step.setRecipe(recipe));

                return recipe;
        }
}
