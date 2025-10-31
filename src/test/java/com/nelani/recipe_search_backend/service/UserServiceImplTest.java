package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.dto.UserDto;
import com.nelani.recipe_search_backend.model.EmailVerification;
import com.nelani.recipe_search_backend.model.Provider;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.model.VerificationType;
import com.nelani.recipe_search_backend.notifications.EmailService;
import com.nelani.recipe_search_backend.repository.*;
import com.nelani.recipe_search_backend.response.LoginResponse;
import com.nelani.recipe_search_backend.response.UserResponse;
import com.nelani.recipe_search_backend.security.JwtService;
import com.nelani.recipe_search_backend.sockets.UserSocket;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.nelani.recipe_search_backend.service.serviceImpl.UserServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAllergyRepository userAllergyRepository;

    @Mock
    private UserVerificationRepository userVerificationRepository;

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @Mock
    private EmailVerificationRepository emailVerificationRepository;

    @Mock
    private RecipeViewRepository recipeViewRepository;

    @Mock
    private RecipeLikeRepository recipeLikeRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserSocket userSocket;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void init() {
        user = User.builder()
                .firstname("firstname")
                .lastname("lastname")
                .email("test-email@test.co.za")
                .provider(Provider.LOCAL)
                .password("Password@123")
                .build();
    }

    @Test
    public void UserService_GetCurrentUser_ReturnsUserResponse() {
        // Arrange: set up dummy authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Stub DB calls
        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userAllergyRepository.findByUser(user)).thenReturn(new ArrayList<>());

        // Act
        var response = userService.getCurrentUser();

        // Assert
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getFirstname()).isEqualTo(user.getFirstname());
        Assertions.assertThat(response.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void UserService_UpdateUserDetails_ReturnsUserResponse() {
        // Arrange: set up dummy authentication
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Stub DB calls
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userAllergyRepository.findByUser(user)).thenReturn(new ArrayList<>());
        when(jwtService.generateToken(user)).thenReturn("token");
        doNothing().when(userSocket).sendUpdatedUser(any(UserResponse.class));

        UserDto dto = UserDto.builder()
                .firstname("firstname")
                .lastname("lastname")
                .allergies(List.of())
                .build();

        // Act
        var response = userService.updateUserDetails(dto);

        // Assert
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getToken()).isEqualTo("token");
        Assertions.assertThat(response.getUser().getFirstname()).isEqualTo(user.getFirstname());
        Assertions.assertThat(response.getUser().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void UserService_DeleteUser_DeletesUserSuccessfully() {
        // Arrange
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordResetRepository.deleteByUser(any(User.class))).thenReturn(0);
        when(userAllergyRepository.deleteByUser(any(User.class))).thenReturn(0);
        when(userVerificationRepository.deleteByUser(any(User.class))).thenReturn(0);

        // Act
        userService.deleteUser();

        // Assert
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    public void UserService_ChangeEmailRequest_SendEmail() {
        // Arrange
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth);

        EmailVerification verification = EmailVerification.builder()
                .user(user)
                .token("token123")
                .newEmail("test-email@test123.co.za")
                .type(VerificationType.EMAIL)
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUser(user)).thenReturn(List.of());
        when(emailVerificationRepository.save(any(EmailVerification.class))).thenReturn(verification);

        // Act
        userService.changeEmailRequest("test-email@test123.co.za");

        // Assert
        verify(emailService, times(1))
                .sendEmailChangeVerificationEmail(eq("test-email@test123.co.za"), any(String.class));
    }

    @Test
    public void UserService_VerifyChangeEmailRequest_SuccessfullyUpdatesEmail() {
        // Arrange: Mock authenticated user
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Prepare a valid email verification token
        String token = "valid-token";
        EmailVerification emailVerification = EmailVerification.builder()
                .user(user)
                .token(token)
                .newEmail("new-email@test.com")
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();

        // Stub repository interactions
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(emailVerificationRepository.findByUserAndToken(user, token)).thenReturn(Optional.of(emailVerification));
        when(jwtService.generateToken(user)).thenReturn("new-token");
        when(userAllergyRepository.findByUser(user)).thenReturn(List.of()); // Assuming empty allergies list

        // Act
        LoginResponse response = userService.verifyChangeEmailRequest(token);

        // Assert: email was updated and saved
        assertEquals("new-email@test.com", user.getEmail());
        verify(userRepository, times(1)).save(user);
        verify(emailVerificationRepository, times(1)).delete(emailVerification);

        // Assert: token is generated
        assertEquals("new-token", response.getToken());
        assertEquals(86400000, response.getExpiresIn());

        // Assert: Socket broadcast triggered
        verify(userSocket, times(1)).sendUpdatedUser(response.getUser());
    }

}
