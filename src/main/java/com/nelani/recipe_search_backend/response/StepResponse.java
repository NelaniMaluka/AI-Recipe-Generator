package com.nelani.recipe_search_backend.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents a single step in a recipe")
public record StepResponse(
                @Schema(description = "Description of the step to follow in the recipe", example = "Chop the onions and sauté them until golden brown") String description,

                @Schema(description = "Estimated time in minutes to complete this step", example = "5") int estimatedMinutes) {
}
