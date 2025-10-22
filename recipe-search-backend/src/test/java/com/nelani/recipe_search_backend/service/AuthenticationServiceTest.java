package com.nelani.recipe_search_backend.service;

import com.nelani.recipe_search_backend.dto.LoginUserDto;
import com.nelani.recipe_search_backend.dto.RegisterUserDto;
import com.nelani.recipe_search_backend.dto.VerifyUserDto;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.model.UserVerification;
import com.nelani.recipe_search_backend.model.VerificationType;
import com.nelani.recipe_search_backend.notifications.EmailService;
import com.nelani.recipe_search_backend.repository.UserAllergyRepository;
import com.nelani.recipe_search_backend.repository.UserRepository;
import com.nelani.recipe_search_backend.repository.UserVerificationRepository;
import com.nelani.recipe_search_backend.security.JwtService;
import com.nelani.recipe_search_backend.service.serviceImpl.AuthenticationServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class AuthenticationServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private UserVerificationRepository userVerificationRepository;

        @Mock
        private UserAllergyRepository userAllergyRepository;

        @Mock
        private EmailService emailService;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private JwtService jwtService;

        @Mock
        private AuthenticationManager authenticationManager;

        @InjectMocks
        private AuthenticationServiceImpl authService;

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
        public void UserService_Signup_SendEmail() {
                // Arrange
                RegisterUserDto dto = new RegisterUserDto();
                dto.setFirstname("firstname");
                dto.setLastname("lastname");
                dto.setEmail("test-email@test.co.za");
                dto.setPassword("password123");

                when(userRepository.findByEmail(dto.getEmail()))
                                .thenReturn(Optional.empty());
                when(passwordEncoder.encode(dto.getPassword()))
                                .thenReturn("encodedPassword");

                // Act
                authService.signup(dto);

                // Assert
                verify(emailService, Mockito.times(1))
                                .sendEmail(Mockito.eq(dto.getEmail()), Mockito.anyString(), Mockito.anyString());
        }

        @Test
        public void UserService_Login_ReturnToken() {
                // Arrange
                LoginUserDto dto = LoginUserDto.builder()
                                .email("test-email@test.co.za")
                                .password("password123")
                                .build();

                user.setEnabled(true);
                when(userRepository.findByEmail(dto.getEmail()))
                                .thenReturn(Optional.of(user));
                when(jwtService.generateToken(user))
                                .thenReturn("token");

                Authentication authenticationMock = mock(Authentication.class);

                when(authenticationManager.authenticate(
                                argThat(token -> token.getPrincipal().equals(dto.getEmail()) &&
                                                token.getCredentials().equals(dto.getPassword()))))
                                .thenReturn(authenticationMock);

                var response = authService.login(dto);

                // Assert
                Assertions.assertThat(response.getToken()).isEqualTo("token");
                Assertions.assertThat(response.getUser().getEmail()).isEqualTo("test-email@test.co.za");
                Assertions.assertThat(response.getUser().getFirstname()).isEqualTo("firstname");
                Assertions.assertThat(response.getUser().getLastname()).isEqualTo("lastname");
        }

        @Test
        public void UserService_VerifyUser_ReturnToken() {
                // Arrange
                VerifyUserDto dto = VerifyUserDto.builder()
                                .token("verification-token")
                                .email("test-email@test.co.za")
                                .build();

                UserVerification userVerification = UserVerification.builder()
                                .token("verification-token")
                                .user(user)
                                .type(VerificationType.EMAIL)
                                .expiryDate(LocalDateTime.now().plusMinutes(15))
                                .build();

                user.setEnabled(true);
                when(userRepository.findByEmail(dto.getEmail()))
                                .thenReturn(Optional.of(user));
                when(userVerificationRepository.findByToken(dto.getToken()))
                                .thenReturn(Optional.of(userVerification));
                when(jwtService.generateToken(user))
                                .thenReturn("token");

                var response = authService.verifyUser(dto);

                // Assert
                Assertions.assertThat(response.getToken()).isEqualTo("token");
                Assertions.assertThat(response.getUser().getEmail()).isEqualTo("test-email@test.co.za");
                Assertions.assertThat(response.getUser().getFirstname()).isEqualTo("firstname");
                Assertions.assertThat(response.getUser().getLastname()).isEqualTo("lastname");
        }

        @Test
        public void UserService_ResendVerification_SendEmail() {

                when(userRepository.findByEmail("test-email@test.co.za"))
                                .thenReturn(Optional.of(user));

                // Act
                authService.resetVerificationCode("test-email@test.co.za");

                // Assert
                verify(emailService, Mockito.times(1))
                                .sendEmail(Mockito.eq("test-email@test.co.za"), Mockito.anyString(),
                                                Mockito.anyString());
        }

}
