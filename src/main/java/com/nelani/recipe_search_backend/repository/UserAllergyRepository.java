package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.Allergy;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.model.UserAllergy;
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
public interface UserAllergyRepository extends JpaRepository<UserAllergy, UUID> {

    Optional<UserAllergy> findByUserAndAllergy(User user, Allergy allergy);

    List<UserAllergy> findByUser(User user);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM UserAllergy ua WHERE ua.user = :user")
    int deleteByUser(@Param("user") User user);

}
