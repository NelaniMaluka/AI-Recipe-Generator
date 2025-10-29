package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.model.RecipeView;
import com.nelani.recipe_search_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipeViewRepository extends JpaRepository<RecipeView, UUID> {
    Optional<RecipeView> findByUserAndRecipe(User user, Recipe recipe);

    @Modifying
    @Transactional
    @Query("DELETE FROM RecipeView rv WHERE rv.user = :user")
    int deleteByUser(@Param("user") User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM RecipeView rv WHERE rv.recipe = :recipe")
    int deleteByRecipe(@Param("recipe") Recipe recipe);
}
