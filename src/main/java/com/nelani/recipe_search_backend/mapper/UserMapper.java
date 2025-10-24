package com.nelani.recipe_search_backend.mapper;

import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.model.UserAllergy;
import com.nelani.recipe_search_backend.response.UserResponse;

import java.util.List;

public class UserMapper {

    public static UserResponse mapUserWithAllDetails(User user, List<UserAllergy> userAllergies) {
        List<String> allergies = userAllergies.stream()
                .map(ua -> ua.getAllergy().getName())
                .toList();

        return UserResponse.builder()
                .publicId(user.getUsername())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .allergies(allergies)
                .build();
    }
}
