package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.Allergy;
import com.nelani.recipe_search_backend.model.Provider;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.model.UserAllergy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
public class UserAllergyRepositoryTest {

        @Autowired
        private UserAllergyRepository userAllergyRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private AllergyRepository allergyRepository;

        private User user;
        private Allergy allergy;
        private Allergy allergy2;
        private UserAllergy userAllergy;
        private final List<UserAllergy> userAllergyList = new ArrayList<>();

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

                allergy = Allergy.builder()
                                .name("allergy")
                                .build();

                allergy2 = Allergy.builder()
                                .name("allergy2")
                                .build();

                userRepository.save(user);
                allergyRepository.save(allergy);
                allergyRepository.save(allergy2);

                userAllergy = UserAllergy.builder()
                                .user(user)
                                .allergy(allergy)
                                .build();

                UserAllergy userAllergy2 = UserAllergy.builder()
                                .user(user)
                                .allergy(allergy2)
                                .build();

                userAllergyList.add(userAllergy);
                userAllergyList.add(userAllergy2);
        }

        @Test
        public void UserAllergyRepository_FindByUserAndAllergy_ReturnOptionalUserAllergy() {
                // Arrange
                userAllergyRepository.save(userAllergy);

                // Assert
                var optionalUserAllergy = userAllergyRepository.findByUserAndAllergy(user, allergy);
                Assertions.assertThat(optionalUserAllergy).isPresent();
                UserAllergy ul = optionalUserAllergy.get();
                Assertions.assertThat(ul.getUser()).isEqualTo(user);
                Assertions.assertThat(ul.getAllergy()).isEqualTo(allergy);
        }

        @Test
        public void UserAllergyRepository_FindByUser_ReturnListUserAllergy() {
                // Arrange
                userAllergyList.forEach(userAllergyRepository::save);

                // Act
                var resultList = userAllergyRepository.findByUser(user);

                // Assert
                Assertions.assertThat(resultList)
                                .hasSize(2) // ensures there are exactly 2 entries
                                .allSatisfy(ua -> Assertions.assertThat(ua.getUser()).isEqualTo(user)); // all entries

                // Optionally, assert the allergies match expected ones
                Assertions.assertThat(resultList)
                                .extracting(UserAllergy::getAllergy)
                                .containsExactlyInAnyOrder(allergy, allergy2);
        }

        @Test
        public void UserAllergyRepository_DeleteByUser_RemovesRecords() {
                // Arrange
                userAllergyRepository.save(userAllergy);

                // Assert it's saved
                Assertions.assertThat(userAllergyRepository.findByUser(user)).hasSize(1);

                // Act - Delete
                int rowsDeleted = userAllergyRepository.deleteByUser(user);

                // Assert row is deleted
                Assertions.assertThat(rowsDeleted).isEqualTo(1);
                Assertions.assertThat(userAllergyRepository.findByUser(user)).isEmpty();
        }

}
