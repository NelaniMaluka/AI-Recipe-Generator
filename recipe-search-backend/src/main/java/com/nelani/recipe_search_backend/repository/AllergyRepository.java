package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AllergyRepository extends JpaRepository<Allergy, UUID> {
    Optional<Allergy> findByName(String name);
}
