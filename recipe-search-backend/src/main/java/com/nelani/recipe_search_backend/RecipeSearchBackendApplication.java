package com.nelani.recipe_search_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableCaching
@EnableAsync
public class RecipeSearchBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecipeSearchBackendApplication.class, args);
	}

}
