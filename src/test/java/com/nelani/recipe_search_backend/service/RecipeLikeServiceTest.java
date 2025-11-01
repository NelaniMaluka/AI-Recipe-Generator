package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.model.*;
import com.nelani.recipe_search_backend.repository.RecipeLikeRepository;
import com.nelani.recipe_search_backend.repository.RecipeRepository;
import com.nelani.recipe_search_backend.repository.UserRepository;
import com.nelani.recipe_search_backend.security.ApplicationUserRole;
import com.nelani.recipe_search_backend.service.serviceImpl.RecipeLikeServiceImpl;
import com.nelani.recipe_search_backend.sockets.RecipeSocket;
import com.nelani.recipe_search_backend.sockets.UserSocket;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RecipeLikeServiceTest {

        @Mock
        private RecipeLikeRepository recipeLikeRepository;

        @Mock
        private RecipeRepository recipeRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private UserSocket userSocket;

        @Mock
        private RecipeSocket recipeSocket;

        @InjectMocks
        private RecipeLikeServiceImpl recipeLikeService;

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
        public void RecipeLikeService_GetRecipeLikes_ReturnsCount() {
                when(recipeRepository.findByPublicId(any(String.class)))
                                .thenReturn(Optional.of(recipe));
                when(recipeLikeRepository.countByRecipe(any(Recipe.class)))
                                .thenReturn(1L);

                long count = recipeLikeService.getRecipeLikes("publicId");
                Assertions.assertThat(count).isEqualTo(1);
        }

        @Test
        public void RecipeLikeService_AddRecipeLike_ReturnsLikeResponse() {
                // Mock authentication
                var authentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getName()).thenReturn("test-email@test.co.za");

                var securityContext = org.mockito.Mockito
                                .mock(org.springframework.security.core.context.SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                org.springframework.security.core.context.SecurityContextHolder.setContext(securityContext);

                // Arrange mocks
                when(userRepository.findByEmail(any(String.class)))
                                .thenReturn(Optional.of(user));
                when(recipeRepository.findByPublicId(any(String.class)))
                                .thenReturn(Optional.of(recipe));
                when(recipeLikeRepository.findByUserAndRecipe(any(User.class), any(Recipe.class)))
                                .thenReturn(Optional.empty()); // <--- no existing like
                when(recipeLikeRepository.countByRecipe(any(Recipe.class)))
                                .thenReturn(1L);
                when(recipeLikeRepository.findRecentLikedRecipeIdsByUser(any(User.class), any(Pageable.class)))
                                .thenReturn(List.of(recipe.getPublicId()));

                // Act
                var response = recipeLikeService.addRecipeLike(recipe.getPublicId(), 0, 500);

                // Assert
                Assertions.assertThat(response.getMessage()).isEqualTo("Like added successfully");
                Assertions.assertThat(response.getPublicId()).isEqualTo(recipe.getPublicId());
                Assertions.assertThat(response.getLikeCount()).isEqualTo(1);
                Assertions.assertThat(response.getLikedRecipes()).containsExactly(recipe.getPublicId());
        }

        @Test
        public void RecipeLikeService_RemoveRecipeLike_Success() {
                // Mock authentication
                var authentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getName()).thenReturn("test-email@test.co.za");

                var securityContext = org.mockito.Mockito
                                .mock(org.springframework.security.core.context.SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                // Arrange mocks
                when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
                when(recipeRepository.findByPublicId(any(String.class))).thenReturn(Optional.of(recipe));
                when(recipeLikeRepository.findByUserAndRecipe(any(User.class), any(Recipe.class)))
                                .thenReturn(Optional.of(recipeLike));
                when(recipeLikeRepository.findRecentLikedRecipeIdsByUser(any(User.class), any(Pageable.class)))
                                .thenReturn(List.of());
                when(recipeLikeRepository.countByRecipe(any(Recipe.class))).thenReturn(0L);

                // Act
                recipeLikeService.removeRecipeLike(recipe.getPublicId());

                // Verify deletion and socket calls
                verify(recipeLikeRepository).delete(recipeLike);
                verify(recipeSocket).sendUpdatedRecipeLikes(recipe, 0L);
                verify(userSocket).sendUpdatedUserLikes(user, List.of());
        }

        @Test
        public void RecipeLikeService_GetUserLikes_ReturnsLikedRecipeIds() {
                // Mock authentication
                var authentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getName()).thenReturn("test-email@test.co.za");

                var securityContext = org.mockito.Mockito
                                .mock(org.springframework.security.core.context.SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                // Arrange mocks
                when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
                when(recipeLikeRepository.findRecentLikedRecipeIdsByUser(any(User.class), any(Pageable.class)))
                                .thenReturn(List.of("recipe1", "recipe2", "recipe3"));

                // Act
                List<String> likedRecipes = recipeLikeService.getUserLikes();

                // Assertions
                Assertions.assertThat(likedRecipes).containsExactly("recipe1", "recipe2", "recipe3");
        }

        @Test
        public void RecipeLikeService_FallbackUserLikedCheck_ReturnsFalseIfNotLiked() {
                // Mock authentication
                var authentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getName()).thenReturn("test-email@test.co.za");

                var securityContext = org.mockito.Mockito
                                .mock(org.springframework.security.core.context.SecurityContext.class);
                when(securityContext.getAuthentication()).thenReturn(authentication);
                SecurityContextHolder.setContext(securityContext);

                // Arrange mocks
                when(userRepository.findByEmail(any(String.class))).thenReturn(Optional.of(user));
                when(recipeRepository.findByPublicId(any(String.class))).thenReturn(Optional.of(recipe));
                when(recipeLikeRepository.existsByUserAndRecipe(any(User.class), any(Recipe.class))).thenReturn(false);

                // Act
                boolean liked = recipeLikeService.fallbackUserLikedCheck(recipe.getPublicId());

                // Assertions
                Assertions.assertThat(liked).isFalse();
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
