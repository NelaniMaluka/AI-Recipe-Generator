package com.nelani.recipe_search_backend.response;

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
public class RecipeResponse {
    private String publicId;
    private String name;
    private String imageUrl;
    private MealType mealType;
    private Integer cookTimeMinutes;

    private List<IngredientResponse> ingredients;
    private List<StepResponse> steps;
}
