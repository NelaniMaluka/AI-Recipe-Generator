package com.nelani.recipe_search_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;


@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // Allowed origins: Render app + local dev
        corsConfiguration.setAllowedOrigins(List.of(
                "https://ai-recipe-generator-5rbk.onrender.com",
                "http://localhost:8080"
        ));

        // Allowed HTTP methods
        corsConfiguration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS"
        ));

        // Allowed headers
        corsConfiguration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type"
        ));

        // Allow credentials if needed (cookies / JWTs)
        corsConfiguration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

}

