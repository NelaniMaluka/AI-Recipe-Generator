package com.nelani.recipe_search_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/oauth-info")
@Tag(name = "OAuth2 Info", description = "Provides information about available OAuth2 providers and their login endpoints")
public class OAuthInfoController {

    @Operation(summary = "Get all available OAuth2 providers")
    @GetMapping("/providers")
    public ResponseEntity<?> getOAuthProviders() {
        return ResponseEntity.ok(Map.of(
                "google", "https://ai-recipe-generator-5rbk.onrender.com/oauth2/authorization/google",
                "github", "https://ai-recipe-generator-5rbk.onrender.com/oauth2/authorization/github"));
    }
}
