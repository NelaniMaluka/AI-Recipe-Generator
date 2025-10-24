package com.nelani.recipe_search_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_verifications")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Many verifications can belong to one user
    @NotNull(message = "User must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(min = 6, max = 128, message = "Token must be between 6 and 128 characters")
    @Column(name = "token", unique = true, length = 128)
    private String token;

    @NotNull(message = "Date must not be null")
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime date = LocalDateTime.now();

    @Builder.Default
    @NotNull(message = "Expiry date must not be null")
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

    @NotNull(message = "Verification type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", nullable = false, length = 20)
    private VerificationType type;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private boolean used = false;
}
