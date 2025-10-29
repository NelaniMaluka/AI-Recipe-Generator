package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.response.MessageResponse;
import com.nelani.recipe_search_backend.response.RecipeResponse;
import com.nelani.recipe_search_backend.model.DateFilter;
import com.nelani.recipe_search_backend.model.MealType;
import com.nelani.recipe_search_backend.service.RecipeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Recipes Controller", description = "Endpoints for searching, filtering, and emailing recipes, including meal types and date filters.")
public class RecipeController {

        private final RecipeService recipeService;

        public RecipeController(RecipeService recipeService) {
                this.recipeService = recipeService;
        }

        @Operation(summary = "Retrieve all available meal types", description = "Returns a list of predefined meal types (e.g., Breakfast, Lunch, Dinner).")
        @ApiResponse(responseCode = "200", description = "Meal types retrieved successfully")
        @GetMapping("/public/recipe/meal-types")
        @Cacheable("meal-types")
        public ResponseEntity<?> getMealTypes() {
                return ResponseEntity.ok(MealType.values());
        }

        @Operation(summary = "Retrieve all available date filters", description = "Returns a list of predefined date filters (e.g., Today, This Week, This Month).")
        @ApiResponse(responseCode = "200", description = "Date filters retrieved successfully")
        @GetMapping("/public/recipe/date-filters")
        @Cacheable("date-filters")
        public ResponseEntity<?> getDateFilters() {
                return ResponseEntity.ok(DateFilter.values());
        }

        @Operation(summary = "Get a specific recipe by its public ID", description = "Retrieves detailed information about a recipe based on its public identifier.")
        @ApiResponse(responseCode = "200", description = "Recipe retrieved successfully")
        @GetMapping("/public/recipe/{publicId}")
        public ResponseEntity<?> getRecipe(
                        @PathVariable @NotBlank(message = "Recipe Id cannot be blank") String publicId) {
                RecipeResponse recipe = recipeService.getRecipe(publicId);
                return ResponseEntity.ok(recipe);
        }

        @Operation(summary = "Search recipes by keyword", description = "Returns a paginated list of recipes matching the provided search keyword.")
        @ApiResponse(responseCode = "200", description = "Recipes retrieved successfully")
        @GetMapping("/public/recipe/search")
        public ResponseEntity<?> getRecipes(
                        @RequestParam @NotBlank(message = "Search word cannot be blank") String searchWord,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size) {
                List<RecipeResponse> recipes = recipeService.getRecipes(searchWord, page, size);
                return ResponseEntity.ok(recipes);
        }

        @Operation(summary = "Filter recipes by time, meal type, and date", description = "Returns a paginated list of recipes filtered by preparation time, meal type, and date category.")
        @ApiResponse(responseCode = "200", description = "Filtered recipes retrieved successfully")
        @GetMapping("/public/recipes")
        public ResponseEntity<?> getRecipesByTimeAndMealType(
                        @RequestParam(defaultValue = "0") int startTime,
                        @RequestParam(defaultValue = "180") int endTime,
                        @RequestParam(required = false) MealType mealType,
                        @RequestParam(defaultValue = "ALL") DateFilter dateFilter,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
                List<RecipeResponse> recipes = recipeService.getRecipesByTimeAndMealType(startTime, endTime, mealType,
                                dateFilter, page, size);
                return ResponseEntity.ok(recipes);
        }

        @Operation(summary = "Send recipe details via email", description = "Emails the specified recipe to the provided email address.")
        @ApiResponse(responseCode = "200", description = "Recipe email sent successfully")
        @PostMapping("/public/recipe/email-recipe")
        public ResponseEntity<?> emailRecipe(
                        @RequestParam @NotBlank(message = "Email cannot be blank") @Email(message = "Invalid email format") String email,
                        @RequestParam @NotBlank(message = "Recipe Id cannot be blank") String publicId) {
                recipeService.emailRecipe(email, publicId);
                return ResponseEntity.ok(new MessageResponse("Recipe has been successfully sent to " + email, null));
        }

        @Operation(summary = "Update an existing recipe", description = "Updates the recipe details. All fields are required.")
        @ApiResponse(responseCode = "200", description = "Recipe updated successfully")
        @PutMapping("/admin/recipe/{publicId}")
        @PreAuthorize("hasAuthority('recipe:write')")
        public ResponseEntity<?> updateRecipe(
                        @PathVariable String publicId,
                        @Valid @RequestBody RecipeDto recipeDto) {
                RecipeResponse response = recipeService.updateRecipe(publicId, recipeDto);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Delete a recipe", description = "Deletes the recipe with the specified public ID.")
        @ApiResponse(responseCode = "204", description = "Recipe deleted successfully")
        @DeleteMapping("/admin/recipe/{publicId}")
        @PreAuthorize("hasAuthority('recipe:delete')")
        public ResponseEntity<?> deleteRecipe(@PathVariable String publicId) {
                recipeService.deleteRecipe(publicId);
                return ResponseEntity.noContent().build();
        }

}
