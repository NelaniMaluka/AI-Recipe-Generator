package com.nelani.recipe_search_backend.controller;

import com.nelani.recipe_search_backend.dto.UserDto;
import com.nelani.recipe_search_backend.response.LoginResponse;
import com.nelani.recipe_search_backend.response.UserResponse;
import com.nelani.recipe_search_backend.security.JwtService;
import com.nelani.recipe_search_backend.service.UserService;
import com.nelani.recipe_search_backend.sockets.UserSocket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UserControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private UserService userService;

        @MockitoBean
        private UserSocket userSocket;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private UserDetailsService userDetailsService;

        @Test
        public void UserController_GetCurrentUser_ReturnsUserResponse() throws Exception {
                // Arrange
                UserResponse user = UserResponse.builder()
                                .firstname("firstname")
                                .lastname("lastname")
                                .email("test-email@test.co.za")
                                .publicId("PublicId")
                                .allergies(new ArrayList<>())
                                .build();

                Authentication auth = new UsernamePasswordAuthenticationToken(user.getPublicId(), null,
                                new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(auth);

                when(userService.getCurrentUser()).thenReturn(user);

                // Act
                var response = mockMvc.perform(get("/api/user/me")
                                .contentType(MediaType.APPLICATION_JSON));

                // Assert
                response.andExpect(status().isOk())
                                .andExpect(jsonPath("$.publicId").value(user.getPublicId()))
                                .andExpect(jsonPath("$.email").value(user.getEmail()))
                                .andExpect(jsonPath("$.firstname").value(user.getFirstname()))
                                .andExpect(jsonPath("$.lastname").value(user.getLastname()));
        }

        @Test
        public void UserController_updateUserDetails_ReturnsLoginResponse() throws Exception {
                // Arrange
                UserResponse user = UserResponse.builder()
                                .firstname("firstname")
                                .lastname("lastname")
                                .email("test-email@test.co.za")
                                .publicId("PublicId")
                                .allergies(new ArrayList<>())
                                .build();

                LoginResponse loginResponse = LoginResponse.builder()
                                .token("token")
                                .expiresIn(1000)
                                .user(user)
                                .build();

                UserDto dto = UserDto.builder()
                                .firstname("firstname1")
                                .lastname("lastname2")
                                .allergies(List.of())
                                .build();

                Authentication auth = new UsernamePasswordAuthenticationToken(user.getPublicId(), null,
                                new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(auth);

                when(userService.updateUserDetails(dto)).thenReturn(loginResponse);
                doNothing().when(userSocket).sendUpdatedUser(any(UserResponse.class));

                // Act
                var response = mockMvc.perform(put("/api/user/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "firstname": "firstname1",
                                                  "lastname": "lastname2",
                                                  "allergies": []
                                                }
                                                """));

                // Assert
                response.andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value(loginResponse.getToken()))
                                .andExpect(jsonPath("$.expiresIn").value(loginResponse.getExpiresIn()))
                                .andExpect(jsonPath("$.user.publicId").value(user.getPublicId()))
                                .andExpect(jsonPath("$.user.email").value(user.getEmail()))
                                .andExpect(jsonPath("$.user.firstname").value(user.getFirstname()))
                                .andExpect(jsonPath("$.user.lastname").value(user.getLastname()));
        }

        @Test
        public void UserController_DeleteUser_Returns204() throws Exception {
                // Arrange
                UserResponse user = UserResponse.builder()
                                .firstname("firstname")
                                .lastname("lastname")
                                .email("test-email@test.co.za")
                                .publicId("PublicId")
                                .allergies(new ArrayList<>())
                                .build();

                Authentication auth = new UsernamePasswordAuthenticationToken(user.getPublicId(), null,
                                new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(auth);

                doNothing().when(userService).deleteUser();
                doNothing().when(userSocket).sendUpdatedUser(any(UserResponse.class));

                // Act
                var response = mockMvc.perform(delete("/api/user/delete")
                                .contentType(MediaType.APPLICATION_JSON));

                // Assert
                response.andExpect(status().isNoContent());
        }

        @Test
        public void UserController_UpdateEmailRequest_Returns200() throws Exception {
                // Arrange
                String newEmail = "test-email@test123.co.za";
                doNothing().when(userService).changeEmailRequest(newEmail);

                // Act & Assert
                mockMvc.perform(post("/api/user/email/request")
                                .param("newEmail", newEmail)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message")
                                                .value("Verification email sent successfully to " + newEmail));

                // Verify service method was called
                verify(userService, times(1)).changeEmailRequest(newEmail);
        }

        @Test
        public void UserController_VerifyEmailChange_Returns200() throws Exception {
                // Arrange
                String token = "7439349";

                UserResponse user = UserResponse.builder()
                                .firstname("firstname")
                                .lastname("lastname")
                                .email("test-email@test.co.za")
                                .publicId("PublicId")
                                .allergies(new ArrayList<>())
                                .build();

                LoginResponse loginResponse = LoginResponse.builder()
                                .token("token")
                                .expiresIn(1000)
                                .user(user)
                                .build();

                when(userService.verifyChangeEmailRequest(token)).thenReturn(loginResponse);

                // Act & Assert
                mockMvc.perform(post("/api/user/email/verify")
                                .param("token", token)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("token"))
                                .andExpect(jsonPath("$.expiresIn").value(1000))
                                .andExpect(jsonPath("$.user.email").value("test-email@test.co.za"))
                                .andExpect(jsonPath("$.user.firstname").value("firstname"))
                                .andExpect(jsonPath("$.user.lastname").value("lastname"));

                // Verify service method was called once
                verify(userService, times(1)).verifyChangeEmailRequest(token);
        }

}
