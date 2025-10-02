package com.nelani.recipe_search_backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nelani.recipe_search_backend.model.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecipeDto {
    private String publicId;
    private String name;
    private String imageUrl;
    private MealType mealType;
    private Integer cookTimeMinutes;

    private List<IngredientDto> ingredients;
    private List<StepDto> steps;
}
