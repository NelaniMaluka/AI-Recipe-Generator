package com.nelani.recipe_search_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "allergies")
public class Allergy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    @NotBlank(message = "Allergy name cannot be empty")
    private String name;

    @PrePersist
    @PreUpdate
    private void convertNameToLowerChar() {
        name = name.trim().toLowerCase();
    }

}
