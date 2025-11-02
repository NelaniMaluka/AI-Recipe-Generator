package com.nelani.recipe_search_backend.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response returned after a successful login")
public record LoginResponse(
                @Schema(description = "JWT token issued to the user for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String token,

                @Schema(description = "Token expiration time in seconds", example = "3600") long expiresIn,

                @Schema(description = "Details of the authenticated user") UserResponse user) {
}
