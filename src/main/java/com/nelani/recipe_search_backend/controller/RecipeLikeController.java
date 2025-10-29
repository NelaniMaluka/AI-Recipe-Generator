package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.response.LikeResponse;
import com.nelani.recipe_search_backend.service.RecipeLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Recipe Likes", description = "Endpoints for managing recipe likes and user like actions")
public class RecipeLikeController {

        private final RecipeLikeService recipeLikeService;

        public RecipeLikeController(RecipeLikeService recipeLikeService) {
                this.recipeLikeService = recipeLikeService;
        }

        @Operation(summary = "Get total likes for a recipe", description = "Retrieves the total number of likes for a public recipe by its public ID.", responses = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved like count", content = @Content(schema = @Schema(example = "{\"likes\": 42}")))
        })
        @GetMapping("/public/recipes/{publicId}/likes")
        public ResponseEntity<?> getRecipeLikes(
                        @Parameter(description = "Unique public identifier of the recipe", example = "abc123") @PathVariable @NotBlank(message = "Public ID cannot be blank") String publicId) {
                long count = recipeLikeService.getRecipeLikes(publicId);
                return ResponseEntity.ok(Map.of("likes", count));
        }

        @Operation(summary = "Add a like to a recipe", description = "Allows an authenticated user to like a specific recipe using its public ID.")
        @ApiResponse(responseCode = "201", description = "Like added successfully", content = @Content(schema = @Schema(example = "{\"message\": \"Like added successfully\", \"publicId\": \"abc123\"}")))
        @PostMapping("/user/recipes/{publicId}/likes")
        @PreAuthorize("hasAuthority('user:write')")
        public ResponseEntity<?> addRecipeLike(
                        @Parameter(description = "Unique public identifier of the recipe", example = "abc123") @PathVariable @NotBlank(message = "Public ID cannot be blank") String publicId,

                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "500") int size) {
                LikeResponse response = recipeLikeService.addRecipeLike(publicId, page, size);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(Map.of("message", "Like added successfully", "publicId", publicId));
        }

        @Operation(summary = "Remove a like from a recipe", description = "Allows an authenticated user to remove a like they previously added to a recipe.")
        @ApiResponse(responseCode = "204", description = "Like removed successfully")
        @DeleteMapping("/user/recipes/{publicId}/likes")
        @PreAuthorize("hasAuthority('user:write')")
        public ResponseEntity<?> removeRecipeLike(
                        @Parameter(description = "Unique public identifier of the recipe", example = "abc123") @PathVariable @NotBlank(message = "Public ID cannot be blank") String publicId) {
                recipeLikeService.removeRecipeLike(publicId);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Get all liked recipes for a user", description = "Returns a list of public IDs for all recipes liked by the authenticated user.")
        @ApiResponse(responseCode = "200", description = "Successfully retrieved liked recipes", content = @Content(schema = @Schema(example = "{\"likedRecipes\": [\"abc123\", \"xyz789\"]}")))
        @GetMapping("/user/likes")
        @PreAuthorize("hasAuthority('user:read')")
        public ResponseEntity<?> getUserLikes() {
                List<String> response = recipeLikeService.getUserLikes();
                return ResponseEntity.ok(Map.of("likedRecipes", response));
        }

        @Operation(summary = "Check if a user has liked a recipe", description = "Checks whether the authenticated user has liked a particular recipe by its public ID.")
        @ApiResponse(responseCode = "200", description = "Returns whether the user liked the recipe", content = @Content(schema = @Schema(example = "{\"liked\": true}")))
        @GetMapping("/user/recipes/{publicId}/liked")
        @PreAuthorize("hasAuthority('user:read')")
        public ResponseEntity<?> userLikedCheck(
                        @Parameter(description = "Unique public identifier of the recipe", example = "abc123") @PathVariable @NotBlank(message = "Public ID cannot be blank") String publicId) {
                boolean response = recipeLikeService.fallbackUserLikedCheck(publicId);
                return ResponseEntity.ok(Map.of("liked", response));
        }
}
