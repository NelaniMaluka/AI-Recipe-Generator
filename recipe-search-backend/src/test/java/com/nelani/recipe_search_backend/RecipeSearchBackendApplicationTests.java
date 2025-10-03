package com.nelani.recipe_search_backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Disabled in CI because it fails without real DB")
class RecipeSearchBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
