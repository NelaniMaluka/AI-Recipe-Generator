package com.nelani.recipe_search_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StepDto {
    private Long id;
    private String description;
    private int estimatedMinutes;
}
