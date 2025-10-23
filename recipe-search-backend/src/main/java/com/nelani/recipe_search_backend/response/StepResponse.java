package com.nelani.recipe_search_backend.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Represents a single step in a recipe")
public class StepResponse {

        @Schema(description = "Description of the step to follow in the recipe", example = "Chop the onions and sauté them until golden brown")
        private String description;

        @Schema(description = "Estimated time in minutes to complete this step", example = "5")
        private int estimatedMinutes;
}
