package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.dto.UserDto;
import com.nelani.recipe_search_backend.response.LoginResponse;
import com.nelani.recipe_search_backend.response.UserResponse;

public interface UserService {
    UserResponse getCurrentUser();

    LoginResponse updateUserDetails(UserDto userDto);

    void deleteUser();

    void changeEmailRequest(String newEmail);

    LoginResponse verifyChangeEmailRequest(String token);
}
