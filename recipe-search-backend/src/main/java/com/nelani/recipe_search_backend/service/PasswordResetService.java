package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.dto.ChangePasswordDto;
import com.nelani.recipe_search_backend.dto.PasswordResetDto;

public interface PasswordResetService {
    void createPasswordResetToken(String email);

    void resetPassword(PasswordResetDto passwordResetDto);

    void changePassword(ChangePasswordDto changePasswordDto);
}
