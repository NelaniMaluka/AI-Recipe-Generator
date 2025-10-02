package com.nelani.recipe_search_backend.service.serviceImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.repository.RecipeRepository;
import com.nelani.recipe_search_backend.sockets.RecipeSocket;

import lombok.extern.log4j.Log4j2;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Log4j2
public class RecipeGenerator {

    @Value("${HUGGINGFACE_API_KEY}")
    private String huggingfaceApiKey;

    @Value("${UNSPLASH_API_KEY}")
    private String unsplashApiKey;

    private final RecipeRepository recipeRepository;
    private final RecipeSocket recipeSocket;

    public RecipeGenerator(RecipeRepository recipeRepository, RecipeSocket recipeSocket) {
        this.recipeRepository = recipeRepository;
        this.recipeSocket = recipeSocket;
    }

    @Async("recipeTaskExecutor")
    public void generateAndSaveRecipes(String searchWord) {
        // Call AI service to fetch recipes (may return empty if AI fails or no matches
        // found)
        List<Recipe> recipes = fetchRecipesFromAi(searchWord);

        // Guard clause: stop early if no recipes were generated
        if (recipes == null || recipes.isEmpty()) {
            log.warn("No recipes generated for '{}'", searchWord);
            return;
        }

        List<Recipe> savedRecipes = new ArrayList<>();
        // Try saving each recipe individually
        recipes.forEach(recipe -> {
            try {
                // Attempt to insert recipe into DB
                boolean exists = recipeRepository.existsByName(recipe.getName());

                if (!exists) {
                    saveRecipe(recipe);
                    savedRecipes.add(recipe);
                }
            } catch (DataIntegrityViolationException e) {
                // Skip duplicates (unique constraints like recipe name, etc.)
                log.debug("Recipe '{}' already exists, skipping.", recipe.getName());
            }
        });

        recipeSocket.sendAiResults(savedRecipes, searchWord);

        // Log success with count of how many recipes were processed
        log.info("Successfully processed {} recipes for '{}'", recipes.size(), searchWord);
    }

    /**
     * Generates a list of 5 recipes based on the search word.
     * Uses the hosted DeepSeek-V3.1-Terminus model on Hugging Face chat API.
     *
     * @param searchWord The main ingredient or recipe type to generate recipes for.
     * @return List of Recipe objects
     */
    public List<Recipe> fetchRecipesFromAi(String searchWord) {
        String url = "https://router.huggingface.co/v1/chat/completions";
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + huggingfaceApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String inputJson = """
                    {
                      "model": "deepseek-ai/DeepSeek-V3.1-Terminus:novita",
                      "messages": [
                        {
                          "role": "user",
                          "content": "Generate 5 cooking recipes about %s in JSON format. \
                The response should be a JSON array of objects with this structure: {\\\"name\\\": string, \
                \\\"cookTimeMinutes\\\": integer, \
                \\\"ingredients\\\": [{\\\"name\\\": string, \\\"quantity\\\": string}], \
                \\\"steps\\\": [{\\\"description\\\": string, \\\"estimatedMinutes\\\": int}], \
                \\\"mealType\\\": one of [BREAKFAST, BRUNCH, LUNCH, DINNER, SNACK, APPETIZER, MAIN_COURSE, SIDE_DISH, SALAD, SOUP, DESSERT, BEVERAGE]}."
                        }
                      ]
                    }
                    """
                .formatted(searchWord);

        HttpEntity<String> entity = new HttpEntity<>(inputJson, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            String responseJson = response.getBody();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseJson);

            String rawContent = root.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

            // Remove code fences and extra text
            rawContent = rawContent.strip()
                    .replaceAll("(?s)^```.*?\\n", "")
                    .replaceAll("(?s)```$", "");

            // Extract the JSON array inside the response
            int start = rawContent.indexOf("[");
            int end = rawContent.lastIndexOf("]");
            if (start == -1 || end == -1 || end <= start) {
                log.warn("No JSON array found in Hugging Face response for '{}'", searchWord);
                recipeSocket.sendAiResults(Collections.emptyList(), searchWord);
                return Collections.emptyList();
            }
            String jsonArray = rawContent.substring(start, end + 1);

            List<Recipe> recipes = mapper.readValue(jsonArray, new TypeReference<List<Recipe>>() {
            });
            recipes.forEach(recipe -> recipe.setImageUrl(recipeImageGenerator(recipe.getName())));
            return recipes;

        } catch (Exception e) {
            log.error("Failed to generate recipes for '{}'", searchWord, e);
            recipeSocket.sendAiResults(Collections.emptyList(), searchWord);
            return Collections.emptyList();
        }
    }

    public String recipeImageGenerator(String recipeName) {
        String url = "https://api.unsplash.com/search/photos?query="
                + recipeName + "&client_id=" + unsplashApiKey;

        RestTemplate restTemplate = new RestTemplate();

        try {
            // Call Unsplash API
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());

                // Navigate to first result's URL
                JsonNode results = root.path("results");
                if (results.isArray() && results.size() > 0) {
                    return results.get(0).path("urls").path("regular").asText();
                }
            }

            // Fallback if no images found
            return "https://via.placeholder.com/600x400.png?text=" + recipeName;

        } catch (Exception e) {
            // Log and fallback
            System.err.println("Error fetching image for " + recipeName + ": " + e.getMessage());
            return "https://via.placeholder.com/600x400.png?text=" + recipeName;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRecipe(Recipe recipe) {
        recipeRepository.save(recipe);
    }

}