package com.nelani.recipe_search_backend.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    @Column(unique = true)
    private String publicId;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    @NotBlank(message = "Recipe name cannot be blank")
    private String name;

    @Column(nullable = false, length = 500)
    @NotBlank(message = "Image URL cannot be blank")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MealType mealType;

    @Column(nullable = false)
    private Integer cookTimeMinutes;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

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

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null || this.publicId.isBlank()) {
            String slug = name
                    .toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-");
            String suffix = UUID.randomUUID()
                    .toString()
                    .substring(0, 6);
            this.publicId = slug + "-" + suffix;
        }
    }
}
