package com.nelani.recipe_search_backend.sockets;

import com.nelani.recipe_search_backend.response.RecipeResponse;
import com.nelani.recipe_search_backend.mapper.RecipeMapper;
import com.nelani.recipe_search_backend.model.Recipe;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RecipeSocket {

    private final SimpMessagingTemplate messagingTemplate;

    public RecipeSocket(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendAiResults(List<Recipe> recipes, String searchTerm) {
        List<RecipeResponse> formattedRecipes = recipes.stream()
                .map(RecipeMapper::mapRecipeWithMinimalDetails)
                .toList();
        messagingTemplate.convertAndSend("/topic/recipes/" + searchTerm, formattedRecipes);
    }

    public void sendSendUpdatedRecipe(RecipeResponse recipe) {
        messagingTemplate.convertAndSend("/topic/recipes/" + recipe.getPublicId(), recipe);
    }
}
