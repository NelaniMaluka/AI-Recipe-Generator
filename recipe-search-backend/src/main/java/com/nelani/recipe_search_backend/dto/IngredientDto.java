package com.nelani.recipe_search_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IngredientDto {
    private Long id;
    private String name;
    private String quantity;
}
