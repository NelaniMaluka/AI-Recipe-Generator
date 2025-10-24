package com.nelani.recipe_search_backend.repository;

import com.nelani.recipe_search_backend.model.Allergy;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@ActiveProfiles("test")
public class AllergyRepositoryTest {

    @Autowired
    private AllergyRepository allergyRepository;

    @Test
    public void AllergyRepository_FindByName_ReturnOptionalAllergy() {
        // Arrange
        Allergy allergy = Allergy.builder()
                .name("allergy")
                .build();

        // Act
        allergyRepository.save(allergy);

        // Assert
        var optionalAllergy = allergyRepository.findByName(allergy.getName());
        Assertions.assertThat(optionalAllergy).isPresent();
        Allergy retrievedAllergy = optionalAllergy.get();
        Assertions.assertThat(retrievedAllergy.getName()).isEqualTo(allergy.getName());
    }
}
