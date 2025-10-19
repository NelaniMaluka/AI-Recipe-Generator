package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, UUID> {
    Optional<UserVerification> findByToken(String token);
}
