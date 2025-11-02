package com.nelani.recipe_search_backend.service.serviceImpl;

import com.nelani.recipe_search_backend.dto.UserDto;
import com.nelani.recipe_search_backend.mapper.UserMapper;
import com.nelani.recipe_search_backend.model.*;
import com.nelani.recipe_search_backend.notifications.EmailService;
import com.nelani.recipe_search_backend.repository.*;
import com.nelani.recipe_search_backend.response.LoginResponse;
import com.nelani.recipe_search_backend.response.UserResponse;
import com.nelani.recipe_search_backend.security.JwtService;
import com.nelani.recipe_search_backend.service.UserService;
import com.nelani.recipe_search_backend.sockets.UserSocket;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.List;

@Log4j2
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AllergyRepository allergyRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final RecipeLikeRepository recipeLikeRepository;
    private final RecipeViewRepository recipeViewRepository;
    private final EmailService emailService;
    private final UserSocket userSocket;
    private final JwtService jwtService;

    public UserServiceImpl(UserRepository userRepository, AllergyRepository allergyRepository,
            UserAllergyRepository userAllergyRepository, PasswordResetRepository passwordResetRepository,
            UserVerificationRepository userVerificationRepository,
            EmailVerificationRepository emailVerificationRepository, RecipeLikeRepository recipeLikeRepository,
            RecipeViewRepository recipeViewRepository, EmailService emailService, UserSocket userSocket,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.allergyRepository = allergyRepository;
        this.userAllergyRepository = userAllergyRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.userVerificationRepository = userVerificationRepository;
        this.emailVerificationRepository = emailVerificationRepository;
        this.recipeLikeRepository = recipeLikeRepository;
        this.recipeViewRepository = recipeViewRepository;
        this.emailService = emailService;
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
    @Transactional
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
        var user = userRepository.findByEmail(username)
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
    @Transactional
    @CacheEvict(value = "user", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public LoginResponse updateUserDetails(UserDto userDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Validate that a real (non-anonymous) user is authenticated
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Unauthorized attempt to update user details");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login or register");
        }

        // Extract email from authentication
        String email = authentication.getName();
        log.info("User '{}' initiated updateUserDetails request", email);

        // get user by email or return 404 if not found
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User '{}' not found while attempting to update details", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        log.debug("Updating firstname and lastname for user '{}'", email);
        user.setFirstname(userDto.firstname());
        user.setLastname(userDto.lastname());

        log.debug("Saving allergies for user '{}'", email);
        saveAllergies(user, userDto);

        userRepository.save(user);
        log.info("User '{}' details updated successfully", email);

        // Retrieve all allergy associations for the given user
        var allergyList = userAllergyRepository.findByUser(user);

        // Generate a new user response
        LoginResponse response = LoginResponse.builder()
                .token(jwtService.generateToken(user))
                .expiresIn(86400000)
                .user(UserMapper.mapUserWithAllDetails(user, allergyList))
                .build();

        userSocket.sendUpdatedUser(response.user());
        log.info("Updated user '{}' broadcasted through WebSocket", email);

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
    @Transactional
    @CacheEvict(value = "user", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public void deleteUser() {
        log.info("Starting user deletion process.");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Validate that a real (non-anonymous) user is authenticated
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Unauthorized deletion attempt detected.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login or register");
        }

        // Extract email from authentication
        String email = authentication.getName();
        log.debug("Authenticated user: {}", email);

        // Delete user by email or return 404 if not found
        userRepository.findByEmail(email)
                .ifPresentOrElse(user -> {
                    userAllergyRepository.deleteByUser(user);
                    passwordResetRepository.deleteByUser(user);
                    userVerificationRepository.deleteByUser(user);
                    emailVerificationRepository.deleteByUser(user);
                    recipeLikeRepository.deleteByUser(user);
                    recipeViewRepository.deleteByUser(user);
                    userRepository.delete(user);
                    log.info("User '{}' successfully deleted.", email);
                    emailService.sendAccountDeletionEmail(email, user.getFirstname() + " " + user.getLastname());
                },
                        () -> {
                            log.error("Deletion failed: user '{}' not found.", email);
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                        });
    }

    /**
     * Creates a verification request to change the email address of the currently
     * authenticated user.
     * Generates a new verification token, saves it, and sends a verification email
     * to the new address.
     *
     * @param newEmail The new email address requested by the user.
     * @throws ResponseStatusException If the user is not authenticated, doesn't
     *                                 exist,
     *                                 or already has a pending active verification
     *                                 request.
     */
    @Override
    @Transactional
    public void changeEmailRequest(String newEmail) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verify authentication (ensure real logged-in user)
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Unauthorized attempt to request email change to '{}'", newEmail);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login or register");
        }

        // Get current authenticated email
        String currentEmail = authentication.getName();
        log.info("User '{}' initiated email change request to '{}'", currentEmail, newEmail);

        // Retrieve user record
        var user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> {
                    log.error("Authenticated user '{}' not found while attempting email change to '{}'", currentEmail,
                            newEmail);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        // Prevent changing to the same email
        if (currentEmail.equals(newEmail)) {
            log.warn("User '{}' attempted to change email to the same address '{}'", currentEmail, newEmail);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "New email must be different from the current email.");
        }

        LocalDateTime now = LocalDateTime.now();

        // Get active email verification requests
        List<EmailVerification> activeVerifications = emailVerificationRepository.findByUser(user).stream()
                .filter(v -> v.getExpiryDate().isAfter(now))
                .toList();

        if (!activeVerifications.isEmpty()) {
            log.warn("User '{}' attempted to create a new email change request while one is still valid", currentEmail);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A valid verification code already exists for this user.");
        }

        // Optional: Limit the number of changes per 7 days
        long recentResets = emailVerificationRepository.findByUser(user).stream()
                .filter(v -> v.getExpiryDate().isAfter(now.minusDays(7)))
                .count();

        if (recentResets >= 3) {
            log.warn("User '{}' exceeded the maximum number of email change requests in the last 7 days", currentEmail);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "You can only request an email change 3 times every 7 days.");
        }

        // Generate and save a new email verification entry
        String token = generateVerificationCode();
        EmailVerification emailVerification = EmailVerification.builder()
                .type(VerificationType.EMAIL)
                .newEmail(newEmail)
                .token(token)
                .user(user)
                .build();
        emailVerificationRepository.save(emailVerification);
        log.info("New email verification token '{}' generated for user '{}' for new email '{}'", token, currentEmail,
                newEmail);

        // Send verification email
        emailService.sendEmailChangeVerificationEmail(newEmail, token);
        log.info("Verification email sent to '{}' for user '{}'", newEmail, currentEmail);
    }

    /**
     * Verifies an email change request using a provided token.
     * If the token is valid, unused, unexpired, and belongs to the authenticated
     * user,
     * their email address is updated and the verification record is removed.
     *
     * @param token The verification token used to confirm the email change request.
     * @throws ResponseStatusException If the user is not authenticated, the token
     *                                 is invalid,
     *                                 expired, already used, or does not belong to
     *                                 the user.
     */
    @Override
    @Transactional
    @CacheEvict(value = "user", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public LoginResponse verifyChangeEmailRequest(String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Ensure there is a valid-authenticated user
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Unauthorized attempt to change email using token: {}", token);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login or register");
        }

        // Extract authenticated user's current email
        String currentEmail = authentication.getName();
        log.info("User '{}' attempting to verify an email change using token: {}", currentEmail, token);

        // Load user from repository (should exist if authenticated, but validated for
        // safety)
        var user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> {
                    log.error("Authenticated user '{}' not found in the system during email change verification",
                            currentEmail);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        // Load token details from database, error if not found
        var emailValidation = emailVerificationRepository.findByUserAndToken(user, token)
                .orElseThrow(() -> {
                    log.warn("Invalid or unknown token '{}' used by user '{}'", token, currentEmail);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid or unknown token");
                });

        // Ensure the token is not expired
        if (emailValidation.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("User '{}' attempted to use expired token '{}'", currentEmail, token);
            throw new ResponseStatusException(HttpStatus.GONE, "Verification token has expired");
        }

        // Apply email update and clean token entry
        String newEmail = emailValidation.getNewEmail();
        user.setEmail(newEmail);
        userRepository.save(user);
        emailVerificationRepository.delete(emailValidation);

        // Retrieve all allergy associations for the given user
        var allergyList = userAllergyRepository.findByUser(user);

        // Generate a new user response
        LoginResponse response = LoginResponse.builder()
                .token(jwtService.generateToken(user))
                .expiresIn(86400000)
                .user(UserMapper.mapUserWithAllDetails(user, allergyList))
                .build();

        userSocket.sendUpdatedUser(response.user());
        log.info("Updated user '{}' broadcasted through WebSocket", currentEmail);

        return response;
    }

    private void saveAllergies(User user, UserDto userDto) {
        if (userDto.allergies() == null || userDto.allergies().isEmpty()) {
            log.debug("No allergies provided for user '{}', skipping allergy update", user.getUsername());
            return;
        }

        log.info("Processing {} allergy(ies) for user '{}'", userDto.allergies().size(), user.getUsername());

        for (String allergyName : userDto.allergies()) {
            // Normalize allergy name
            allergyName = allergyName.trim().toLowerCase();
            if (allergyName.isEmpty()) {
                log.debug("Skipped empty allergy entry for user '{}'", user.getUsername());
                continue;
            }

            log.debug("Checking if allergy '{}' exists in database", allergyName);

            // Find or create Allergy
            String finalAllergyName = allergyName;
            Allergy allergy = allergyRepository.findByName(allergyName)
                    .orElseGet(() -> {
                        log.info("Allergy '{}' not found, creating new record", finalAllergyName);
                        Allergy newAllergy = Allergy.builder().name(finalAllergyName).build();
                        return allergyRepository.save(newAllergy);
                    });

            // Link User to Allergy if not already linked
            userAllergyRepository.findByUserAndAllergy(user, allergy)
                    .orElseGet(() -> {
                        log.info("Linking allergy '{}' to user '{}'", finalAllergyName, user.getUsername());
                        return userAllergyRepository.save(
                                UserAllergy.builder()
                                        .user(user)
                                        .allergy(allergy)
                                        .build());
                    });
        }

        log.info("Finished saving allergies for user '{}'", user.getUsername());
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(9000000) + 100000;
        return String.valueOf(code);
    }

}
