package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.EmailVerification;
import com.nelani.recipe_search_backend.model.Provider;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.model.VerificationType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
public class EmailVerificationRepositoryTest {

        @Autowired
        private EmailVerificationRepository emailVerificationRepository;

        @Autowired
        private UserRepository userRepository;

        @Test
        public void EmailVerificationRepository_FindByUserAndToken_ReturnOptionalEmailVerification() {
                // Arrange
                User user = User.builder()
                                .firstname("firstname")
                                .lastname("lastname")
                                .email("test-email@test.co.za")
                                .publicId("publicId")
                                .provider(Provider.LOCAL)
                                .password("Password@123")
                                .build();

                EmailVerification verification = EmailVerification.builder()
                                .user(user)
                                .token("token123")
                                .newEmail("test-email@test123.co.za")
                                .type(VerificationType.EMAIL)
                                .build();

                userRepository.save(user);
                emailVerificationRepository.save(verification);

                // Assert
                var optionalResult = emailVerificationRepository.findByUserAndToken(user, verification.getToken());
                Assertions.assertThat(optionalResult).isPresent();
                var result = optionalResult.get();
                Assertions.assertThat(result.getUser()).isEqualTo(user);
                Assertions.assertThat(result.getToken()).isEqualTo(verification.getToken());
                Assertions.assertThat(result.getExpiryDate()).isAfter(LocalDateTime.now());
                Assertions.assertThat(result.getType()).isEqualTo(verification.getType());
        }

        @Test
        public void EmailVerificationRepository_FindByUser_ReturnsUserList() {
                // Arrange
                User user = User.builder()
                                .firstname("firstname")
                                .lastname("lastname")
                                .email("test-email@test.co.za")
                                .publicId("publicId")
                                .provider(Provider.LOCAL)
                                .password("Password@123")
                                .build();

                EmailVerification verification = EmailVerification.builder()
                                .user(user)
                                .token("token123")
                                .newEmail("test-email@test123.co.za")
                                .type(VerificationType.EMAIL)
                                .build();

                userRepository.save(user);
                emailVerificationRepository.save(verification);

                // Assert it's saved
                Assertions.assertThat(emailVerificationRepository.findByUser(user)).hasSize(1);
        }

        @Test
        public void EmailVerificationRepository_DeleteByUser_RemovesUser() {
                // Arrange
                User user = User.builder()
                                .firstname("firstname")
                                .lastname("lastname")
                                .email("test-email@test.co.za")
                                .publicId("publicId")
                                .provider(Provider.LOCAL)
                                .password("Password@123")
                                .build();

                EmailVerification verification = EmailVerification.builder()
                                .user(user)
                                .token("token123")
                                .newEmail("test-email@test123.co.za")
                                .type(VerificationType.EMAIL)
                                .build();

                userRepository.save(user);
                emailVerificationRepository.save(verification);

                // Assert it's saved
                Assertions.assertThat(emailVerificationRepository.findByUser(user)).hasSize(1);

                // Act - Delete
                int rowsDeleted = emailVerificationRepository.deleteByUser(user);

                // Assert row is deleted
                Assertions.assertThat(rowsDeleted).isEqualTo(1);
                Assertions.assertThat(emailVerificationRepository.findByUser(user)).isEmpty();
        }
}
