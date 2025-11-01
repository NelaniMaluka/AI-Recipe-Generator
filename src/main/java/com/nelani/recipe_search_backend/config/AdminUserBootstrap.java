package com.nelani.recipe_search_backend.config;

import com.nelani.recipe_search_backend.model.Provider;
import com.nelani.recipe_search_backend.repository.UserRepository;
import com.nelani.recipe_search_backend.security.ApplicationUserRole;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.concurrent.ThreadLocalRandom;

@Configuration
public class AdminUserBootstrap {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void createAdminUserIfNotExist() {
        String adminEmail = "admin@gmail.com";
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            var adminUser = com.nelani.recipe_search_backend.model.User.builder()
                    .firstname("Admin")
                    .lastname("User")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("Admin123@nelani"))
                    .provider(Provider.LOCAL)
                    .enabled(true)
                    .role(ApplicationUserRole.ADMIN)
                    .build();

            // Generate the publicId for the user
            String publicId;
            do {
                int randomNumber = ThreadLocalRandom.current().nextInt(10000000, 100000000);
                publicId = "user-" + randomNumber;
            } while (userRepository.existsByUsername(publicId));

            adminUser.setPublicId(publicId);

            userRepository.save(adminUser);
            System.out.println("Admin user created at startup: " + adminEmail);
        }
    }
}

