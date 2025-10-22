package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.dto.ChangePasswordDto;
import com.nelani.recipe_search_backend.dto.PasswordResetDto;
import com.nelani.recipe_search_backend.service.PasswordResetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
@Validated
public class PasswordController {

    private final PasswordResetService passwordResetService;

    public PasswordController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/reset/request-reset")
    public ResponseEntity<?> createResetToken(
            @RequestParam @NotBlank(message = "Email cannot be blank") @Email(message = "Invalid email format") String email) {
        passwordResetService.createPasswordResetToken(email);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(String.format("Password reset token has been sent successfully to %s.", email));
    }

    @PostMapping("/reset/change-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetDto passwordResetDto) {
        passwordResetService.resetPassword(passwordResetDto);
        return ResponseEntity.ok()
                .body("Your password has been successfully reset. You can now log in with your new password.");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordDto changePasswordDto) {
        passwordResetService.changePassword(changePasswordDto);
        return ResponseEntity.ok()
                .body("Your password has been successfully updated.");
    }

}
