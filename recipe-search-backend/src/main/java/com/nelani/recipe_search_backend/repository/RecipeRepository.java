package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.Ingredient;
import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.model.Step;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Optional<Recipe> findByPublicId(String publicId);

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.ingredients i " +
            "WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Recipe> searchRecipes(@Param("searchTerm") String searchTerm, Pageable pageable);

    boolean existsByName(String name);

}
