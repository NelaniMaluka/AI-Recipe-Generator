package com.nelani.recipe_search_backend.sockets;

import com.nelani.recipe_search_backend.response.RecipeResponse;
import com.nelani.recipe_search_backend.mapper.RecipeMapper;
import com.nelani.recipe_search_backend.model.Recipe;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handles real-time recipe updates and broadcasts via WebSocket.
 */
@Component
public class RecipeSocket {

    private final SimpMessagingTemplate messagingTemplate;

    public RecipeSocket(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /** Sends AI-generated recipe search results to all subscribed clients. */
    public void sendAiResults(List<Recipe> recipes, String searchTerm) {
        List<RecipeResponse> formattedRecipes = recipes.stream()
                .map(RecipeMapper::mapRecipeWithMinimalDetails)
                .toList();
        messagingTemplate.convertAndSend("/topic/recipes/search/" + searchTerm, formattedRecipes);
    }

    /** Broadcasts a recipe update event to subscribers of the specific recipe. */
    public void sendUpdatedRecipe(RecipeResponse recipe) {
        messagingTemplate.convertAndSend("/topic/recipes/" + recipe.publicId() + "/update", recipe);
    }

    /** Sends live view count updates for a specific recipe. */
    public void sendUpdatedRecipeViews(Recipe recipe) {
        messagingTemplate.convertAndSend("/topic/recipes/" + recipe.getPublicId() + "/views", recipe.getViews());
    }

    /** Sends live like count updates for a specific recipe. */
    public void sendUpdatedRecipeLikes(Recipe recipe, Long likes) {
        messagingTemplate.convertAndSend("/topic/recipes/" + recipe.getPublicId() + "/likes", likes);
    }
}
