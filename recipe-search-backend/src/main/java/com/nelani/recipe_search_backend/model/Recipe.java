package com.nelani.recipe_search_backend.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recipes")
@Access(AccessType.FIELD)
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    @NotBlank(message = "Recipe name cannot be blank")
    private String name;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Image URL cannot be blank")
    private String imageUrl;

    @Column(nullable = false)
    private Integer cookTimeMinutes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    @NotEmpty(message = "Recipe must have at least one ingredient")
    @Valid
    private List<Ingredient> ingredients;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    @NotEmpty(message = "Recipe must have at least one step")
    @Valid
    private List<Step> steps;
}
