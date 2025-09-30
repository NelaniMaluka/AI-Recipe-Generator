package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.dto.RecipeDto;
import com.nelani.recipe_search_backend.service.RecipeService;
import jakarta.validation.constraints.NotBlank;
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

    @GetMapping
    public ResponseEntity<?> getRecipes(
            @RequestParam @NotBlank(message = "Search word cannot be blank") String searchWord,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<RecipeDto> recipes = recipeService.getRecipes(searchWord, page, size);
        return ResponseEntity.ok(recipes);
    }

}
