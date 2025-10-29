package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.dto.LoginUserDto;
import com.nelani.recipe_search_backend.dto.RegisterUserDto;
import com.nelani.recipe_search_backend.dto.VerifyUserDto;
import com.nelani.recipe_search_backend.response.LoginResponse;

public interface AuthenticationService {
    void signup(RegisterUserDto userDto);

    LoginResponse login(LoginUserDto loginUserDto);

    LoginResponse verifyUser(VerifyUserDto verifyUserDto);

    void resendVerificationCode(String email);
}
