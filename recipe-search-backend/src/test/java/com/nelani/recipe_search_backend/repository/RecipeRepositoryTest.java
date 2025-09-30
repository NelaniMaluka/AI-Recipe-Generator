package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.Ingredient;
import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.model.Step;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

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
    public void RecipeRepository_SaveAll_RetrieveSavedRecipe() {
        // Arrange
        List<Ingredient> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
        List<Step> stepsList = List.of(createStep("description", 10));
        Recipe saveRecipe = createRecipe("recipe", "igmUrl", 10, ingredientsList, stepsList);

        // Act
        recipeRepository.save(saveRecipe);

        // Retrieve the saved recipe from DB and assert
        Recipe retrievedRecipe = recipeRepository.findById(saveRecipe.getId()).orElse(null);
        Assertions.assertThat(retrievedRecipe).isNotNull();
        Assertions.assertThat(retrievedRecipe.getId()).isGreaterThan(0);
        Assertions.assertThat(retrievedRecipe.getIngredients()).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("name", "ingredient");
        Assertions.assertThat(retrievedRecipe.getSteps()).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("description", "description");
    }

    @Test
    public void RecipeRepository_SearchRecipes_Get5RelevantRecipes() {
        // Act
        recipeList.forEach(recipe -> {
            recipeRepository.save(recipe);
        });

        // Retrieve the saved recipe from DB and assert
        Pageable pageable = PageRequest.of(0, 5);
        List<Recipe> retrievedRecipesList = recipeRepository.searchRecipes("recipe", pageable);

        Assertions.assertThat(retrievedRecipesList).isNotNull();
        Assertions.assertThat(retrievedRecipesList)
                .hasSize(5)
                .extracting(Recipe::getName)
                .contains("recipe0", "recipe1", "recipe2", "recipe3", "recipe4");
        retrievedRecipesList.forEach(recipe -> {
            Assertions.assertThat(recipe.getIngredients())
                    .extracting(Ingredient::getName)
                    .contains("ingredient");
            Assertions.assertThat(recipe.getSteps())
                    .extracting(Step::getDescription)
                    .contains("description");
        });
    }

    @Test
    public void RecipeRepository_CheckRecipe_FindMatchingRecipe() {
        // Arrange
        List<Ingredient> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
        List<Step> stepsList = List.of(createStep("description", 10));
        Recipe saveRecipe = createRecipe("recipe", "igmUrl", 10, ingredientsList, stepsList);

        // Act
        recipeRepository.save(saveRecipe);

        // Retrieve the saved recipe from DB and assert
        boolean recipeMatch = recipeRepository.existsByNameAndIngredientsAndSteps(saveRecipe.getName(), saveRecipe.getIngredients(), saveRecipe.getSteps());
        Assertions.assertThat(recipeMatch).isTrue();
    }

    @Test
    public void RecipeRepository_CheckRecipe_FindNonMatchingRecipe() {
        // Arrange
        List<Ingredient> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
        List<Step> stepsList = List.of(createStep("description", 10));
        Recipe saveRecipe = createRecipe("recipe", "igmUrl", 10, ingredientsList, stepsList);

        Recipe checkRecipe = createRecipe("recipe2", "igmUrl", 10, ingredientsList, stepsList);

        // Act
        recipeRepository.save(saveRecipe);

        // Retrieve the saved recipe from DB and assert
        boolean recipeMatch = recipeRepository.existsByNameAndIngredientsAndSteps(checkRecipe.getName(), checkRecipe.getIngredients(), checkRecipe.getSteps());
        Assertions.assertThat(recipeMatch).isFalse();
    }

    @Test
    public void RecipeRepository_SearchRecipes_GetEmptyList() {
        // Retrieve non-existent recipe from DB and assert
        Pageable pageable = PageRequest.of(0, 5);
        List<Recipe> retrievedRecipesList = recipeRepository.searchRecipes("nonexistent", pageable);

        Assertions.assertThat(retrievedRecipesList).isNotNull();
        Assertions.assertThat(retrievedRecipesList).isEmpty();
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


