package com.nelani.recipe_search_backend.sockets.socketDoc;

import com.nelani.recipe_search_backend.response.RecipeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "WebSocket", description = "Documentation for WebSocket endpoints")
public class RecipeSocketDocsController {

        @GetMapping("/ws/recipes/{searchTerm}")
        @Operation(summary = "WebSocket subscription for AI recipe search results", description = "Subscribe via WebSocket to receive real-time AI-generated recipes for a search term.\n\n"
                        + "**WebSocket URL:** ws://localhost:8080/ws\n"
                        + "**Topic:** /topic/recipes/{searchTerm}\n"
                        + "**Payload:** List of RecipeResponse objects", responses = {
                                        @ApiResponse(description = "Example RecipeResponse payload", content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = RecipeResponse.class, example = "[{\"id\":\"1\",\"name\":\"Spaghetti Carbonara\",\"mealType\":\"Dinner\",\"cookTimeMinutes\":25,\"ingredients\":[{\"name\":\"Spaghetti\",\"quantity\":\"200g\"}],\"steps\":[{\"description\":\"Boil spaghetti\",\"estimatedMinutes\":10}]}]")))
                        })
        public String wsRecipeInfo(@PathVariable String searchTerm) {
                return "This endpoint is for Swagger documentation only. "
                                + "Subscribe to /topic/recipes/" + searchTerm
                                + " via WebSocket to receive RecipeResponse updates.";
        }

        @GetMapping("/ws/recipes/{publicId}")
        @Operation(summary = "WebSocket subscription for updated recipe details", description = "Subscribe via WebSocket to receive real-time updates when a recipe is updated.\n\n"
                        +
                        "**WebSocket URL:** ws://localhost:8080/ws\n" +
                        "**Topic:** /topic/recipes/{publicId}\n" +
                        "**Payload:** RecipeResponse object", responses = {
                                        @ApiResponse(description = "Example RecipeResponse payload", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecipeResponse.class)))
                        })
        public String wsUpdatedRecipeInfo(@PathVariable String publicId) {
                return "This endpoint is for Swagger documentation only. " +
                                "Subscribe to /topic/recipes/" + publicId
                                + " via WebSocket to receive RecipeResponse updates.";
        }

}
