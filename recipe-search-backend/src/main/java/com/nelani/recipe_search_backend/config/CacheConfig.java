package com.nelani.recipe_search_backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CaffeineCacheManager cacheManager() {
        // No names in constructor → global/default cache applies to any cache name
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Set default cache spec (30 days TTL, 1000 max entries)
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofDays(30))
                .maximumSize(1500));

        // Register caches with custom TTLs
        cacheManager.registerCustomCache("AI recipes", Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofDays(1))
                .maximumSize(500)
                .build());

        // Register caches with custom TTLs
        cacheManager.registerCustomCache("recipes", Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofDays(1))
                .maximumSize(500)
                .build());

        return cacheManager;
    }

}