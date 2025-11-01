package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.model.*;
import com.nelani.recipe_search_backend.repository.RecipeRepository;
import com.nelani.recipe_search_backend.repository.RecipeViewRepository;
import com.nelani.recipe_search_backend.repository.UserRepository;
import com.nelani.recipe_search_backend.security.ApplicationUserRole;
import com.nelani.recipe_search_backend.service.serviceImpl.RecipeViewServiceImpl;
import com.nelani.recipe_search_backend.sockets.RecipeSocket;
import com.nelani.recipe_search_backend.sockets.UserSocket;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RecipeViewServiceTest {

    @Mock
    private RecipeViewRepository recipeViewRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSocket userSocket;

    @Mock
    private RecipeSocket recipeSocket;

    @InjectMocks
    private RecipeViewServiceImpl recipeViewService;

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
    public void RecipeViewService_GetRecipeLikes_ReturnsCount() {
        when(recipeRepository.findByPublicId(any(String.class)))
                .thenReturn(Optional.of(recipe));

        long count = recipeViewService.getRecipeViews("publicId");
        Assertions.assertThat(count).isEqualTo(0);
    }

    @Test
    void RecipeService_AddView_AddsViewIfNotExists() {
        // Mock authentication
        var authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-email@test.co.za");

        var securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Arrange mocks
        when(recipeRepository.findByPublicId(any(String.class))).thenReturn(Optional.of(recipe));
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
        when(recipeViewRepository.findByUserAndRecipe(any(), any())).thenReturn(Optional.empty());

        // Act
        recipeViewService.addView(recipe.getPublicId());

        // Assert
        verify(recipeViewRepository).save(any(RecipeView.class));
        verify(recipeRepository).save(recipe);
        verify(recipeSocket).sendUpdatedRecipeViews(recipe);
    }

    @Test
    public void RecipeService_AddView_DoesNotSaveDuplicateView() {
        // Mock authentication
        var authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test-email@test.co.za");

        var securityContext = mock(org.springframework.security.core.context.SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Arrange mocks: view already exists
        when(recipeRepository.findByPublicId(any(String.class))).thenReturn(Optional.of(recipe));
        when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));

        // A Mock view already exists for this user and recipe
        RecipeView existingView = RecipeView.builder().user(user).recipe(recipe).build();
        when(recipeViewRepository.findByUserAndRecipe(user, recipe))
                .thenReturn(Optional.of(existingView));

        // Act
        recipeViewService.addView(recipe.getPublicId());

        // Verify save is NOT called for duplicate view
        verify(recipeViewRepository, never()).save(any(RecipeView.class));

        // Recipe save should also NOT be called
        verify(recipeRepository, never()).save(recipe);

        // Socket still called
        verify(recipeSocket).sendUpdatedRecipeViews(recipe);
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
