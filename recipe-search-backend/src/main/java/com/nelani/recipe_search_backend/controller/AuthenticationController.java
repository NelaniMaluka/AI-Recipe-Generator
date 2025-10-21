package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.dto.LoginUserDto;
import com.nelani.recipe_search_backend.dto.RegisterUserDto;
import com.nelani.recipe_search_backend.dto.VerifyUserDto;
import com.nelani.recipe_search_backend.response.LoginResponse;
import com.nelani.recipe_search_backend.service.AuthenticationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authService;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserDto registerUserDto) {
        authService.signup(registerUserDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("We have sent a verification email to your account. Please authenticate your email.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginUserDto loginUserDto) {
        LoginResponse response = authService.login(loginUserDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@Valid @RequestBody VerifyUserDto verifyUserDto) {
        LoginResponse response = authService.verifyUser(verifyUserDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-verification")
    public ResponseEntity<?> resetVerification(
            @RequestParam @NotBlank(message = "Email must not be blank") @Email(message = "Email should be valid") String email) {
        authService.resetVerificationCode(email);
        return ResponseEntity.ok("Successfully sent a new verification code.");
    }

}
