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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
public class RecipeLikeRepositoryTest {

    @Autowired
    private RecipeLikeRepository recipeLikeRepository;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Recipe recipe;
    private RecipeLike recipeLike;

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

        recipeLike = RecipeLike.builder()
                .recipe(recipe)
                .user(user)
                .build();
    }

    @Test
    public void RecipeLikeRepository_CountByRecipe_ReturnLong() {
        // Arrange
        userRepository.save(user);
        recipeRepository.save(recipe);
        recipeLikeRepository.save(recipeLike);

        // Assert
        var result = recipeLikeRepository.countByRecipe(recipe);
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    public void RecipeLikeRepository_ExistsByUserAndRecipe_ReturnTrue() {
        // Arrange
        userRepository.save(user);
        recipeRepository.save(recipe);
        recipeLikeRepository.save(recipeLike);

        // Assert
        var result = recipeLikeRepository.existsByUserAndRecipe(user, recipe);
        Assertions.assertThat(result).isTrue();
    }

    @Test
    public void RecipeLikeRepository_FindByUserAndRecipe_ReturnOptionalUserLike() {
        // Arrange
        userRepository.save(user);
        recipeRepository.save(recipe);
        recipeLikeRepository.save(recipeLike);

        // Assert
        var optionalLike = recipeLikeRepository.findByUserAndRecipe(user, recipe);
        Assertions.assertThat(optionalLike).isPresent();
        RecipeLike like = optionalLike.get();
        Assertions.assertThat(like.getId()).isEqualTo(recipeLike.getId());
        Assertions.assertThat(like.getUser().getId()).isEqualTo(user.getId());
        Assertions.assertThat(like.getRecipe().getId()).isEqualTo(recipe.getId());
    }

    @Test
    public void RecipeLikeRepository_FindRecentLikedRecipeIdsByUser_ReturnUserLikeList() {
        // Arrange
        userRepository.save(user);
        recipeRepository.save(recipe);
        recipeLikeRepository.save(recipeLike);

        // Assert
        Pageable page = PageRequest.of(0, 500);
        var likeList = recipeLikeRepository.findRecentLikedRecipeIdsByUser(user, page);
        Assertions.assertThat(likeList.size()).isEqualTo(1);
        Assertions.assertThat(likeList.get(0)).isEqualTo(recipe.getPublicId());
    }

    @Test
    public void RecipeLikeRepository_DeleteByUser_ReturnUserLikeList() {
        // Arrange
        userRepository.save(user);
        recipeRepository.save(recipe);
        recipeLikeRepository.save(recipeLike);

        // Assert
        var result = recipeLikeRepository.deleteByUser(user);
        Assertions.assertThat(result).isEqualTo(1);
    }

    @Test
    public void RecipeLikeRepository_DeleteByRecipe_ReturnUserLikeList() {
        // Arrange
        userRepository.save(user);
        recipeRepository.save(recipe);
        recipeLikeRepository.save(recipeLike);

        // Assert
        var result = recipeLikeRepository.deleteByRecipe(recipe);
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
