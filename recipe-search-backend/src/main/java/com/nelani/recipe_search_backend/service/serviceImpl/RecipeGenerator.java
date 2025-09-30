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
import org.springframework.web.client.RestTemplate;

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

        // Try saving each recipe individually
        recipes.forEach(recipe -> {
            try {
                // Attempt to insert recipe into DB
                boolean exists = recipeRepository.existsByNameAndIngredientsAndSteps(recipe.getName(),
                        recipe.getIngredients(), recipe.getSteps());
                if (!exists) {
                    recipeRepository.save(recipe);
                }
            } catch (DataIntegrityViolationException e) {
                // Skip duplicates (unique constraints like recipe name, etc.)
                log.debug("Recipe '{}' already exists, skipping.", recipe.getName());
            }
        });

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
        // Hugging Face chat endpoint
        String url = "https://router.huggingface.co/v1/chat/completions";

        // Spring RestTemplate to make HTTP requests
        RestTemplate restTemplate = new RestTemplate();

        // Set HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + huggingfaceApiKey); // Bearer token for authentication
        headers.setContentType(MediaType.APPLICATION_JSON); // Set content type to JSON

        // JSON prompt to generate 5 recipes in a structured format
        String inputJson = """
                {
                  "model": "deepseek-ai/DeepSeek-V3.1-Terminus:novita",
                  "messages": [
                    {"role": "user", "content": "Generate 5 cooking recipes about %s in JSON format. The response should be a JSON array of objects with this structure {\\\"name\\\": string, \\\"cookTimeMinutes\\\": integer, \\\"ingredients\\\": [{\\\"name\\\": string, \\\"quantity\\\": string}], \\\"steps\\\": [{\\\"description\\\": string, \\\"estimatedMinutes\\\": int}]}."}
                  ]
                }
                """
                .formatted(searchWord); // Inject searchWord into prompt

        // Wrap the JSON prompt and headers into an HttpEntity for the POST request
        HttpEntity<String> entity = new HttpEntity<>(inputJson, headers);

        try {
            // Make the POST request to Hugging Face API
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // Raw response from the model
            String responseJson = response.getBody();

            // Jackson ObjectMapper to parse JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseJson);

            // Extract the model's output text from the response structure
            String jsonString = root.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();

            // Clean any code block formatting or backticks (common in chat models)
            jsonString = jsonString.strip()
                    .replaceAll("(?s)^```.*?\\n", "") // remove starting ``` if present
                    .replaceAll("(?s)```$", ""); // remove ending ``` if present

            // Parse cleaned JSON string into List<Recipe>
            List<Recipe> recipes = mapper.readValue(jsonString, new TypeReference<List<Recipe>>() {
            });

            // imageUrl is assigned here before saving
            recipes.forEach(recipe -> {
                String imgUrl = recipeImageGenerator(recipe.getName());
                recipe.setImageUrl(imgUrl);
            });

            recipeSocket.sendAiResults(recipes, searchWord);

            return recipes;
        } catch (Exception e) {
            log.error("Failed to generate recipes for '{}'", searchWord, e);

            // notify client that AI generation failed
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

}