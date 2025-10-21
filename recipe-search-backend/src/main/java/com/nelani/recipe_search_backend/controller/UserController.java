package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.dto.UserDto;
import com.nelani.recipe_search_backend.response.LoginResponse;
import com.nelani.recipe_search_backend.response.UserResponse;
import com.nelani.recipe_search_backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UserDto userDto) {
        LoginResponse response = userService.updateUserDetails(userDto);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser() {
        userService.deleteUser();
        return ResponseEntity.noContent().build();
    }

}
