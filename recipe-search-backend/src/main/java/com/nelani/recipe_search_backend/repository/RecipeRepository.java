package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    Optional<Recipe> findByPublicId(String publicId);

    boolean existsByName(String name);

    @Query("SELECT DISTINCT r FROM Recipe r " +
            "LEFT JOIN FETCH r.ingredients i " +
            "WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(i.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Recipe> searchRecipes(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("""
    SELECT r FROM Recipe r
    WHERE (:startTime IS NULL OR :endTime IS NULL OR r.cookTimeMinutes BETWEEN :startTime AND :endTime)
      AND (:mealType IS NULL OR r.mealType = :mealType)
      AND (:startDate IS NULL OR r.createdAt >= :startDate)
      AND (:endDate IS NULL OR r.createdAt < :endDate)
    ORDER BY r.createdAt DESC
    """)
    List<Recipe> getRecipesByTimeAndMealType(
            @Param("startTime") Integer startTime,
            @Param("endTime") Integer endTime,
            @Param("mealType") MealType mealType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

}
