package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.PasswordReset;
import com.nelani.recipe_search_backend.model.Provider;
import com.nelani.recipe_search_backend.model.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
public class PasswordResetRepositoryTest {

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private PasswordReset passwordReset;

    @BeforeEach
    public void init() {
        user = User.builder()
                .firstname("firstname")
                .lastname("lastname")
                .email("test-email@test.co.za")
                .publicId("publicId")
                .provider(Provider.LOCAL)
                .password("Password@123")
                .build();

        passwordReset = PasswordReset.builder()
                .token("token-123")
                .user(user)
                .build();
    }

    @Test
    public void PasswordResetRepository_FindByUserAndToken_ReturnOptionalPasswordReset() {
        // Arrange
        userRepository.save(user);
        passwordResetRepository.save(passwordReset);

        // Assert
        var optionalPasswordReset = passwordResetRepository.findByUserAndToken(user, passwordReset.getToken());
        Assertions.assertThat(optionalPasswordReset).isPresent();
        PasswordReset result = optionalPasswordReset.get();
        Assertions.assertThat(result.getToken()).isEqualTo(passwordReset.getToken());
        Assertions.assertThat(result.getUser()).isEqualTo(user);
        Assertions.assertThat(result.getExpiryDate()).isAfter(LocalDateTime.now());
    }

    @Test
    public void PasswordResetRepository_FindByUser_ReturnPasswordResetList() {
        // Arrange
        userRepository.save(user);
        passwordResetRepository.save(passwordReset);

        // Act
        List<PasswordReset> passwordResetList = passwordResetRepository.findByUser(user);

        // Assert
        Assertions.assertThat(passwordResetList).hasSize(1);

        PasswordReset result = passwordResetList.get(0); // first (and only) item

        Assertions.assertThat(result.getToken()).isEqualTo(passwordReset.getToken());
        Assertions.assertThat(result.getUser()).isEqualTo(user);
        Assertions.assertThat(result.getExpiryDate()).isAfter(LocalDateTime.now());
    }

    @Test
    public void PasswordResetRepository_DeleteByUser_ReturnRecords() {
        // Arrange
        userRepository.save(user);
        passwordResetRepository.save(passwordReset);

        // Assert it's saved
        Assertions.assertThat(passwordResetRepository.findById(passwordReset.getId())).isPresent();

        // Act - Delete
        int rowsDeleted = passwordResetRepository.deleteByUser(user);

        // Assert row is deleted
        Assertions.assertThat(rowsDeleted).isEqualTo(1);
        Assertions.assertThat(passwordResetRepository.findById(passwordReset.getId())).isEmpty();
    }
}
