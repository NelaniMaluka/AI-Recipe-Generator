package com.nelani.recipe_search_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;


@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "recipeTaskExecutor")
    public Executor recipeTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);          // Minimum threads
        executor.setMaxPoolSize(50);           // Maximum threads
        executor.setQueueCapacity(500);        // Queue size for waiting tasks
        executor.setThreadNamePrefix("Recipe-"); // Thread name prefix
        executor.initialize();
        return executor;
    }
    
}
