package com.nelani.recipe_search_backend.mapper;

import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.response.UserResponse;

public class UserMapper {

    public static UserResponse mapUserWithAllDetails(User user) {
        return UserResponse.builder()
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}
