package com.nelani.recipe_search_backend.service.serviceImpl;

import com.nelani.recipe_search_backend.dto.UserDto;
import com.nelani.recipe_search_backend.mapper.UserMapper;
import com.nelani.recipe_search_backend.model.Allergy;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.model.UserAllergy;
import com.nelani.recipe_search_backend.repository.AllergyRepository;
import com.nelani.recipe_search_backend.repository.UserAllergyRepository;
import com.nelani.recipe_search_backend.repository.UserRepository;
import com.nelani.recipe_search_backend.response.LoginResponse;
import com.nelani.recipe_search_backend.response.UserResponse;
import com.nelani.recipe_search_backend.security.JwtService;
import com.nelani.recipe_search_backend.service.UserService;
import com.nelani.recipe_search_backend.sockets.UserSocket;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AllergyRepository allergyRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final UserSocket userSocket;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository, AllergyRepository allergyRepository,
            UserAllergyRepository userAllergyRepository, UserSocket userSocket, JwtService jwtService) {
        this.userRepository = userRepository;
        this.allergyRepository = allergyRepository;
        this.userAllergyRepository = userAllergyRepository;
        this.userSocket = userSocket;
        this.jwtService = jwtService;
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return mapped user details of the authenticated user.
     * @throws ResponseStatusException if the user is not authenticated or not
     *                                 found.
     */
    @Override
    @Cacheable(value = "user", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Validate that a real (non-anonymous) user is authenticated
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login or register");
        }

        // Extract username from authentication
        String username = authentication.getName();

        // get user by username or return 404 if not found
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Retrieve all allergy associations for the given user
        var allergyList = userAllergyRepository.findByUser(user);

        return UserMapper.mapUserWithAllDetails(user, allergyList);
    }

    /**
     * Updates the authenticated user's profile and allergy preferences.
     * <p>
     * Updates first and last name, persists allergy associations, and
     * returns a refreshed JWT token with updated user info.
     *
     * @param userDto DTO containing updated user information
     * @return LoginResponse with new token and updated user details
     * @throws ResponseStatusException if the user is not authenticated (401)
     *                                 or not found (404)
     */
    @Override
    @CacheEvict(value = "user", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public LoginResponse updateUserDetails(UserDto userDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Validate that a real (non-anonymous) user is authenticated
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login or register");
        }

        // Extract username from authentication
        String username = authentication.getName();

        // get user by username or return 404 if not found
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setFirstname(userDto.getFirstname());
        user.setLastname(userDto.getLastname());

        saveAllergies(user, userDto);

        userRepository.save(user);

        // Retrieve all allergy associations for the given user
        var allergyList = userAllergyRepository.findByUser(user);

        // Generate a new user response
        LoginResponse response = new LoginResponse();
        response.setToken(jwtService.generateToken(user));
        response.setExpiresIn(86400000);
        response.setUser(UserMapper.mapUserWithAllDetails(user, allergyList));

        userSocket.sendUpdatedUser(response.getUser());
        return response;
    }

    /**
     * Deletes the currently authenticated user.
     * <p>
     * The method validates that the user is authenticated, then deletes the user
     * from the repository.
     * The cached user data is evicted to prevent stale information.
     *
     * @throws ResponseStatusException if the user is not authenticated (HTTP 401)
     * @throws ResponseStatusException if the user does not exist (HTTP 404)
     */
    @Override
    @CacheEvict(value = "user", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public void deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Validate that a real (non-anonymous) user is authenticated
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login or register");
        }

        // Extract username from authentication
        String username = authentication.getName();

        // Delete user by username or return 404 if not found
        userRepository.findByUsername(username)
                .ifPresentOrElse(
                        userRepository::delete,
                        () -> {
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                        });
    }

    private void saveAllergies(User user, UserDto userDto) {
        if (userDto.getAllergies() == null || userDto.getAllergies().isEmpty()) {
            return;
        }

        for (String allergyName : userDto.getAllergies()) {
            // Normalize allergy name
            allergyName = allergyName.trim().toLowerCase();
            if (allergyName.isEmpty())
                continue;

            // Find or create Allergy
            String finalAllergyName = allergyName;
            Allergy allergy = allergyRepository.findByName(allergyName)
                    .orElseGet(() -> {
                        Allergy newAllergy = Allergy.builder().name(finalAllergyName).build();
                        return allergyRepository.save(newAllergy);
                    });

            // Link User to Allergy if not already linked
            userAllergyRepository.findByUserAndAllergy(user, allergy)
                    .orElseGet(() -> userAllergyRepository.save(
                            UserAllergy.builder()
                                    .user(user)
                                    .allergy(allergy)
                                    .build()));
        }
    }

}
