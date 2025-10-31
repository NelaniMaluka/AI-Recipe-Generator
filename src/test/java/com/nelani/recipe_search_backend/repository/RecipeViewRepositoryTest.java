package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.*;
import com.nelani.recipe_search_backend.security.ApplicationUserRole;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
public class RecipeViewRepositoryTest {

    @Autowired
    private RecipeViewRepository recipeViewRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Recipe recipe;
    private RecipeView recipeView;

    @BeforeEach
    public void init() {
        user = User.builder()
                .firstname("firstname")
                .lastname("lastname")
                .email("test-email@test.co.za")
                .publicId("publicId")
                .role(ApplicationUserRole.USER)
                .provider(Provider.LOCAL)
                .password("Password@123")
                .build();

        // Arrange
        List<Ingredient> ingredientsList = List.of(createIngredient("ingredient", "4 cups"));
        List<Step> stepsList = List.of(createStep("description", 10));
        recipe = createRecipe("publicId", "recipe", "igmUrl", 10, ingredientsList, stepsList);

        recipeView = RecipeView.builder()
                .recipe(recipe)
                .user(user)
                .build();
    }

    @Test
    public void RecipeViewRepository_FindByUserAndRecipe_ReturnOptionalUserView() {
        // Arrange
        userRepository.save(user);
        recipeRepository.save(recipe);
        recipeViewRepository.save(recipeView);

        // Assert
        var optionalView = recipeViewRepository.findByUserAndRecipe(user, recipe);
        Assertions.assertThat(optionalView).isPresent();
        RecipeView view = optionalView.get();
        Assertions.assertThat(view.getId()).isEqualTo(recipeView.getId());
        Assertions.assertThat(view.getUser().getId()).isEqualTo(user.getId());
        Assertions.assertThat(view.getRecipe().getId()).isEqualTo(recipe.getId());
    }

    @Test
    public void RecipeViewRepository_DeleteByUser_ReturnUserViewList() {
        // Arrange
        userRepository.save(user);
        recipeRepository.save(recipe);
        recipeViewRepository.save(recipeView);

        // Assert
        var result = recipeViewRepository.deleteByUser(user);
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    public void RecipeViewRepository_DeleteByRecipe_ReturnUserViewList() {
        // Arrange
        userRepository.save(user);
        recipeRepository.save(recipe);
        recipeViewRepository.save(recipeView);

        // Assert
        var result = recipeViewRepository.deleteByRecipe(recipe);
        Assertions.assertThat(result).isEqualTo(1);
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
