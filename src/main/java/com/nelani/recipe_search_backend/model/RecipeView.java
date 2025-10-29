package com.nelani.recipe_search_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "recipe_id" }))
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecipeView {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "User must not be blank")
    @ManyToOne(optional = false)
    private User user;

    @NotNull(message = "Recipe must not be blank")
    @ManyToOne(optional = false)
    private Recipe recipe;

    @Builder.Default
    private LocalDateTime viewedAt = LocalDateTime.now();
}
