package com.nelani.recipe_search_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableRetry
public class RecipeSearchBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecipeSearchBackendApplication.class, args);
	}

}
