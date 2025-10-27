package com.nelani.recipe_search_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IngredientDto {

    @NotBlank(message = "Ingredient name cannot be blank")
    private String name;

    @NotBlank(message = "Quantity cannot be blank")
    private String quantity;

}
