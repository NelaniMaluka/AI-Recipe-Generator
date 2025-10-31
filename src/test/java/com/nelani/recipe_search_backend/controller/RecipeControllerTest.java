package com.nelani.recipe_search_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nelani.recipe_search_backend.dto.IngredientDto;
import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.dto.StepDto;
import com.nelani.recipe_search_backend.response.IngredientResponse;
import com.nelani.recipe_search_backend.response.RecipeResponse;
import com.nelani.recipe_search_backend.response.StepResponse;
import com.nelani.recipe_search_backend.model.*;
import com.nelani.recipe_search_backend.security.JwtService;
import com.nelani.recipe_search_backend.service.RecipeService;
import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RecipeController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RecipeControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private RecipeService recipeService;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private UserDetailsService userDetailsService;

        private List<RecipeResponse> recipeList;

        @BeforeEach
        public void init() {
                recipeList = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                        List<IngredientResponse> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
                        List<StepResponse> stepsList = List.of(createStep("description", 10));
                        recipeList.add(createRecipe("publicId" + i, "recipe" + i, "imgUrl", 10, ingredientsList,
                                        stepsList));
                }
        }

        @Test
        public void RecipeController_GetMealTypes_ReturnMealTypeList() throws Exception {
                // Act
                ResultActions response = mockMvc.perform(
                                get("/api/public/recipe/meal-types")
                                                .contentType(MediaType.APPLICATION_JSON));

                // Assert
                response.andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", CoreMatchers.is(MealType.values().length)));
        }

        @Test
        public void RecipeController_GetDateFilter_ReturnDateFilterList() throws Exception {
                // Act
                ResultActions response = mockMvc.perform(
                                get("/api/public/recipe/date-filters")
                                                .contentType(MediaType.APPLICATION_JSON));

                // Assert
                response.andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", CoreMatchers.is(DateFilter.values().length)));
        }

        @Test
        public void RecipeController_GetRecipe_ReturnsRecipeDto() throws Exception {
                List<IngredientResponse> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
                List<StepResponse> stepsList = List.of(createStep("description", 10));
                RecipeResponse savedRecipe = createRecipe("publicId", "recipe0", "imgUrl", 10, ingredientsList,
                                stepsList);

                // Act
                when(recipeService.getRecipe("publicId")).thenReturn(savedRecipe);
                ResultActions response = mockMvc.perform(
                                get("/api/public/recipe/publicId")
                                                .contentType(MediaType.APPLICATION_JSON));

                // Asserts
                Assertions.assertThat(response).isNotNull();
                response.andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("recipe0"));
        }

        @Test
        public void RecipeController_GetRecipe_ReturnsException() throws Exception {
                when(recipeService.getRecipe("invalid"))
                                .thenThrow(new IllegalArgumentException("Invalid recipe Id."));

                // Act
                ResultActions response = mockMvc.perform(
                                get("/api/public/recipe/invalid")
                                                .contentType(MediaType.APPLICATION_JSON));

                // Asserts
                response.andExpect(status().isBadRequest());
        }

        @Test
        public void RecipeController_GetRecipes_ReturnRecipeDtoList() throws Exception {
                // Arrange
                when(recipeService.getRecipes("recipe", 0, 5)).thenReturn(recipeList);

                // Act
                ResultActions response = mockMvc.perform(
                                get("/api/public/recipe/search")
                                                .param("searchWord", "recipe")
                                                .param("page", "0")
                                                .param("size", "5")
                                                .contentType(MediaType.APPLICATION_JSON));

                // Assert
                response.andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", CoreMatchers.is(recipeList.size())))
                                .andExpect(jsonPath("$[0].name").value("recipe0"))
                                .andExpect(jsonPath("$[1].name").value("recipe1"));
        }

        @Test
        public void RecipeController_GetRecipesByTimeAndMealType_ReturnRecipeDtoList() throws Exception {
                // Arrange
                when(recipeService.getRecipesByTimeAndMealType(0, 180, MealType.APPETIZER, DateFilter.TODAY, 0, 5))
                                .thenReturn(recipeList);

                // Act
                ResultActions response = mockMvc.perform(
                                get("/api/public/recipes")
                                                .param("startTime", String.valueOf(0))
                                                .param("endTime", String.valueOf(180))
                                                .param("mealType", "APPETIZER")
                                                .param("dateFilter", "TODAY")
                                                .param("page", String.valueOf(0))
                                                .param("size", String.valueOf(5))
                                                .contentType(MediaType.APPLICATION_JSON));

                // Assert
                response.andExpect(status().isOk())
                                .andExpect(jsonPath("$.length()", CoreMatchers.is(recipeList.size())))
                                .andExpect(jsonPath("$[0].name").value("recipe0"))
                                .andExpect(jsonPath("$[1].name").value("recipe1"));
        }

        @Test
        void RecipeController_EmailRecipe_ReturnsMessageResponse() throws Exception {
                // Arrange
                doNothing().when(recipeService).emailRecipe(any(String.class), any(String.class));

                // Act
                mockMvc.perform(post("/api/public/recipe/email-recipe")
                                .param("email", "test-email@test.co.za")
                                .param("publicId", "recipe123")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message")
                                                .value("Recipe has been successfully sent to test-email@test.co.za"))
                                .andExpect(jsonPath("$.email").doesNotExist());
        }

        @Test
        public void RecipeController_UpdateRecipe_ReturnsUpdatedRecipe() throws Exception {
                // Arrange
                String publicId = "recipe123";
                RecipeDto recipeDto = RecipeDto.builder()
                                .name("Updated Cake")
                                .imageUrl("image.jpg")
                                .mealType(MealType.APPETIZER)
                                .cookTimeMinutes(30)
                                .ingredients(List.of(new IngredientDto("Sugar", "2 tbsp")))
                                .steps(List.of(new StepDto("Mix ingredients", 5)))
                                .build();

                RecipeResponse updatedResponse = RecipeResponse.builder()
                                .publicId(publicId)
                                .name("Updated Cake")
                                .imageUrl("image.jpg")
                                .mealType(MealType.APPETIZER)
                                .cookTimeMinutes(30)
                                .ingredients(List.of(
                                                IngredientResponse.builder().name("Sugar").quantity("2 tbsp").build()))
                                .steps(List.of(StepResponse.builder().description("Mix ingredients").estimatedMinutes(5)
                                                .build()))
                                .build();

                // Mock service
                when(recipeService.updateRecipe(any(String.class), any(RecipeDto.class))).thenReturn(updatedResponse);

                // Act & Assert
                mockMvc.perform(put("/api/admin/recipe/publicId")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(new ObjectMapper().writeValueAsString(recipeDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated Cake"))
                                .andExpect(jsonPath("$.mealType").value("APPETIZER"))
                                .andExpect(jsonPath("$.ingredients[0].name").value("Sugar"))
                                .andExpect(jsonPath("$.steps[0].description").value("Mix ingredients"));
        }

        @Test
        public void deleteRecipe_ShouldReturnSuccessMessage() throws Exception {
                String publicId = "abc123";

                // Mock the service call
                doNothing().when(recipeService).deleteRecipe(publicId);

                // Act & Assert
                mockMvc.perform(delete("/api/admin/recipe/abc123")
                                .param("publicId", publicId)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNoContent());

                // Verify that the service was called
                verify(recipeService).deleteRecipe(publicId);
        }

        private IngredientResponse createIngredient(String name, String quantity) {
                return IngredientResponse.builder()
                                .name(name)
                                .quantity(quantity)
                                .build();
        }

        private StepResponse createStep(String description, int minutes) {
                return StepResponse.builder()
                                .description(description)
                                .estimatedMinutes(minutes)
                                .build();
        }

        private RecipeResponse createRecipe(String publicId, String name, String imgUrl, int cookTimeMinutes,
                        List<IngredientResponse> ingredients, List<StepResponse> steps) {
                return RecipeResponse.builder()
                                .publicId(publicId)
                                .name(name)
                                .imageUrl(imgUrl)
                                .mealType(MealType.APPETIZER)
                                .cookTimeMinutes(cookTimeMinutes)
                                .ingredients(ingredients)
                                .steps(steps)
                                .build();
        }
}
