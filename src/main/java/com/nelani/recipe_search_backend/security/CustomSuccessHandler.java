package com.nelani.recipe_search_backend.security;

import com.nelani.recipe_search_backend.model.Provider;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Lazy;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;

    public CustomSuccessHandler(UserRepository userRepository, @Lazy JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        DefaultOAuth2User oauthUser = (DefaultOAuth2User) authentication.getPrincipal();
        String provider = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("google"); // fallback if needed

        String email = oauthUser.getAttribute("email");
        String rawName = oauthUser.getAttribute("name") != null ? oauthUser.getAttribute("name").toString() : "";
        String firstName = oauthUser.getAttribute("given_name") != null
                ? oauthUser.getAttribute("given_name").toString()
                : rawName.split(" ")[0];
        String lastName = oauthUser.getAttribute("family_name") != null
                ? oauthUser.getAttribute("family_name").toString()
                : (rawName.split(" ").length > 1 ? rawName.split(" ")[1] : "");

        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not provided by OAuth provider");
            return;
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user = existingUser.orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .firstname(firstName)
                    .lastname(lastName)
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
            return userRepository.save(newUser);
        });

        String token = jwtService.generateToken(user);

        // Return token as JSON or redirect to frontend
        response.setContentType("application/json");
        response.getWriter().write("{\"token\":\"" + token + "\"}");
        response.getWriter().flush();
    }

    private Provider providerFromString(String provider) {
        return switch (provider.toLowerCase()) {
            case "google", "oidc_user" -> Provider.GOOGLE;
            case "github" -> Provider.GITHUB;
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }
}
