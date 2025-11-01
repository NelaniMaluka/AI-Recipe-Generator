package com.nelani.recipe_search_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "password_reset")
@Builder
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    private User user;

    @Size(min = 6, max = 128, message = "Token must be between 6 and 128 characters")
    @Column(name = "token", unique = true, length = 128)
    private String token;

    @Builder.Default
    @NotNull(message = "Expiry date must not be null")
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);
}
