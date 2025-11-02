package com.nelani.recipe_search_backend.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Standard error response for API failures")
public record ErrorResponse(
                @Schema(description = "Short error code or type", example = "INVALID_REQUEST") String error,

                @Schema(description = "Detailed error message explaining the issue", example = "The provided email address is invalid.") String message) {
}
