package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void init() {
        user = User.builder()
                .firstname("firstname")
                .lastname("lastname")
                .email("test-email@test.co.za")
                .publicId("publicId")
                .password("Password@123")
                .build();
    }

    @Test
    public void UserRepository_FindByEmail_ReturnOptionalUser() {
        // Act
        userRepository.save(user);

        // Asset
        var optionalUser = userRepository.findByEmail("test-email@test.co.za");
        Assertions.assertThat(optionalUser).isPresent();
        User response = optionalUser.get();
        Assertions.assertThat(response.getFirstname()).isEqualTo("firstname");
        Assertions.assertThat(response.getLastname()).isEqualTo("lastname");
        Assertions.assertThat(response.getUsername()).isEqualTo("test-email@test.co.za");
        Assertions.assertThat(response.getPassword()).isEqualTo("Password@123");

    }

    @Test
    public void UserRepository_ExistByUsername_ReturnTrue() {
        // Act
        userRepository.save(user);

        // Asset
        var result = userRepository.existsByUsername("test-email@test.co.za");
        Assertions.assertThat(result).isTrue();
    }
}
