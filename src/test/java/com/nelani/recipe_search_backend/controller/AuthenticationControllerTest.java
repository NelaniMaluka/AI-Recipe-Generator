package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.dto.LoginUserDto;
import com.nelani.recipe_search_backend.dto.RegisterUserDto;
import com.nelani.recipe_search_backend.dto.VerifyUserDto;
import com.nelani.recipe_search_backend.response.LoginResponse;
import com.nelani.recipe_search_backend.response.UserResponse;
import com.nelani.recipe_search_backend.security.JwtService;
import com.nelani.recipe_search_backend.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class AuthenticationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private AuthenticationService authenticationService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private UserDetailsService userDetailsService;

  @Test
  public void AuthenticationController_Register_ReturnsString() throws Exception {
    // Arrange
    doNothing().when(authenticationService).signup(any(RegisterUserDto.class));

    // Act
    var response = mockMvc.perform(post("/api/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "firstname": "firstname",
              "lastname": "lastname",
              "email": "test-email@test.co.za",
              "password": "Password@123"
            }
            """));

    // Assert
    response.andExpect(status().isCreated())
        .andExpect(content().string(
            "We have sent a verification email to your account. Please authenticate your email."));
  }

  @Test
  public void AuthenticationController_Login_ReturnsLoginResponse() throws Exception {
    // Arrange
    UserResponse userResponse = UserResponse.builder()
        .firstname("firstname")
        .lastname("lastname")
        .build();

    LoginResponse loginResponse = LoginResponse.builder()
        .token("token")
        .expiresIn(1000)
        .user(userResponse)
        .build();

    when(authenticationService.login(any(LoginUserDto.class))).thenReturn(loginResponse);

    // Act
    var response = mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "email": "test-email@test.co.za",
              "password": "Password@123"
            }
            """));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("token"))
        .andExpect(jsonPath("$.expiresIn").value(1000))
        .andExpect(jsonPath("$.user.firstname").value("firstname"))
        .andExpect(jsonPath("$.user.lastname").value("lastname"));
  }

  @Test
  public void AuthenticationController_Verify_ReturnsLoginResponse() throws Exception {
    // Arrange
    UserResponse userResponse = UserResponse.builder()
        .firstname("firstname")
        .lastname("lastname")
        .build();

    LoginResponse loginResponse = LoginResponse.builder()
        .token("token")
        .expiresIn(1000)
        .user(userResponse)
        .build();

    when(authenticationService.verifyUser(any(VerifyUserDto.class))).thenReturn(loginResponse);

    // Act
    var response = mockMvc.perform(post("/api/auth/verify")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "token": "123456",
              "email": "test-email@test.co.za"
            }
            """));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("token"))
        .andExpect(jsonPath("$.expiresIn").value(1000))
        .andExpect(jsonPath("$.user.firstname").value("firstname"))
        .andExpect(jsonPath("$.user.lastname").value("lastname"));
  }

  @Test
  public void AuthenticationController_ResetVerification_ReturnsString() throws Exception {
    // Arrange
    doNothing().when(authenticationService).resendVerificationCode(any(String.class));

    // Act
    var response = mockMvc.perform(post("/api/auth/reset-verification")
        .param("email", "malukanelani@gmail.com"));

    // Assert
    response.andExpect(status().isOk())
        .andExpect(content().string(
            "Successfully sent a new verification code."));
  }
}
