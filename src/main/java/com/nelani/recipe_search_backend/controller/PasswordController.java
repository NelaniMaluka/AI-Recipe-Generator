package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.dto.ChangePasswordDto;
import com.nelani.recipe_search_backend.dto.PasswordResetDto;
import com.nelani.recipe_search_backend.response.MessageResponse;
import com.nelani.recipe_search_backend.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "Password Management Controller", description = "Endpoints for password reset, password change, and password token generation.")
public class PasswordController {

        private final PasswordResetService passwordResetService;

        public PasswordController(PasswordResetService passwordResetService) {
                this.passwordResetService = passwordResetService;
        }

        @Operation(summary = "Request password reset token", description = "Generates and sends a password reset token to the specified email address.")
        @ApiResponse(responseCode = "201", description = "Password reset token sent successfully")
        @PostMapping("/public/password/request-reset")
        public ResponseEntity<?> createResetToken(
                        @RequestParam @NotBlank(message = "Email cannot be blank") @Email(message = "Invalid email format") String email) {
                passwordResetService.createPasswordResetToken(email);
                MessageResponse response = new MessageResponse(
                                "Password reset token has been sent successfully.", email);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @Operation(summary = "Reset password using a valid token", description = "Resets the user's password after validating the password reset token.")
        @ApiResponse(responseCode = "200", description = "Password reset successfully")
        @PostMapping("/public/password/reset")
        public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto) {
                passwordResetService.resetPassword(passwordResetDto);
                return ResponseEntity.ok(new MessageResponse(
                                "Your password has been successfully reset. You can now log in with your new password.",
                                null));
        }

        @Operation(summary = "Change password for authenticated user", description = "Allows an authenticated user to update their password without using a reset token.")
        @ApiResponse(responseCode = "200", description = "Password updated successfully")
        @PreAuthorize("hasAuthority('user:write')")
        @PutMapping("/user/password")
        public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
                passwordResetService.changePassword(changePasswordDto);
                return ResponseEntity.ok(new MessageResponse(
                                "Your password has been successfully updated.", null));
        }
}
