package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.dto.UserDto;
import com.nelani.recipe_search_backend.response.LoginResponse;
import com.nelani.recipe_search_backend.response.UserResponse;
import com.nelani.recipe_search_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
@Tag(name = "User Management Controller", description = "Endpoints for retrieving, updating, and deleting user profiles.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get current authenticated user", description = "Retrieves the profile information of the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "Current user retrieved successfully")
    @GetMapping("/user/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user details", description = "Updates the profile information of the authenticated user.")
    @ApiResponse(responseCode = "200", description = "User details updated successfully")
    @PutMapping("/user/update")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDto userDto) {
        LoginResponse response = userService.updateUserDetails(userDto);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Delete current user", description = "Deletes the authenticated user's account from the system.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @DeleteMapping("/user/delete")
    @PreAuthorize("hasAuthority('user:delete')")
    public ResponseEntity<Void> deleteUser() {
        userService.deleteUser();
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Request email change", description = "Sends a verification token to the new email address for confirmation.")
    @ApiResponse(responseCode = "200", description = "Verification email sent successfully")
    @PostMapping("/user/email/request")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> updateEmailRequest(@RequestParam String newEmail) {
        userService.changeEmailRequest(newEmail);
        return ResponseEntity.ok().body(Map.of("message", "Verification email sent successfully to " + newEmail));
    }

    @Operation(summary = "Verify email change", description = "Verifies the token and updates the user's email.")
    @ApiResponse(responseCode = "200", description = "Email updated successfully")
    @PostMapping("/user/email/verify")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<LoginResponse> verifyEmailChangeRequest(@RequestParam String token) {
        LoginResponse response = userService.verifyChangeEmailRequest(token);
        return ResponseEntity.ok(response);
    }

}
