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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Validated
@Tag(name = "User Management Controller", description = "Endpoints for retrieving, updating, and deleting user profiles.")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get current authenticated user", description = "Retrieves the profile information of the currently authenticated user.")
    @ApiResponse(responseCode = "200", description = "Current user retrieved successfully")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user details", description = "Updates the profile information of the authenticated user.")
    @ApiResponse(responseCode = "200", description = "User details updated successfully")
    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDto userDto) {
        LoginResponse response = userService.updateUserDetails(userDto);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Delete current user", description = "Deletes the authenticated user's account from the system.")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser() {
        userService.deleteUser();
        return ResponseEntity.noContent().build();
    }

}
