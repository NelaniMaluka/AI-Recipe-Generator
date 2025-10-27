package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.EmailVerification;
import com.nelani.recipe_search_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {
    Optional<EmailVerification> findByUserAndToken(User user, String token);

    List<EmailVerification> findByUser(User user);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerification ev WHERE ev.user = :user")
    int deleteByUser(@Param("user") User user);
}
