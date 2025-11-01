package com.nelani.recipe_search_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StepDto {

    @NotBlank(message = "Step description cannot be blank")
    private String description;

    @Min(value = 1, message = "Estimated minutes must be at least 1")
    private int estimatedMinutes;

}
