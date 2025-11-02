package com.nelani.recipe_search_backend.service.serviceImpl;

import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.model.*;
import com.nelani.recipe_search_backend.repository.RecipeLikeRepository;
import com.nelani.recipe_search_backend.repository.RecipeViewRepository;
import com.nelani.recipe_search_backend.response.RecipeResponse;
import com.nelani.recipe_search_backend.mapper.RecipeMapper;
import com.nelani.recipe_search_backend.notifications.EmailService;
import com.nelani.recipe_search_backend.repository.RecipeRepository;
import com.nelani.recipe_search_backend.service.RecipeService;

import com.nelani.recipe_search_backend.sockets.RecipeSocket;
import com.nelani.recipe_search_backend.util.DateRangeUtil;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RecipeServiceImpl implements RecipeService {

        private final RecipeGenerator recipeGenerator;
        private final RecipeRepository recipeRepository;
        private final RecipeViewRepository recipeViewRepository;
        private final RecipeLikeRepository recipeLikeRepository;
        private final EmailService emailService;
        private final RecipeSocket recipeSocket;

        public RecipeServiceImpl(RecipeGenerator recipeGenerator, RecipeRepository recipeRepository,
                        RecipeViewRepository recipeViewRepository, RecipeLikeRepository recipeLikeRepository,
                        EmailService emailService, RecipeSocket recipeSocket) {
                this.recipeGenerator = recipeGenerator;
                this.recipeRepository = recipeRepository;
                this.recipeViewRepository = recipeViewRepository;
                this.recipeLikeRepository = recipeLikeRepository;
                this.emailService = emailService;
                this.recipeSocket = recipeSocket;
        }

        /**
         * Retrieves a recipe by its public ID and returns a detailed response.
         *
         * @param publicId the unique public identifier of the recipe
         * @return a RecipeResponse containing all recipe details
         * @throws IllegalArgumentException if no recipe is found with the given public
         *                                  ID
         */
        @Override
        @Cacheable(value = "recipe", key = "#publicId")
        @Transactional
        public RecipeResponse getRecipe(String publicId) {
                // Fetch the Recipe
                Recipe recipe = recipeRepository.findByPublicId(publicId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Recipe not found with ID: " + publicId));

                // Return a RecipeDto to the user
                return RecipeMapper.mapRecipeWithAllDetails(recipe);
        }

        /**
         * Searches for recipes based on a keyword and returns paginated results.
         * Provides immediate fallback results from the database while triggering
         * asynchronous AI-based recipe generation for future queries.
         *
         * @param searchWord the keyword to search recipes
         * @param page       the page number for pagination (0-based)
         * @param size       the number of recipes per page
         * @return a list of RecipeResponse objects with minimal details
         */
        @Override
        @Cacheable(value = "AI recipes", key = "#searchWord")
        @Transactional
        public List<RecipeResponse> getRecipes(String searchWord, int page, int size) {
                // Fetch fallback immediately
                Pageable pageable = PageRequest.of(page, size);
                List<Recipe> fallbackRecipes = recipeRepository.searchRecipes(searchWord, pageable);
                List<RecipeResponse> fallbackRecipesDto = fallbackRecipes.stream()
                                .map(RecipeMapper::mapRecipeWithMinimalDetails)
                                .toList();

                // Trigger async AI generation for DB population
                recipeGenerator.generateAndSaveRecipes(searchWord);

                // ️Return fallback instantly
                return fallbackRecipesDto;
        }

        /**
         * Retrieves recipes filtered by preparation time, meal type, and date range,
         * with support for pagination. Results are cached to improve performance.
         *
         * @param startTime  minimum preparation time in minutes
         * @param endTime    maximum preparation time in minutes
         * @param mealType   type of meal (e.g., APPETIZER, MAIN, DESSERT)
         * @param dateFilter filter to limit recipes to a specific date range
         * @param page       zero-based page index for pagination
         * @param size       number of recipes per page
         * @return a list of RecipeResponse objects with minimal details
         */
        @Override
        @Cacheable(value = "recipes", key = "#startTime + '_' + #endTime + '_' + #mealType + '_' + #dateFilter + '_' + #page + '_' + #size")
        @Transactional
        public List<RecipeResponse> getRecipesByTimeAndMealType(int startTime, int endTime, MealType mealType,
                        DateFilter dateFilter, int page, int size) {
                LocalDateTime[] range = DateRangeUtil.getDateRange(dateFilter);
                LocalDateTime startDate = range[0];
                LocalDateTime endDate = range[1];

                // fetch the recipes
                Pageable pageable = PageRequest.of(page, size);
                Page<Recipe> recipesPage = recipeRepository.getRecipesByTimeAndMealType(
                                startTime, endTime, mealType, startDate, endDate, pageable);

                return recipesPage.stream()
                                .map(RecipeMapper::mapRecipeWithMinimalDetails)
                                .toList();
        }

        /**
         * Sends a detailed HTML email of a specific recipe to a given email address.
         *
         * <p>
         * The email includes the recipe name, meal type, cook time, ingredients
         * (with quantities), and step-by-step instructions (with estimated times),
         * formatted with a branded HTML template.
         * </p>
         *
         * @param email    the recipient's email address
         * @param publicId the unique public ID of the recipe to email
         * @throws IllegalArgumentException if the recipe with the given publicId does
         *                                  not exist
         */
        @Override
        @Transactional
        public void emailRecipe(String email, String publicId) {
                // Fetch the Recipe
                Recipe recipe = recipeRepository.findByPublicId(publicId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Recipe not found with ID: " + publicId));

                emailService.sendRecipeEmail(email, recipe);
        }

        /**
         * Updates a recipe identified by its public ID.
         * <p>
         * Updates recipe fields, ingredients, and steps. Saves changes to the database,
         * evicts cache, and sends the updated recipe via WebSocket.
         *
         * @param publicId the recipe's public ID
         * @param dto      DTO containing updated recipe details
         * @return the updated {@link RecipeResponse}
         * @throws IllegalArgumentException if the recipe does not exist
         */
        @Caching(evict = {
                        @CacheEvict(value = "recipe", key = "#publicId"),
                        @CacheEvict(value = "recipes", allEntries = true)
        })
        @Override
        @Transactional
        public RecipeResponse updateRecipe(String publicId, RecipeDto dto) {
                // Fetch the recipe or throw exception if not found
                Recipe recipe = recipeRepository.findByPublicId(publicId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Recipe not found with ID: " + publicId));

                // Update basic fields
                recipe.setName(dto.name());
                recipe.setImageUrl(dto.imageUrl());
                recipe.setMealType(dto.mealType());
                recipe.setCookTimeMinutes(dto.cookTimeMinutes());

                // Map ingredients and steps
                recipe.setIngredients(dto.ingredients().stream()
                                .map(i -> Ingredient.builder()
                                                .name(i.name())
                                                .quantity(i.quantity())
                                                .recipe(recipe)
                                                .build())
                                .toList());

                recipe.setSteps(dto.steps().stream()
                                .map(s -> Step.builder()
                                                .description(s.description())
                                                .estimatedMinutes(s.estimatedMinutes())
                                                .recipe(recipe)
                                                .build())
                                .toList());

                // Persist updates
                recipeRepository.save(recipe);

                // Map to response and push via WebSocket
                RecipeResponse response = RecipeMapper.mapRecipeWithAllDetails(recipe);
                recipeSocket.sendUpdatedRecipe(response);

                return response;
        }

        /**
         * Deletes a recipe identified by its public ID.
         * <p>
         * Removes the recipe from the database and evicts its cache entry.
         *
         * @param publicId the recipe's public ID
         * @throws IllegalArgumentException if the recipe does not exist
         */
        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "recipe", key = "#publicId"),
                        @CacheEvict(value = "recipes", allEntries = true)
        })
        public void deleteRecipe(String publicId) {
                // Fetch the recipe or throw exception if not found
                Recipe recipe = recipeRepository.findByPublicId(publicId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Recipe not found with ID: " + publicId));

                // Delete the recipe
                recipeViewRepository.deleteByRecipe(recipe);
                recipeLikeRepository.deleteByRecipe(recipe);
                recipeRepository.delete(recipe);
        }

}
