package com.nelani.recipe_search_backend.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents an ingredient in a recipe")
public record IngredientResponse(
                @Schema(description = "Name of the ingredient", example = "Tomato") String name,

                @Schema(description = "Quantity of the ingredient required", example = "2 cups") String quantity) {
}
