package com.nelani.recipe_search_backend.security;

import com.nelani.recipe_search_backend.model.Provider;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LogManager.getLogger(CustomSuccessHandler.class);

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public CustomSuccessHandler(UserRepository userRepository, @Lazy JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        logger.info("OAuth authentication successful");

        Object principal = authentication.getPrincipal();
        String email;
        String firstName;
        String lastName;
        String rawName = "";
        String provider; // Default

        // --- Handle both OIDC and OAuth2 users ---
        if (principal instanceof OidcUser oidcUser) {
            email = oidcUser.getEmail();
            rawName = oidcUser.getFullName();
            firstName = oidcUser.getGivenName();
            lastName = oidcUser.getFamilyName();
            provider = "google";
        } else if (principal instanceof OAuth2User oauthUser) {
            email = oauthUser.getAttribute("email");
            rawName = oauthUser.getAttribute("name");
            firstName = oauthUser.getAttribute("given_name");
            lastName = oauthUser.getAttribute("family_name");
            provider = "google";
        } else {
            provider = "google";
            lastName = "";
            firstName = "";
            email = null;
            logger.error("Unsupported principal type: {}", principal.getClass());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unsupported OAuth principal type");
            return;
        }

        logger.debug("OAuth user details - email: {}, firstName: {}, lastName: {}", email, firstName, lastName);

        if (email == null) {
            logger.error("Email not provided by OAuth provider");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not provided by OAuth provider");
            return;
        }

        // --- Find or create user ---
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user = existingUser.orElseGet(() -> {
            logger.info("Creating new user for email: {}", email);
            User newUser = User.builder()
                    .email(email)
                    .firstname(firstName != null ? firstName : "")
                    .lastname(lastName != null ? lastName : "")
                    .provider(providerFromString(provider))
                    .enabled(true)
                    .build();

            // Generate the publicId for the user
            String publicId;
            do {
                int randomNumber = ThreadLocalRandom.current().nextInt(10000000, 100000000);
                publicId = "user-" + randomNumber;
            } while (userRepository.existsByUsername(publicId));

            newUser.setPublicId(publicId);
            logger.debug("Assigned publicId: {}", publicId);

            return userRepository.save(newUser);
        });

        logger.info("User login completed for: {}", user.getEmail());

        // --- Generate JWT token ---
        String token = jwtService.generateToken(user);
        logger.debug("JWT token generated for user: {}", user.getEmail());

        // --- Send token as JSON response ---
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"token\":\"" + token + "\"}");
        response.getWriter().flush();

        logger.info("JWT token sent in response for user: {}", user.getEmail());
    }

    private Provider providerFromString(String provider) {
        return switch (provider.toLowerCase()) {
            case "google", "oidc_user" -> Provider.GOOGLE;
            case "github" -> Provider.GITHUB;
            default -> {
                logger.error("Unsupported provider: {}", provider);
                throw new IllegalArgumentException("Unsupported provider: " + provider);
            }
        };
    }
}
