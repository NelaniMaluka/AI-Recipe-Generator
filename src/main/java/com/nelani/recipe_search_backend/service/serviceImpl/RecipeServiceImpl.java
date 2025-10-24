package com.nelani.recipe_search_backend.service.serviceImpl;

import com.nelani.recipe_search_backend.response.RecipeResponse;
import com.nelani.recipe_search_backend.mapper.RecipeMapper;
import com.nelani.recipe_search_backend.model.DateFilter;
import com.nelani.recipe_search_backend.model.MealType;
import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.notifications.EmailService;
import com.nelani.recipe_search_backend.repository.RecipeRepository;
import com.nelani.recipe_search_backend.service.RecipeService;

import com.nelani.recipe_search_backend.util.DateRangeUtil;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecipeServiceImpl implements RecipeService {

        private final RecipeGenerator recipeGenerator;
        private final RecipeRepository recipeRepository;
        private final EmailService emailService;

        public RecipeServiceImpl(RecipeGenerator recipeGenerator, RecipeRepository recipeRepository,
                        EmailService emailService) {
                this.recipeGenerator = recipeGenerator;
                this.recipeRepository = recipeRepository;
                this.emailService = emailService;
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
                                .orElseThrow(() -> new IllegalArgumentException("Invalid recipe Id."));

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
                                .orElseThrow(() -> new IllegalArgumentException("Invalid recipe Id."));

                String subject = "AI Recipe Generator - " + recipe.getName();

                // Ingredients with quantity
                String ingredientsHtml = recipe.getIngredients().stream()
                                .map(i -> "<li>" + i.getQuantity() + " " + i.getName() + "</li>")
                                .collect(Collectors.joining());

                // Steps with estimated time
                String stepsHtml = recipe.getSteps().stream()
                                .map(s -> "<li>" + s.getDescription() + " (Estimated: " + s.getEstimatedMinutes()
                                                + " min)</li>")
                                .collect(Collectors.joining());

                String htmlContent = "<!DOCTYPE html>"
                                + "<html lang='en'>"
                                + "  <head>"
                                + "    <meta charset='UTF-8' />"
                                + "    <meta name='viewport' content='width=device-width, initial-scale=1.0' />"
                                + "    <title>AI Recipe Generator</title>"
                                + "  </head>"
                                + "  <body style='font-family: Arial, sans-serif; color: #333; background: #f9f9f9; margin: 0; padding: 0;'>"
                                + "    <div style='max-width: 500px; width: 100%; margin: auto; background: #fff'>"
                                + "      <div style='padding: 1px 20px 0 20px'>"
                                + "        <h2 style='margin: 20px 0'>AI Recipe Generator</h2>"
                                + "      </div>"
                                + "      <div style='text-align:center;'>"
                                + "        <img src='https://ai-recipe-generator-5rbk.onrender.com/images/logo.png' alt='AI Recipe Generator Logo' "
                                + "             style='width:100%;height:auto;display:block;margin-bottom:20px;'/>"
                                + "      </div>"
                                + "      <div style='padding: 0 20px 40px'>"
                                + "        <h2 style='color: #2e86c1; margin: 40px 0 20px 0'>Hi there,</h2>"
                                + "        <p style='line-height: 1.6'>"
                                + "          Your custom recipe <strong>" + recipe.getName()
                                + "</strong> has been generated by AI!"
                                + "        </p>"
                                + "        <p><strong>Meal Type:</strong> " + recipe.getMealType() + "</p>"
                                + "        <p><strong>Cook Time:</strong> " + recipe.getCookTimeMinutes()
                                + " minutes</p>"
                                + "        <h3 style='margin-top:30px; color:#2e86c1;'>Ingredients:</h3>"
                                + "        <ul style='line-height:1.6;'>" + ingredientsHtml + "</ul>"
                                + "        <h3 style='margin-top:30px; color:#2e86c1;'>Steps:</h3>"
                                + "        <ol style='line-height:1.6;'>" + stepsHtml + "</ol>"
                                + "        <hr style='margin: 30px 0; border: none; border-top: 1px solid #ccc' />"
                                + "        <p style='font-size: 13px; color: #666; text-align: center'>"
                                + "          Need help? Visit our"
                                + "          <a href='#' style='color: #2e86c1; text-decoration: none'>Help Center</a>"
                                + "          or reply to this email."
                                + "        </p>"
                                + "        <p style='font-size: 12px; color: #999; text-align: center; margin-top: 15px;'>"
                                + "          AI Recipe Generator – Where every dish is unique."
                                + "        </p>"
                                + "      </div>"
                                + "    </div>"
                                + "  </body>"
                                + "</html>";

                // Email the recipe to the provided email
                emailService.sendEmail(email, subject, htmlContent);
        }

}
