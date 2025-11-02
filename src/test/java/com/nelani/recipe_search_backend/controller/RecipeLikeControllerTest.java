package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.response.LikeResponse;
import com.nelani.recipe_search_backend.security.JwtService;
import com.nelani.recipe_search_backend.service.RecipeLikeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecipeLikeController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RecipeLikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RecipeLikeService recipeLikeService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void RecipeController_GetRecipeLikes_ReturnsLikeCount() throws Exception {
        // Arrange
        String publicId = "abc123";
        long likeCount = 42L;
        when(recipeLikeService.getRecipeLikes(publicId)).thenReturn(likeCount);

        // Act & Assert
        mockMvc.perform(get("/api/public/recipes/{publicId}/likes", publicId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likes").value(likeCount));
    }

    @Test
    void RecipeController_AddRecipeLike_ReturnsCreated() throws Exception {
        // Arrange
        String publicId = "abc123";
        LikeResponse likeResponse = LikeResponse.builder().build();
        when(recipeLikeService.addRecipeLike(publicId, 0, 500)).thenReturn(likeResponse);

        // Act & Assert
        mockMvc.perform(post("/api/user/recipes/{publicId}/likes", publicId)
                .param("page", "0")
                .param("size", "500")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Like added successfully"))
                .andExpect(jsonPath("$.publicId").value(publicId));
    }

    @Test
    void RecipeController_RemoveRecipeLike_ReturnsNoContent() throws Exception {
        // Arrange
        String publicId = "abc123";
        // Mock the service so it does nothing (void method)
        doNothing().when(recipeLikeService).removeRecipeLike(publicId);

        // Act & Assert
        mockMvc.perform(delete("/api/user/recipes/{publicId}/likes", publicId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void RecipeController_GetUserLikes_ReturnsLikedRecipes() throws Exception {
        // Arrange
        List<String> likedRecipes = List.of("abc123", "xyz789");
        when(recipeLikeService.getUserLikes()).thenReturn(likedRecipes);

        // Act & Assert
        mockMvc.perform(get("/api/user/likes")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likedRecipes[0]").value("abc123"))
                .andExpect(jsonPath("$.likedRecipes[1]").value("xyz789"));
    }

    @Test
    void RecipeController_UserLikedCheck_ReturnsLikedStatus() throws Exception {
        // Arrange
        String publicId = "abc123";
        boolean likedStatus = true;
        when(recipeLikeService.fallbackUserLikedCheck(publicId)).thenReturn(likedStatus);

        // Act & Assert
        mockMvc.perform(get("/api/user/recipes/{publicId}/liked", publicId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.liked").value(likedStatus));
    }

}
