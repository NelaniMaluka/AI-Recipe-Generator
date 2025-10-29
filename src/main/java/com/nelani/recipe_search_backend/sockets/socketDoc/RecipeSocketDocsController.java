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

/**
 * Swagger documentation controller for WebSocket endpoints.
 * <p>
 * These endpoints are not callable via HTTP — they exist purely for documenting
 * WebSocket topics in Swagger UI.
 */
@RestController
@Tag(name = "WebSocket", description = "WebSocket endpoints for real-time recipe updates")
public class RecipeSocketDocsController {

        @GetMapping("/ws/recipes/search/{term}")
        @Operation(summary = "WebSocket: AI recipe search results", description = """
                        Subscribe via WebSocket to receive AI-generated recipe search results in real time.

                        **WebSocket URL:** ws://localhost:8080/ws
                        **Topic:** /topic/recipes/search/{term}
                        **Payload:** List of RecipeResponse objects
                        """, responses = {
                        @ApiResponse(description = "Example RecipeResponse payload", content = @Content(mediaType = "application/json", schema = @Schema(type = "array", implementation = RecipeResponse.class, example = "[{\"publicId\":\"abc123\",\"name\":\"Spaghetti Carbonara\",\"mealType\":\"Dinner\",\"cookTimeMinutes\":25}]")))
        })
        public String wsRecipeSearchInfo(@PathVariable String term) {
                return "Subscribe to /topic/recipes/search/" + term
                                + " via WebSocket to receive real-time recipe search results.";
        }

        @GetMapping("/ws/recipes/{publicId}/update")
        @Operation(summary = "WebSocket: Updated recipe details", description = """
                        Subscribe via WebSocket to receive real-time updates when a recipe is modified.

                        **WebSocket URL:** ws://localhost:8080/ws
                        **Topic:** /topic/recipes/{publicId}/update
                        **Payload:** RecipeResponse object
                        """, responses = {
                        @ApiResponse(description = "Example RecipeResponse payload", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RecipeResponse.class)))
        })
        public String wsUpdatedRecipeInfo(@PathVariable String publicId) {
                return "Subscribe to /topic/recipes/" + publicId
                                + "/update via WebSocket to receive live recipe updates.";
        }

        @GetMapping("/ws/recipes/{publicId}/views")
        @Operation(summary = "WebSocket: Live recipe view count updates", description = """
                        Subscribe via WebSocket to receive live updates when a recipe's view count changes.

                        **WebSocket URL:** ws://localhost:8080/ws
                        **Topic:** /topic/recipes/{publicId}/views
                        **Payload:** Long (number of views)
                        """, responses = {
                        @ApiResponse(description = "Example view count payload", content = @Content(mediaType = "application/json", schema = @Schema(type = "number", example = "1523")))
        })
        public String wsRecipeViewsInfo(@PathVariable String publicId) {
                return "Subscribe to /topic/recipes/" + publicId
                                + "/views via WebSocket to receive real-time view count updates.";
        }

        @GetMapping("/ws/recipes/{publicId}/likes")
        @Operation(summary = "WebSocket: Live recipe like count updates", description = """
                        Subscribe via WebSocket to receive real-time updates whenever a recipe's like count changes.

                        **WebSocket URL:** ws://localhost:8080/ws
                        **Topic:** /topic/recipes/{publicId}/likes
                        **Payload:** Long (number of likes)
                        """, responses = {
                        @ApiResponse(description = "Example like count payload", content = @Content(mediaType = "application/json", schema = @Schema(type = "number", example = "248")))
        })
        public String wsRecipeLikesInfo(@PathVariable String publicId) {
                return "Subscribe to /topic/recipes/" + publicId
                                + "/likes via WebSocket to receive real-time like count updates.";
        }

}
