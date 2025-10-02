package com.nelani.recipe_search_backend.sockets;

import com.nelani.recipe_search_backend.dto.RecipeDto;
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

    public void sendAiResults (List<Recipe> recipes, String searchTerm) {
        List<RecipeDto> formattedRecipes = recipes.stream()
                .map(RecipeMapper::mapRecipeWithMinimalDetails)
                .toList();
        messagingTemplate.convertAndSend("/topic/recipes/" + searchTerm, formattedRecipes);
    }
}
