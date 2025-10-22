package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.PasswordReset;
import com.nelani.recipe_search_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, UUID> {
    Optional<PasswordReset> findByUserAndToken(User user, String token);

    List<PasswordReset> findByUser(User user);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("DELETE FROM PasswordReset pr WHERE pr.user = :user")
    int deleteByUser(@Param("user") User user);
}
