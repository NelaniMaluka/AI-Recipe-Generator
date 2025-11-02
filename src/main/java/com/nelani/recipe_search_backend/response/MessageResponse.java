package com.nelani.recipe_search_backend.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic response for operations returning a message")
public record MessageResponse(
                @Schema(description = "Message describing the result of the operation", example = "Password reset token has been sent successfully") String message,

                @Schema(description = "Optional related email or resource", example = "user@example.com") String email) {
}
