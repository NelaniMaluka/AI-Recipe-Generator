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

        sendResetEmail(email, token);
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
        String email = passwordResetDto.getEmail();
        log.info("Starting password reset process for email: {}", email);

        // Retrieve user
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Password reset failed: User not found for email: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        // Retrieve and validate token
        var passwordReset = passwordResetRepository.findByUserAndToken(user, passwordResetDto.getToken())
                .orElseThrow(() -> {
                    log.warn("Invalid or missing token for user with email: {}", email);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "No active password reset found. Request a new one.");
                });

        if (passwordReset.getExpiryDate().isBefore(LocalDateTime.now())) {
            log.warn("Expired token used for password reset by email: {}", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password reset token has expired.");
        }

        if (!passwordResetDto.getNewPassword().equals(passwordResetDto.getRepeatPassword())) {
            log.warn("Password mismatch during reset for email: {}", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
        }

        if (passwordEncoder.matches(passwordResetDto.getNewPassword(), user.getPassword())) {
            log.warn("User attempted to reuse old password during reset: {}", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "New password cannot be the same as the old password.");
        }

        // Update password and mark token as used
        passwordResetRepository.delete(passwordReset);
        user.setPassword(passwordEncoder.encode(passwordResetDto.getNewPassword()));
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

        // Ensure the provided passwords match
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getRepeatPassword())) {
            log.warn("Password change failed for user '{}': provided passwords do not match.", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
        }

        // Ensure the new password is different from the current one
        if (passwordEncoder.matches(changePasswordDto.getNewPassword(), user.getPassword())) {
            log.warn("Password change failed for user '{}': new password matches the old password.", email);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "New password cannot be the same as the old password.");
        }

        // Update password and save user
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
        log.info("Password successfully changed for user '{}'.", email);
    }

    /**
     * Sends a password reset email with a verification code to the specified user
     * email.
     *
     * <p>
     * The email contains an HTML message with the reset code and instructions.
     *
     * @param email the recipient's email address
     * @param token the password reset verification code
     */
    private void sendResetEmail(String email, String token) {
        String subject = "AI Recipe Generator - Verification code";

        String htmlContent = "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "  <head>"
                + "    <meta charset='UTF-8' />"
                + "    <meta name='viewport' content='width=device-width, initial-scale=1.0' />"
                + "    <title>Password Reset</title>"
                + "  </head>"
                + "  <body style='font-family: Arial, sans-serif; color: #333; background: #f9f9f9; margin: 0; padding: 0;'>"
                + "    <div style='max-width: 500px; width: 100%; margin: auto; background: #fff; border-radius: 10px; overflow: hidden;'>"
                + "      <div style='text-align: center; padding: 20px; background: #2e86c1; color: white;'>"
                + "        <h2 style='margin: 0;'>AI Recipe Generator</h2>"
                + "      </div>"
                + "      <div style='text-align:center; padding: 20px;'>"
                + "        <img src='https://github.com/NelaniMaluka/AI-Recipe-Generator/blob/main/recipe-search-backend/images/logo.png' alt='AI Recipe Generator Logo' "
                + "             style='width: 120px; height: auto; margin-bottom: 20px;'/>"
                + "      </div>"
                + "      <div style='padding: 0 20px 40px 20px;'>"
                + "        <h3 style='color: #2e86c1;'>Reset Your Password</h3>"
                + "        <p style='line-height: 1.6;'>We received a request to reset your password for <strong>AI Recipe Generator</strong>. "
                + "        Use the code below to set a new password:</p>"
                + "        <div style='text-align: center; margin: 30px 0;'>"
                + "          <p style='font-size: 26px; font-weight: bold; color: #2e86c1; letter-spacing: 3px;'>"
                + "            " + token
                + "          </p>"
                + "        </div>"
                + "        <p style='line-height: 1.6;'>Enter this code in the app or website to reset your password. "
                + "        This code will expire in 30 minutes.</p>"
                + "        <hr style='margin: 30px 0; border: none; border-top: 1px solid #ccc' />"
                + "        <p style='font-size: 13px; color: #666; text-align: center;'>If you did not request a password reset, you can safely ignore this email.</p>"
                + "        <p style='font-size: 12px; color: #999; text-align: center; margin-top: 15px;'>© "
                + "            " + java.time.Year.now().getValue() + " AI Recipe Generator – All rights reserved.</p>"
                + "      </div>"
                + "    </div>"
                + "  </body>"
                + "</html>";

        // Email the validation token to the provided email
        emailService.sendEmail(email, subject, htmlContent);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(9000000) + 100000;
        return String.valueOf(code);
    }
}
