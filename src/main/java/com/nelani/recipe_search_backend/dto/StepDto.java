package com.nelani.recipe_search_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record StepDto(

                @NotBlank(message = "Step description cannot be blank") String description,

                @Min(value = 1, message = "Estimated minutes must be at least 1") int estimatedMinutes) {
}
