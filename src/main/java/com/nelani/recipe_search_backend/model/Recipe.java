package com.nelani.recipe_search_backend.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "recipes")
@Builder
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 100)
    private String publicId;

    @Column(unique = true, nullable = false, length = 100)
    private String uniquenessIdentifier;

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Recipe name cannot be blank")
    private String name;

    @Column(nullable = false, length = 500)
    @NotBlank(message = "Image URL cannot be blank")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MealType mealType;

    @Column(nullable = false)
    @Min(value = 1, message = "Cook time must be at least 1 minute")
    private Integer cookTimeMinutes;

    @Column(nullable = false)
    @Min(value = 0, message = "Views cannot be negative")
    @Builder.Default
    private long views = 0;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotEmpty(message = "Recipe must have at least one ingredient")
    @Valid
    private List<Ingredient> ingredients;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @NotEmpty(message = "Recipe must have at least one step")
    @Valid
    private List<Step> steps;

    @PrePersist
    @PreUpdate
    public void generatePublicIdAndUniqueness() {
        // Public ID
        if (this.publicId == null || this.publicId.isBlank()) {
            String slug = name.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")
                    .replaceAll("\\s+", "-");
            String suffix = UUID.randomUUID().toString().substring(0, 6);
            this.publicId = slug + "-" + suffix;
        }

        // Uniqueness Identifier
        if (this.uniquenessIdentifier == null || this.uniquenessIdentifier.isBlank()) {
            String ingredientInitials = this.ingredients.stream()
                    .map(i -> i.getName().substring(0, 1)) // take first character
                    .collect(Collectors.joining());

            String stepInitials = this.steps.stream()
                    .map(s -> s.getDescription().substring(0, 1)) // take first character
                    .collect(Collectors.joining());

            String combined = name + "|" + ingredientInitials + "|" + stepInitials;

            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
                this.uniquenessIdentifier = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to generate uniqueness identifier", e);
            }
        }
    }
}
