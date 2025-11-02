package com.nelani.recipe_search_backend.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
@Schema(name = "LikeResponse", description = "Response returned when a recipe is liked or unliked")
public record LikeResponse(
                @Schema(description = "Message describing the result of the operation", example = "Like added successfully") String message,

                @Schema(description = "Public ID of the recipe that was liked or unliked", example = "abc123") String publicId,

                @Schema(description = "Total number of likes for the recipe", example = "42") long likeCount,

                @Schema(description = "List of the most recent liked recipe IDs by the user", example = "[\"abc123\", \"def456\", \"ghi789\"]") List<String> likedRecipes) {
}
