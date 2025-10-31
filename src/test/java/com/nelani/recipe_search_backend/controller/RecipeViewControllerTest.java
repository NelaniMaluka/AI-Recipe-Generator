package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.security.JwtService;
import com.nelani.recipe_search_backend.service.RecipeLikeService;
import com.nelani.recipe_search_backend.service.RecipeViewService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = RecipeViewController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RecipeViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private RecipeViewService recipeViewService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void RecipeController_GetRecipeViews_ReturnsViewCount() throws Exception {
        // Arrange
        String publicId = "abc123";
        long viewCount = 124L;
        when(recipeViewService.getRecipeViews(publicId)).thenReturn(viewCount);

        // Act & Assert
        mockMvc.perform(get("/api/public/recipes/{publicId}/views", publicId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.views").value(viewCount));
    }

    @Test
    void RecipeController_AddView_ReturnsNoContent() throws Exception {
        // Arrange
        String publicId = "abc123";

        // Act & Assert
        mockMvc.perform(post("/api/public/recipes/{publicId}/views", publicId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

}
