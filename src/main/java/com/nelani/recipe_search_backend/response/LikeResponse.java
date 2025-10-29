package com.nelani.recipe_search_backend.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "LikeResponse", description = "Response returned when a recipe is liked or unliked")
public class LikeResponse {

    @Schema(description = "Message describing the result of the operation", example = "Like added successfully")
    private String message;

    @Schema(description = "Public ID of the recipe that was liked or unliked", example = "abc123")
    private String publicId;

    @Schema(description = "Total number of likes for the recipe", example = "42")
    private long likeCount;

    @Schema(description = "List of the most recent liked recipe IDs by the user", example = "[\"abc123\", \"def456\", \"ghi789\"]")
    private List<String> likedRecipes;
}
