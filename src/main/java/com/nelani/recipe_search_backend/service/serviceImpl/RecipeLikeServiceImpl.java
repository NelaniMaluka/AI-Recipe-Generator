package com.nelani.recipe_search_backend.service.serviceImpl;

import com.nelani.recipe_search_backend.model.Recipe;
import com.nelani.recipe_search_backend.model.RecipeLike;
import com.nelani.recipe_search_backend.repository.RecipeLikeRepository;
import com.nelani.recipe_search_backend.repository.RecipeRepository;
import com.nelani.recipe_search_backend.repository.UserRepository;
import com.nelani.recipe_search_backend.response.LikeResponse;
import com.nelani.recipe_search_backend.service.RecipeLikeService;
import com.nelani.recipe_search_backend.sockets.RecipeSocket;
import com.nelani.recipe_search_backend.sockets.UserSocket;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RecipeLikeServiceImpl implements RecipeLikeService {

        private final RecipeLikeRepository recipeLikeRepository;
        private final RecipeRepository recipeRepository;
        private final UserRepository userRepository;
        private final UserSocket userSocket;
        private final RecipeSocket recipeSocket;

        public RecipeLikeServiceImpl(RecipeLikeRepository recipeLikeRepository, RecipeRepository recipeRepository,
                        UserRepository userRepository, UserSocket userSocket, RecipeSocket recipeSocket) {
                this.recipeLikeRepository = recipeLikeRepository;
                this.recipeRepository = recipeRepository;
                this.userRepository = userRepository;
                this.userSocket = userSocket;
                this.recipeSocket = recipeSocket;
        }

        /**
         * Retrieves the total number of likes for a recipe.
         *
         * <p>
         * Caches the result for faster repeated access. Throws an exception
         * if the provided recipe ID is invalid.
         * </p>
         *
         * @param publicId the public identifier of the recipe
         * @return the total number of likes for the recipe
         * @throws IllegalArgumentException if the recipe ID is invalid
         */
        @Override
        @Transactional
        @Cacheable(value = "recipe-likes", key = "#publicId")
        public long getRecipeLikes(String publicId) {
                // Fetch the recipe by its public ID
                Recipe recipe = recipeRepository.findByPublicId(publicId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Recipe not found with ID: " + publicId));

                // Return the total like count for the recipe
                return recipeLikeRepository.countByRecipe(recipe);
        }

        /**
         * Adds a like to a recipe for the currently authenticated user.
         *
         * <p>
         * If the user has already liked the recipe, the method performs no action.
         * Requires the user to be authenticated and clears the recipe-like cache entry
         * after a successful operation.
         * </p>
         *
         * @param publicId the public identifier of the recipe
         * @throws ResponseStatusException if the user is unauthenticated, not found, or
         *                                 the recipe ID is invalid
         */
        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "recipe-likes", key = "#publicId"),
                        @CacheEvict(value = "user-recipe-likes-list", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"),
                        @CacheEvict(value = "user-recipe-likes", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':' + #publicId")
        })
        public LikeResponse addRecipeLike(String publicId, int page, int size) {
                // Get the current authenticated user
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // Validate authentication status
                if (authentication == null || !authentication.isAuthenticated()
                                || "anonymousUser".equals(authentication.getPrincipal())) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login or register");
                }

                // Get user from database
                String email = authentication.getName();
                var user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

                // Fetch the recipe by public ID
                Recipe recipe = recipeRepository.findByPublicId(publicId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Recipe not found with ID: " + publicId));

                // Skip if user already liked this recipe
                var existingRecipeLike = recipeLikeRepository.findByUserAndRecipe(user, recipe);
                if (existingRecipeLike.isPresent()) {
                        long currentLikeCount = recipeLikeRepository.countByRecipe(recipe);
                        Pageable pageable = PageRequest.of(page, size);
                        List<String> likedRecipes = recipeLikeRepository.findRecentLikedRecipeIdsByUser(user, pageable);

                        return new LikeResponse("Recipe already liked", publicId, currentLikeCount, likedRecipes);
                }

                // Save the new like
                RecipeLike like = RecipeLike.builder()
                                .user(user)
                                .recipe(recipe)
                                .build();

                recipeLikeRepository.save(like);

                long updatedLikeCount = recipeLikeRepository.countByRecipe(recipe);
                Pageable pageable = PageRequest.of(page, size);
                List<String> likedRecipes = recipeLikeRepository.findRecentLikedRecipeIdsByUser(user, pageable);
                Long likes = recipeLikeRepository.countByRecipe(recipe);

                recipeSocket.sendUpdatedRecipeLikes(recipe, likes);
                userSocket.sendUpdatedUserLikes(user, likedRecipes);

                return new LikeResponse("Like added successfully", publicId, updatedLikeCount, likedRecipes);
        }

        @Override
        @Transactional
        @Caching(evict = {
                        @CacheEvict(value = "recipe-likes", key = "#publicId"),
                        @CacheEvict(value = "user-recipe-likes-list", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()"),
                        @CacheEvict(value = "user-recipe-likes", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':' + #publicId")
        })
        public void removeRecipeLike(String publicId) {
                // Get the current authenticated user
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // Validate authentication status
                if (authentication == null || !authentication.isAuthenticated()
                                || "anonymousUser".equals(authentication.getPrincipal())) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login or register");
                }

                // Get user from database
                String email = authentication.getName();
                var user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

                Recipe recipe = recipeRepository.findByPublicId(publicId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Recipe not found with ID: " + publicId));

                // Check if the like exists
                var existingRecipeLike = recipeLikeRepository.findByUserAndRecipe(user, recipe)
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Like not found for this user and recipe."));

                recipeLikeRepository.delete(existingRecipeLike); // delete the recipe

                Pageable page = PageRequest.of(0, 500);
                List<String> likedRecipes = recipeLikeRepository.findRecentLikedRecipeIdsByUser(user, page);
                Long likes = recipeLikeRepository.countByRecipe(recipe);

                recipeSocket.sendUpdatedRecipeLikes(recipe, likes);
                userSocket.sendUpdatedUserLikes(user, likedRecipes);
        }

        /**
         * Retrieves a list of recipe IDs that the currently authenticated user has
         * liked.
         *
         * <p>
         * This method caches the result per user to reduce database load for repeated
         * accesses.
         * Only the most recent 500 liked recipes are returned to limit memory usage.
         * </p>
         *
         * <p>
         * Authentication is required; an exception is thrown if the user is not logged
         * in or does not exist.
         * </p>
         *
         * @return a list of recipe public IDs liked by the current user
         * @throws ResponseStatusException if the user is unauthenticated or not found
         */
        @Override
        @Transactional
        @Cacheable(value = "user-recipe-likes-list", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
        public List<String> getUserLikes() {
                // Retrieve the current authenticated user
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // Validate authentication status
                if (authentication == null || !authentication.isAuthenticated()
                                || "anonymousUser".equals(authentication.getPrincipal())) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login or register");
                }

                // Get the user entity from the database
                String email = authentication.getName();
                var user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

                // Return the most recent 500 liked recipe IDs for the user
                Pageable page = PageRequest.of(0, 500);
                return recipeLikeRepository.findRecentLikedRecipeIdsByUser(user, page);
        }

        /**
         * Checks whether the currently authenticated user has liked a specific recipe.
         *
         * <p>
         * This method first validates that the user is authenticated. It then checks
         * the database
         * to determine if a like exists between the user and the specified recipe. The
         * result is cached
         * per user and recipe combination to reduce repeated database queries.
         * </p>
         *
         * @param publicId the public identifier of the recipe to check
         * @return true if the user has liked the recipe, false otherwise
         * @throws ResponseStatusException  if the user is unauthenticated or not found
         * @throws IllegalArgumentException if the recipe ID is invalid
         */
        @Override
        @Transactional
        @Cacheable(value = "user-recipe-likes", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName() + ':' + #publicId")
        public boolean fallbackUserLikedCheck(String publicId) {
                // Retrieve the current authenticated user
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                // Validate authentication status
                if (authentication == null || !authentication.isAuthenticated()
                                || "anonymousUser".equals(authentication.getPrincipal())) {
                        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login or register");
                }

                // Get the user entity from the database
                String email = authentication.getName();
                var user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

                // Fetch the recipe by its public ID
                Recipe recipe = recipeRepository.findByPublicId(publicId)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Recipe not found with ID: " + publicId));

                // Check if a like exists for this user and recipe
                return recipeLikeRepository.existsByUserAndRecipe(user, recipe);
        }

}
