package com.nelani.recipe_search_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecipeDto {
    private Long id;
    private String name;
    private String imageUrl;
    private Integer cookTimeMinutes;

    private List<IngredientDto> ingredients;
    private List<StepDto> steps;
}
