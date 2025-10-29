package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.service.RecipeViewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "User Views", description = "Endpoints for retrieving and recording recipe view counts")
public class RecipeViewsController {

        private final RecipeViewsService viewsService;

        public RecipeViewsController(RecipeViewsService viewsService) {
                this.viewsService = viewsService;
        }

        @Operation(summary = "Get recipe view count", description = "Fetches the total number of times a recipe has been viewed.")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved view count", content = @Content(mediaType = "application/json", schema = @Schema(example = "{\"views\": 124}")))
        @GetMapping("/public/recipes/{publicId}/views")
        public ResponseEntity<?> getRecipeViews(
                        @Parameter(description = "The public ID of the recipe", required = true, example = "abc123") @PathVariable @NotBlank(message = "Recipe Id cannot be blank") String publicId) {

                long result = viewsService.getRecipeViews(publicId);
                return ResponseEntity.ok(Map.of("views", result));
        }

        @Operation(summary = "Add a view to a recipe", description = "Increments the recipe’s total view count and records a user view if the user is logged in.")
        @ApiResponse(responseCode = "204", content = @Content)
        @PostMapping("/public/recipes/{publicId}/views")
        public ResponseEntity<?> addView(
                        @Parameter(description = "The public ID of the recipe", required = true, example = "abc123") @PathVariable @NotBlank(message = "Recipe Id cannot be blank") String publicId) {

                viewsService.addView(publicId);
                return ResponseEntity.noContent().build();
        }
}
