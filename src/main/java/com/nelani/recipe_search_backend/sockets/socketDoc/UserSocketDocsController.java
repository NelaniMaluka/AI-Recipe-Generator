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

@RestController
@Tag(name = "WebSocket", description = "Documentation for WebSocket endpoints")
public class UserSocketDocsController {

        @GetMapping("/ws/users/{publicId}")
        @Operation(summary = "WebSocket subscription for user updates", description = "Subscribe to the WebSocket to receive real-time updates about a user.\n\n"
                        + "**WebSocket URL:** ws://localhost:8080/ws\n"
                        + "**Topic:** /topic/recipes/{publicId}\n"
                        + "**Payload:** UserResponse object", responses = {
                                        @ApiResponse(description = "Example UserResponse payload", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class)))
                        })
        public String wsUserInfo(@PathVariable String publicId) {
                return "This endpoint is for Swagger documentation purposes only. "
                                + "Subscribe to /topic/recipes/" + publicId
                                + " via WebSocket to receive UserResponse updates.";
        }
}
