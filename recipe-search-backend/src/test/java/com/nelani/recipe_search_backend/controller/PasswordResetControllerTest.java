package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.dto.ChangePasswordDto;
import com.nelani.recipe_search_backend.dto.PasswordResetDto;
import com.nelani.recipe_search_backend.security.JwtService;
import com.nelani.recipe_search_backend.service.PasswordResetService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PasswordController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PasswordResetControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private PasswordResetService passwordResetService;

        @MockitoBean
        private JwtService jwtService;

        @Test
        public void PasswordResetController_CreateResetToken_ReturnsString() throws Exception {
                // Arrange
                doNothing().when(passwordResetService).createPasswordResetToken(any(String.class));

                // Act
                var response = mockMvc.perform(post("/api/password/reset/request-reset")
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("email", "test-email@test.co.za"));

                // Assert
                response.andExpect(status().isCreated())
                                .andExpect(content().string(
                                                "Password reset token has been sent successfully to test-email@test.co.za."));
        }

        @Test
        public void PasswordResetController_Reset_ReturnsString() throws Exception {
                // Arrange
                doNothing().when(passwordResetService).resetPassword(any(PasswordResetDto.class));

                // Act
                var response = mockMvc.perform(post("/api/password/reset/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "token": "1234567",
                                                  "email": "test-email@test.co.za",
                                                  "newPassword": "Password@123",
                                                  "repeatPassword": "Password@123"
                                                }
                                                """));

                // Assert
                response.andExpect(status().isOk())
                                .andExpect(content().string(
                                                "Your password has been successfully reset. You can now log in with your new password."));
        }

        @Test
        public void PasswordResetController_ChangePassword_ReturnsString() throws Exception {
                // Arrange
                doNothing().when(passwordResetService).changePassword(any(ChangePasswordDto.class));

                // Act
                var response = mockMvc.perform(post("/api/password/change-password")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "oldPassword": "Password@12",
                                                  "newPassword": "Password@123",
                                                  "repeatPassword": "Password@123"
                                                }
                                                """));

                // Assert
                response.andExpect(status().isOk())
                                .andExpect(content().string(
                                                "Your password has been successfully updated."));
        }

}
