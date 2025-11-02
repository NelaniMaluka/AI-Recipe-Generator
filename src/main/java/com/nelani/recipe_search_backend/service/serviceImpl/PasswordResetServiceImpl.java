package com.nelani.recipe_search_backend.service.serviceImpl;

import com.nelani.recipe_search_backend.dto.ChangePasswordDto;
import com.nelani.recipe_search_backend.dto.PasswordResetDto;
import com.nelani.recipe_search_backend.model.PasswordReset;
import com.nelani.recipe_search_backend.notifications.EmailService;
import com.nelani.recipe_search_backend.repository.PasswordResetRepository;
import com.nelani.recipe_search_backend.repository.UserRepository;
import com.nelani.recipe_search_backend.service.PasswordResetService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Random;

@Log4j2
@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetRepository passwordResetRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetServiceImpl(PasswordResetRepository passwordResetRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder, EmailService emailService) {
        this.passwordResetRepository = passwordResetRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Generates a password reset token for the given email and sends it to the
     * user.
     *
     * <p>
     * If the user does not exist, a NOT_FOUND exception is thrown.
     *
     * @param email the user's email address
     */
    @Override
    @Transactional
    public void createPasswordResetToken(String email) {
        log.info("Attempting to create a password reset token for email: {}", email);

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Password reset failed: user not found for email: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        var userPasswordList = passwordResetRepository.findByUser(user);

        // Count resets in the last 7 days
        long recentResets = userPasswordList.stream()
                .filter(passwordReset -> passwordReset.getExpiryDate()
                        .isAfter(LocalDateTime.now().minusDays(7)))
                .count();

        if (recentResets >= 3) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "You can only change your password 3 times every 7 days.");
        }

        String token = generateVerificationCode();
        PasswordReset passwordReset = PasswordReset.builder()
                .user(user)
                .token(token)
                .build();
        passwordResetRepository.save(passwordReset);

        log.info("Password reset token generated and saved for user with email: {}", email);

        emailService.sendPasswordResetEmail(email, token);
        log.info("Password reset email sent to: {}", email);
    }

    /**
     * Changes a user's password using a valid password reset token.
     *
     * @param passwordResetDto Contains email, token, new password, and repeated
     *                         password.
     * @throws ResponseStatusException If validation fails (user not found, token
     *                                 invalid/expired/used, passwords mismatch).
     */
    @Override
    @Transactional
    public void resetPassword(PasswordResetDto passwordResetDto) {
        String email = passwordResetDto.email();
        log.info("Starting password reset process for email: {}", email);

        // Retrieve user
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Password reset failed: User not found for email: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        // Retrieve and validate token
        var passwordReset = passwordResetRepository.findByUserAndToken(user, passwordResetDto.token())
                .orElseThrow(() -> {
                    log.warn("Invalid or missing token for user with email: {}", email);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "No active password reset found. Request a new one.");
                });

        if (passwordReset.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Expired token used for password reset by email: {}", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset token has expired.");
        }

        if (!passwordResetDto.newPassword().equals(passwordResetDto.repeatPassword())) {
            log.warn("Password mismatch during reset for email: {}", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
        }

        if (passwordEncoder.matches(passwordResetDto.newPassword(), user.getPassword())) {
            log.warn("User attempted to reuse old password during reset: {}", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "New password cannot be the same as the old password.");
        }

        // Update password and mark token as used
        passwordResetRepository.delete(passwordReset);
        user.setPassword(passwordEncoder.encode(passwordResetDto.newPassword()));
        userRepository.save(user);

        log.info("Password reset successful for email: {}", email);
    }

    /**
     * Updates the authenticated user's password after validation.
     *
     * @param changePasswordDto new and repeated password
     * @throws ResponseStatusException if user is unauthorized, not found,
     *                                 passwords don't match, or new password
     *                                 matches the old one
     */
    @Override
    @Transactional
    public void changePassword(ChangePasswordDto changePasswordDto) {
        log.info("Attempting to change password for authenticated user.");

        // Get the current authentication context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Ensure the user is authenticated and not anonymous
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Password change attempt failed: user not authenticated.");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please login or register");
        }

        // Extract email from authenticated context
        String email = authentication.getName();
        log.debug("Authenticated user identified as: {}", email);

        // Fetch user from repository or throw 404 if not found
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Password change failed: user '{}' not found.", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        // Ensure the old password matches the current password
        if (!passwordEncoder.matches(changePasswordDto.oldPassword(), user.getPassword())) {
            log.warn("Password change failed for user '{}': old password does not match.", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect.");
        }

        // Ensure the new password and repeat password match
        if (!changePasswordDto.newPassword().equals(changePasswordDto.repeatPassword())) {
            log.warn("Password change failed for user '{}': provided passwords do not match.", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
        }

        // Ensure the new password is different from the current one
        if (passwordEncoder.matches(changePasswordDto.newPassword(), user.getPassword())) {
            log.warn("Password change failed for user '{}': new password matches the old password.", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "New password cannot be the same as the old password.");
        }

        // Update password and save user
        user.setPassword(passwordEncoder.encode(changePasswordDto.newPassword()));
        userRepository.save(user);
        log.info("Password successfully changed for user '{}'.", email);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(9000000) + 100000;
        return String.valueOf(code);
    }
}
