package com.nelani.recipe_search_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nelani.recipe_search_backend.dto.IngredientDto;
import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.dto.StepDto;
import com.nelani.recipe_search_backend.service.RecipeService;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
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
public class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RecipeService recipeService;

    private List<RecipeDto> recipeList;

    @BeforeEach
    public void init() {
        List<IngredientDto> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
        List<StepDto> stepsList = List.of(createStep("description", 10));

        recipeList = new ArrayList<>();  // <-- initialize here
        recipeList.add(createRecipe("recipe0", "imgUrl", 10, ingredientsList, stepsList));
        recipeList.add(createRecipe("recipe1", "imgUrl", 10, ingredientsList, stepsList));
        recipeList.add(createRecipe("recipe2", "imgUrl", 10, ingredientsList, stepsList));
        recipeList.add(createRecipe("recipe3", "imgUrl", 10, ingredientsList, stepsList));
        recipeList.add(createRecipe("recipe4", "imgUrl", 10, ingredientsList, stepsList));
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
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.length()", CoreMatchers.is(recipeList.size())))
                .andExpect(jsonPath("$[0].name").value("recipe0"))
                .andExpect(jsonPath("$[0].ingredients.length()").value(1))
                .andExpect(jsonPath("$[1].name").value("recipe1"))
                .andExpect(jsonPath("$[1].ingredients.length()").value(1));
    }

    @Test
    public void RecipeController_GetRecipes_ReturnEmptyList() throws Exception {
        // Act
        ResultActions response = mockMvc.perform(
                get("/api/recipe")
                        .param("searchWord", "recipe")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // Assert
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isEmpty())
                .andExpect(jsonPath("$.length()", CoreMatchers.is(0)));
    }

    private IngredientDto createIngredient(String name, String quantity) {
        return IngredientDto.builder()
                .name(name)
                .quantity(quantity)
                .build();
    }

    private StepDto createStep(String description, int minutes) {
        return StepDto.builder()
                .description(description)
                .estimatedMinutes(minutes)
                .build();
    }

    private RecipeDto createRecipe(String name, String imgUrl, int cookTimeMinutes, List<IngredientDto> ingredients, List<StepDto> steps) {
        return RecipeDto.builder()
                .name(name)
                .imageUrl(imgUrl)
                .cookTimeMinutes(cookTimeMinutes)
                .ingredients(ingredients)
                .steps(steps)
                .build();
    }
}

