package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.Ingredient;
import com.nelani.recipe_search_backend.model.MealType;
import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.model.Step;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
public class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    private List<Recipe> recipeList;

    @BeforeEach
    public void init() {
        recipeList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            List<Ingredient> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
            List<Step> stepsList = List.of(createStep("description", 10));
            recipeList.add(createRecipe("publicId" + i, "recipe" + i, "imgUrl", 10, ingredientsList, stepsList));
        }
    }

    @Test
    public void RecipeRepository_SaveAll_RetrieveSavedRecipe() {
        // Arrange
        List<Ingredient> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
        List<Step> stepsList = List.of(createStep("description", 10));
        Recipe saveRecipe = createRecipe("publicId", "recipe", "igmUrl", 10, ingredientsList, stepsList);

        // Act
        recipeRepository.save(saveRecipe);

        // Retrieve the saved recipe from DB and assert
        Recipe retrievedRecipe = recipeRepository.findById(saveRecipe.getId()).orElse(null);
        Assertions.assertThat(retrievedRecipe).isNotNull();
        Assertions.assertThat(retrievedRecipe.getId()).isEqualTo(saveRecipe.getId());
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
        Recipe saveRecipe = createRecipe("publicId", "recipe", "igmUrl", 10, ingredientsList, stepsList);

        // Act
        recipeRepository.save(saveRecipe);

        // Retrieve the saved recipe from DB and assert
        boolean recipeMatch = recipeRepository.existsByUniquenessIdentifier(saveRecipe.getUniquenessIdentifier());
        Assertions.assertThat(recipeMatch).isTrue();
    }

    @Test
    public void RecipeRepository_CheckRecipe_FindNonMatchingRecipe() {
        // Arrange
        List<Ingredient> saveIngredients = List.of(createIngredient("ingredient", "4 cups"));
        List<Step> saveSteps = List.of(createStep("description", 10));
        Recipe saveRecipe = createRecipe("publicId", "recipe", "imgUrl", 10, saveIngredients, saveSteps);

        // Create fresh instances for checkRecipe
        List<Ingredient> checkIngredients = List.of(createIngredient("ingredient", "4 cups"));
        List<Step> checkSteps = List.of(createStep("description", 10));
        Recipe checkRecipe = createRecipe("publicId", "recipe2", "imgUrl", 10, checkIngredients, checkSteps);

        // Act
        recipeRepository.save(saveRecipe);

        // Assert
        boolean recipeMatch = recipeRepository.existsByUniquenessIdentifier(checkRecipe.getUniquenessIdentifier());
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

    @Test
    public void RecipeRepository_FindByPublicId_ReturnOptionalRecipe() {
        // Arrange
        List<Ingredient> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
        List<Step> stepsList = List.of(createStep("description", 10));
        Recipe saveRecipe = createRecipe("publicId", "recipe", "igmUrl", 10, ingredientsList, stepsList);

        // Act
        recipeRepository.save(saveRecipe);

        // Retrieve the saved recipe from DB and assert
        Recipe retrievedRecipe = recipeRepository.findByPublicId(saveRecipe.getPublicId()).get();
        Assertions.assertThat(retrievedRecipe).isNotNull();
        Assertions.assertThat(retrievedRecipe.getId()).isEqualTo(saveRecipe.getId());
        Assertions.assertThat(retrievedRecipe.getIngredients()).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("name", "ingredient");
        Assertions.assertThat(retrievedRecipe.getSteps()).hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("description", "description");
    }

    @Test
    public void RecipeRepository_FindByPublicId_ReturnEmptyOptionalRecipe() {
        // Retrieve non-existent recipe from DB and assert
        Optional<Recipe> retrievedRecipesList = recipeRepository.findByPublicId("publicId ");

        Assertions.assertThat(retrievedRecipesList).isNotNull();
        Assertions.assertThat(retrievedRecipesList).isEmpty();
    }

    @Test
    public void RecipeRepository_GetRecipesByTimeAndMealType_ReturnRecipeList() {
        // Arrange
        int startTime = 0;
        int endTime = 180;
        MealType mealType = MealType.APPETIZER;
        LocalDateTime startDate = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endDate = LocalDateTime.now().toLocalDate().plusDays(1).atStartOfDay();
        Pageable pageable = PageRequest.of(0, 5);

        // Act
        recipeList.forEach(recipe -> {
            recipeRepository.save(recipe);
        });

        // Assert
        Page<Recipe> retrievedRecipesList = recipeRepository.getRecipesByTimeAndMealType(startTime, endTime, mealType,
                startDate, endDate, pageable);

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
    public void RecipeRepository_GetRecipesByTimeAndMealType_ReturnEmptyList() {
        // Arrange
        int startTime = 0;
        int endTime = 180;
        MealType mealType = MealType.APPETIZER;
        LocalDateTime startDate = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endDate = LocalDateTime.now().toLocalDate().plusDays(1).atStartOfDay();
        Pageable pageable = PageRequest.of(0, 5);

        // Assert
        Page<Recipe> retrievedRecipesList = recipeRepository.getRecipesByTimeAndMealType(startTime, endTime, mealType,
                startDate, endDate, pageable);

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
