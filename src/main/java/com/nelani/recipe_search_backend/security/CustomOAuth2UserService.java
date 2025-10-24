package com.nelani.recipe_search_backend.security;

import com.nelani.recipe_search_backend.model.Provider;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * CustomOAuth2UserService handles authentication of users via OAuth2 providers
 * such as Google or GitHub.
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Extracts user info from the OAuth2 provider response</li>
 * <li>Checks whether a user already exists in the database</li>
 * <li>Ensures user logs in with the correct provider</li>
 * <li>Registers a new user if not already present</li>
 * <li>Generates a JWT token after successful authentication</li>
 * </ul>
 *
 * This service returns a {@link CustomOAuth2User} containing JWT token which
 * can be used for further authorization.
 */
@Service
@Log4j2
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public CustomOAuth2UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    /**
     * Loads user details from OAuth2 provider and handles registration or login.
     *
     * @param userRequest contains the OAuth2 provider details
     * @return an authenticated {@link CustomOAuth2User} containing JWT token
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        log.info("Starting OAuth2 login process for provider: {}",
                userRequest.getClientRegistration().getRegistrationId());

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        String email;
        String firstName;
        String lastName;

        User user;

        // Extract attributes based on provider
        if (provider.equals("google")) {
            email = oAuth2User.getAttribute("email");
            firstName = oAuth2User.getAttribute("given_name");
            lastName = oAuth2User.getAttribute("family_name");

            log.debug("Google OAuth user info retrieved: email={}, firstName={}, lastName={}", email, firstName,
                    lastName);

            user = buildUser(email, firstName, lastName, Provider.GOOGLE);

        } else if (provider.equals("github")) {
            email = oAuth2User.getAttribute("email");
            String fullName = oAuth2User.getAttribute("name");

            if (fullName == null || fullName.isEmpty()) {
                log.error("GitHub OAuth did not return a valid full name");
                throw new IllegalArgumentException("Full name from GitHub OAuth is missing");
            }

            String[] splitName = fullName.split(" ", 2);
            firstName = splitName[0];
            lastName = splitName.length > 1 ? splitName[1] : "";

            log.debug("GitHub OAuth user info retrieved: email={}, firstName={}, lastName={}", email, firstName,
                    lastName);

            user = buildUser(email, firstName, lastName, Provider.GITHUB);
        } else {
            log.error("Unsupported OAuth2 provider attempted: {}", provider);
            throw new IllegalArgumentException("Unsupported OAuth2 provider: " + provider);
        }

        // Check if the user exists
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            user = optionalUser.get();

            // Ensure the user logs in using correct provider
            if (user.getProvider() != providerFromEnum(provider)) {
                log.warn("User {} attempted to log in using {} instead of their registered provider {}",
                        email, provider, user.getProvider());

                throw new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_provider",
                                "Please login using your registered provider: " + user.getProvider(),
                                null));
            }

            log.info("Existing user {} logged in using provider {}", email, provider);
        } else {
            log.info("New OAuth2 user detected. Registering: {}", email);
            userRepository.save(user);
        }

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user);
        log.info("JWT token successfully generated for user: {}", email);

        // Return custom OAuth2 user with token
        return new CustomOAuth2User(oAuth2User.getAuthorities(), oAuth2User.getAttributes(), "email", jwtToken);
    }

    private User buildUser(String email, String firstName, String lastName, Provider provider) {
        return User.builder()
                .firstname(firstName)
                .lastname(lastName)
                .email(email)
                .provider(provider)
                .enabled(true)
                .build();
    }

    private Provider providerFromEnum(String provider) {
        return switch (provider.toLowerCase()) {
            case "google" -> Provider.GOOGLE;
            case "github" -> Provider.GITHUB;
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }
}
