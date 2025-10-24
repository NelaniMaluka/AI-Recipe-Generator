package com.nelani.recipe_search_backend.controller;

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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

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
        public void RecipeController_GetRecipe_ReturnsRecipeDto() throws Exception {
                List<IngredientResponse> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
                List<StepResponse> stepsList = List.of(createStep("description", 10));
                RecipeResponse savedRecipe = createRecipe("publicId", "recipe0", "imgUrl", 10, ingredientsList,
                                stepsList);

                // Act
                when(recipeService.getRecipe("publicId")).thenReturn(savedRecipe);
                ResultActions response = mockMvc.perform(
                                get("/api/recipe/publicId")
                                                .contentType(MediaType.APPLICATION_JSON));

                // Asserts
                Assertions.assertThat(response).isNotNull();
                response.andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(jsonPath("$.name").value("recipe0"));
        }

        @Test
        public void RecipeController_GetRecipe_ReturnsException() throws Exception {
                when(recipeService.getRecipe("invalid"))
                                .thenThrow(new IllegalArgumentException("Invalid recipe Id."));

                // Act
                ResultActions response = mockMvc.perform(
                                get("/api/recipe/invalid")
                                                .contentType(MediaType.APPLICATION_JSON));

                // Asserts
                response.andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        public void RecipeController_GetMealTypes_ReturnMealTypeList() throws Exception {
                // Act
                ResultActions response = mockMvc.perform(
                                get("/api/recipe/meal-types")
                                                .contentType(MediaType.APPLICATION_JSON));

                // Assert
                response.andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(jsonPath("$.length()", CoreMatchers.is(MealType.values().length)));
        }

        @Test
        public void RecipeController_GetDateFilter_ReturnDateFilterList() throws Exception {
                // Act
                ResultActions response = mockMvc.perform(
                                get("/api/recipe/date-filters")
                                                .contentType(MediaType.APPLICATION_JSON));

                // Assert
                response.andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(jsonPath("$.length()", CoreMatchers.is(DateFilter.values().length)));
        }

        @Test
        public void RecipeController_GetRecipes_ReturnRecipeDtoList() throws Exception {
                // Arrange
                when(recipeService.getRecipes("recipe", 0, 5)).thenReturn(recipeList);

                // Act
                ResultActions response = mockMvc.perform(
                                get("/api/recipe")
                                                .param("searchWord", "recipe")
                                                .param("page", "0")
                                                .param("size", "5")
                                                .contentType(MediaType.APPLICATION_JSON));

                // Assert
                response.andExpect(MockMvcResultMatchers.status().isOk())
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
                                get("/api/recipe/all-recipes")
                                                .param("startTime", String.valueOf(0))
                                                .param("endTime", String.valueOf(180))
                                                .param("mealType", "APPETIZER")
                                                .param("dateFilter", "TODAY")
                                                .param("page", String.valueOf(0))
                                                .param("size", String.valueOf(5))
                                                .contentType(MediaType.APPLICATION_JSON));

                // Assert
                response.andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(jsonPath("$.length()", CoreMatchers.is(recipeList.size())))
                                .andExpect(jsonPath("$[0].name").value("recipe0"))
                                .andExpect(jsonPath("$[1].name").value("recipe1"));
        }

        @Test
        public void RecipeController_GetRecipes_ReturnEmptyList() throws Exception {
                // Act
                ResultActions response = mockMvc.perform(
                                get("/api/recipe")
                                                .param("searchWord", "recipe")
                                                .param("page", "0")
                                                .param("size", "5")
                                                .contentType(MediaType.APPLICATION_JSON));

                // Assert
                response.andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty())
                                .andExpect(jsonPath("$.length()", CoreMatchers.is(0)));
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
