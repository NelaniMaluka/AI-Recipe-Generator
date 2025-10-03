package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.model.DateFilter;
import com.nelani.recipe_search_backend.model.MealType;
import com.nelani.recipe_search_backend.service.RecipeService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipe")
@Validated
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/meal-types")
    @Cacheable("meal-types")
    public ResponseEntity<?> getMealTypes() {
        return ResponseEntity.ok(MealType.values());
    }

    @GetMapping("/date-filters")
    @Cacheable("date-filters")
    public ResponseEntity<?> getDateFilters() {
        return ResponseEntity.ok(DateFilter.values());
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<?> getRecipe(@PathVariable @NotBlank(message = "Recipe Id cannot be blank") String publicId) {
        RecipeDto recipe = recipeService.getRecipe(publicId);
        return ResponseEntity.ok(recipe);
    }

    @GetMapping
    public ResponseEntity<?> getRecipes(
            @RequestParam @NotBlank(message = "Search word cannot be blank") String searchWord,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        List<RecipeDto> recipes = recipeService.getRecipes(searchWord, page, size);
        return ResponseEntity.ok(recipes);
    }

    @GetMapping("/all-recipes")
    public ResponseEntity<?> getRecipesByTimeAndMealType(
            @RequestParam(defaultValue = "0") int startTime,
            @RequestParam(defaultValue = "180") int endTime,
            @RequestParam(required = false) MealType mealType,
            @RequestParam(defaultValue = "ALL") DateFilter dateFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<RecipeDto> recipes = recipeService.getRecipesByTimeAndMealType(startTime, endTime, mealType, dateFilter, page, size);
        return ResponseEntity.ok(recipes);
    }

    @PostMapping("/email-recipe")
    public ResponseEntity<?> emailRecipe(
            @RequestParam
            @NotBlank(message = "Email cannot be blank")
            @Email(message = "Invalid email format") String email,

            @NotBlank(message = "Recipe Id cannot be blank") String publicId
    ) {
        recipeService.emailRecipe(email, publicId);
        return ResponseEntity.ok("Recipe has been successfully sent to " + email);
    }

}
