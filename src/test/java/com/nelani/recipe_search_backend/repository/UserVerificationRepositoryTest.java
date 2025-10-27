package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.Provider;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.model.UserVerification;
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
public class UserVerificationRepositoryTest {

        @Autowired
        private UserVerificationRepository verificationRepository;

        @Autowired
        private UserRepository userRepository;

        @Test
        public void UserVerificationRepository_FindByUserAndToken_ReturnOptionalUserVerification() {
                // Arrange
                User user = User.builder()
                                .firstname("firstname")
                                .lastname("lastname")
                                .email("test-email@test.co.za")
                                .publicId("publicId")
                                .provider(Provider.LOCAL)
                                .password("Password@123")
                                .build();

                UserVerification userVerification = UserVerification.builder()
                                .token("token-123")
                                .user(user)
                                .expiryDate(LocalDateTime.now().plusMinutes(15))
                                .type(VerificationType.EMAIL)
                                .build();

                // Act
                userRepository.save(user);
                verificationRepository.save(userVerification);

                // Assert
                var optionalVerification = verificationRepository.findByUserAndToken(user, "token-123");
                Assertions.assertThat(optionalVerification).isPresent();
                UserVerification result = optionalVerification.get();
                Assertions.assertThat(result.getUser()).isEqualTo(user);
                Assertions.assertThat(result.getExpiryDate()).isAfter(LocalDateTime.now());
                Assertions.assertThat(result.getType()).isEqualTo(VerificationType.EMAIL);
        }

        @Test
        public void UserAllergyRepository_DeleteByUser_RemovesRecords() {
                // Arrange
                // Arrange
                User user = User.builder()
                                .firstname("firstname")
                                .lastname("lastname")
                                .email("test-email@test.co.za")
                                .publicId("publicId")
                                .provider(Provider.LOCAL)
                                .password("Password@123")
                                .build();

                UserVerification userVerification = UserVerification.builder()
                                .token("token-123")
                                .user(user)
                                .expiryDate(LocalDateTime.now().plusMinutes(15))
                                .type(VerificationType.EMAIL)
                                .build();

                // Act
                userRepository.save(user);
                verificationRepository.save(userVerification);

                // Assert it's saved
                Assertions.assertThat(verificationRepository.findByUser(user)).hasSize(1);

                // Act - Delete
                int rowsDeleted = verificationRepository.deleteByUser(user);

                // Assert row is deleted
                Assertions.assertThat(rowsDeleted).isEqualTo(1);
                Assertions.assertThat(verificationRepository.findByUser(user)).isEmpty();
        }

}
