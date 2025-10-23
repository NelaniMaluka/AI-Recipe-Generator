package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.dto.LoginUserDto;
import com.nelani.recipe_search_backend.dto.RegisterUserDto;
import com.nelani.recipe_search_backend.dto.VerifyUserDto;
import com.nelani.recipe_search_backend.response.LoginResponse;
import com.nelani.recipe_search_backend.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication Controller", description = "Handles user registration, login, verification, and token management.")
public class AuthenticationController {

        private final AuthenticationService authService;

        public AuthenticationController(AuthenticationService authService) {
                this.authService = authService;
        }

        @Operation(summary = "Register a new user account", description = "Creates a user and sends a verification email.")
        @ApiResponse(responseCode = "201", description = "User registered successfully")
        @PostMapping("/signup")
        public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
                authService.signup(registerUserDto);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body("We have sent a verification email to your account. Please authenticate your email.");
        }

        @Operation(summary = "Authenticate user and generate token", description = "Validates the provided credentials and returns a JWT token upon successful login.")
        @ApiResponse(responseCode = "200", description = "Login successful, token returned")
        @PostMapping("/login")
        public ResponseEntity<?> login(@Valid @RequestBody LoginUserDto loginUserDto) {
                LoginResponse response = authService.login(loginUserDto);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Verify user account", description = "Verifies a user's account using the provided verification code.")
        @ApiResponse(responseCode = "200", description = "Account successfully verified")
        @PostMapping("/verify")
        public ResponseEntity<?> verifyUser(@Valid @RequestBody VerifyUserDto verifyUserDto) {
                LoginResponse response = authService.verifyUser(verifyUserDto);
                return ResponseEntity.ok(response);
        }

        @Operation(summary = "Resend verification code", description = "Sends a new verification code to the specified email address.")
        @ApiResponse(responseCode = "200", description = "Verification code resent successfully")
        @PostMapping("/reset-verification")
        public ResponseEntity<?> resetVerification(
                        @RequestParam @NotBlank(message = "Email must not be blank") @Email(message = "Email should be valid") String email) {
                authService.resetVerificationCode(email);
                return ResponseEntity.ok("Successfully sent a new verification code.");
        }

}
