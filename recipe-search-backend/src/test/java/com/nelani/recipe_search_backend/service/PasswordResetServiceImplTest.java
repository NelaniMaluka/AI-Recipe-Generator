package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.dto.ChangePasswordDto;
import com.nelani.recipe_search_backend.dto.PasswordResetDto;
import com.nelani.recipe_search_backend.model.PasswordReset;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.notifications.EmailService;
import com.nelani.recipe_search_backend.repository.PasswordResetRepository;
import com.nelani.recipe_search_backend.repository.UserRepository;
import com.nelani.recipe_search_backend.service.serviceImpl.PasswordResetServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class PasswordResetServiceImplTest {

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private User user;

    @BeforeEach
    public void init() {
        user = User.builder()
                .firstname("firstname")
                .lastname("lastname")
                .email("test-email@test.co.za")
                .password("Password@123")
                .build();
    }

    @Test
    public void PasswordResetService_CreatePasswordResetToken_SendEmail() {
        // Arrange
        String email = "test-email@test.co.za";

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        // Act
        passwordResetService.createPasswordResetToken(email);

        // Assert
        verify(emailService, times(1))
                .sendEmail(Mockito.eq(email), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void PasswordResetService_ResetPassword_Success() {
        // Arrange
        PasswordResetDto dto = PasswordResetDto.builder()
                .email(user.getEmail())
                .token("valid-token")
                .newPassword("NewPassword@123")
                .repeatPassword("NewPassword@123")
                .build();

        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setUser(user);
        passwordReset.setToken("valid-token");
        passwordReset.setExpiryDate(LocalDateTime.now().plusMinutes(10)); // valid expiry

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordResetRepository.findByUserAndToken(user, "valid-token")).thenReturn(Optional.of(passwordReset));
        when(passwordEncoder.encode(dto.getNewPassword())).thenReturn("encoded-password");

        // Act
        passwordResetService.resetPassword(dto);

        // Assert
        verify(passwordResetRepository, times(1)).delete(passwordReset);
        verify(userRepository, times(1)).save(user);

        Assertions.assertThat(user.getPassword()).isEqualTo("encoded-password");
    }

    @Test
    void changePassword_SuccessfulChange_UpdatesPassword() {
        // Arrange
        ChangePasswordDto dto = ChangePasswordDto.builder()
                .oldPassword("encoded-old-password")
                .newPassword("NewPass@123")
                .repeatPassword("NewPass@123").build();

        String email = "test-email@test.co.za";

        User user = new User();
        user.setEmail(email);
        user.setPassword("encoded-old-password");

        Authentication auth = Mockito.mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        when(auth.isAuthenticated()).thenReturn(true);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("NewPass@123", user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("NewPass@123")).thenReturn("encoded-new-password");

        // Act
        passwordResetService.changePassword(dto);

        // Assert
        verify(userRepository, times(1)).save(user);
        Assertions.assertThat(user.getPassword()).isEqualTo("encoded-new-password");
    }

}
