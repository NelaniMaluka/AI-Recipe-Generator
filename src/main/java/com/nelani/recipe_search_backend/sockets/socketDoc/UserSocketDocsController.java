package com.nelani.recipe_search_backend.sockets.socketDoc;

import com.nelani.recipe_search_backend.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Swagger documentation controller for WebSocket endpoints related to users.
 * <p>
 * These endpoints are only for documentation and cannot be called directly via
 * HTTP.
 */
@RestController
@Tag(name = "WebSocket - Users", description = "WebSocket endpoints for real-time user updates")
public class UserSocketDocsController {

        @GetMapping("/ws/users/{publicId}/update")
        @Operation(summary = "WebSocket: Live user profile updates", description = """
                        Subscribe via WebSocket to receive real-time updates whenever a user's profile or details change.

                        **WebSocket URL:** ws://localhost:8080/ws
                        **Topic:** /topic/users/{publicId}/update
                        **Payload:** UserResponse object
                        """, responses = {
                        @ApiResponse(description = "Example UserResponse payload", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)))
        })
        public String wsUserProfileUpdateInfo(@PathVariable String publicId) {
                return "Subscribe to /topic/users/" + publicId
                                + "/update via WebSocket to receive real-time user profile updates.";
        }

        @GetMapping("/ws/users/{publicId}/likes")
        @Operation(summary = "WebSocket: Live user liked recipes updates", description = """
                        Subscribe via WebSocket to receive real-time updates whenever a user's liked recipes list changes.

                        **WebSocket URL:** ws://localhost:8080/ws
                        **Topic:** /topic/users/{publicId}/likes
                        **Payload:** List of recipe public IDs (List<String>)
                        """, responses = {
                        @ApiResponse(description = "Example liked recipes payload", content = @Content(mediaType = "application/json", schema = @Schema(type = "array", example = "[\"recipe123\", \"recipe456\", \"recipe789\"]")))
        })
        public String wsUserLikesInfo(@PathVariable String publicId) {
                return "Subscribe to /topic/users/" + publicId
                                + "/likes via WebSocket to receive live updates of the user's liked recipes.";
        }
}
