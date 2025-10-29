package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.model.RecipeLike;
import com.nelani.recipe_search_backend.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipeLikeRepository extends JpaRepository<RecipeLike, UUID> {
    long countByRecipe(Recipe recipe);

    Optional<RecipeLike> findByUserAndRecipe(User user, Recipe recipe);

    boolean existsByUserAndRecipe(User user, Recipe recipe);

    @Query("SELECT rl.recipe.publicId FROM RecipeLike rl WHERE rl.user = :user")
    List<String> findRecentLikedRecipeIdsByUser(@Param("user") User user, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM RecipeLike rl WHERE rl.user = :user")
    int deleteByUser(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM RecipeLike rl WHERE rl.recipe = :recipe")
    int deleteByRecipe(@Param("recipe") Recipe recipe);
}
