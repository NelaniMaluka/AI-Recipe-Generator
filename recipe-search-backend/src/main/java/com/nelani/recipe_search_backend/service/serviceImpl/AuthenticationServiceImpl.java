package com.nelani.recipe_search_backend.service.serviceImpl;

import com.nelani.recipe_search_backend.dto.LoginUserDto;
import com.nelani.recipe_search_backend.dto.RegisterUserDto;
import com.nelani.recipe_search_backend.dto.VerifyUserDto;
import com.nelani.recipe_search_backend.mapper.UserMapper;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.model.UserVerification;
import com.nelani.recipe_search_backend.model.VerificationType;
import com.nelani.recipe_search_backend.notifications.EmailService;
import com.nelani.recipe_search_backend.repository.UserRepository;
import com.nelani.recipe_search_backend.repository.UserVerificationRepository;
import com.nelani.recipe_search_backend.response.LoginResponse;
import com.nelani.recipe_search_backend.security.JwtService;
import com.nelani.recipe_search_backend.service.AuthenticationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationServiceImpl(UserRepository userRepository,
            UserVerificationRepository userVerificationRepository, PasswordEncoder passwordEncoder,
            JwtService jwtService, AuthenticationManager authenticationManager, EmailService emailService) {
        this.userRepository = userRepository;
        this.userVerificationRepository = userVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void signup(RegisterUserDto userDto) {
        // Check if a user exists with the provided email
        Optional<User> optionalUser = userRepository.findByEmail(userDto.getEmail());
        if (optionalUser.isPresent()) {
            throw new IllegalArgumentException("A user already exists with this email.");
        }

        // Create a new user object
        User user = new User();
        user.setFirstname(userDto.getFirstname());
        user.setLastname(userDto.getLastname());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Generate the username for the user
        String username;
        do {
            int randomNumber = ThreadLocalRandom.current().nextInt(10000000, 100000000);
            username = "user-" + randomNumber;
        } while (userRepository.existsByUsername(username));

        user.setUsername(username);

        // Generate new verification entity
        UserVerification verification = new UserVerification();
        verification.setToken(generateVerificationCode());
        verification.setExpiryDate(LocalDateTime.now().plusMinutes(15));
        verification.setType(VerificationType.EMAIL);
        verification.setUser(user);

        // Save and send the verification email
        userRepository.save(user);
        userVerificationRepository.save(verification);
        sendVerificationEmail(user, verification.getToken());
    }

    @Override
    @Transactional
    public LoginResponse login(LoginUserDto loginUserDto) {
        // Check if a user exists with the provided email
        User user = userRepository.findByEmail(loginUserDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // checks if the account is verified
        if (!user.isEnabled()) {
            throw new IllegalArgumentException("Account not verified. Please verify your account.");
        }

        // Authenticates the user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUserDto.getEmail(),
                        loginUserDto.getPassword()));

        // Generate a new user response
        LoginResponse response = new LoginResponse();
        response.setToken(jwtService.generateToken(user));
        response.setExpiresIn(86400000);
        response.setUser(UserMapper.mapUserWithAllDetails(user));
        return response;
    }

    @Override
    @Transactional
    public LoginResponse verifyUser(VerifyUserDto verifyUserDto) {
        // Check if a user exists with the provided email
        Optional<User> optionalUser = userRepository.findByEmail(verifyUserDto.getEmail());
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }

        User user = optionalUser.get();

        // Check if token exists
        Optional<UserVerification> optionalVerification = userVerificationRepository
                .findByToken(verifyUserDto.getToken());
        if (optionalVerification.isEmpty()) {
            throw new IllegalArgumentException("The verification token you provided does not exist or is invalid.");
        }

        UserVerification verification = optionalVerification.get();

        // Check if token expired
        if (verification.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Your verification token has expired. Please request a new token.");
        }

        // Check if token already used
        if (verification.isUsed()) {
            throw new IllegalArgumentException("This verification token has already been used.");
        }

        // Update and save the changes
        user.setEnabled(true);
        verification.setUsed(true);
        verification.setToken(null);

        userRepository.save(user);
        userVerificationRepository.save(verification);

        // Generate a new response
        LoginResponse response = new LoginResponse();
        response.setToken(jwtService.generateToken(user));
        response.setExpiresIn(86400000);
        response.setUser(UserMapper.mapUserWithAllDetails(user));
        return response;
    }

    @Override
    @Transactional
    public void resetVerificationCode(String email) {
        // Check if a user exists with the provided email
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }

        User user = optionalUser.get();

        if (user.isEnabled()) {
            throw new IllegalArgumentException("This account has already been verified. No further action is needed.");
        }

        // Checks if there isn't any active tokens?
        Optional<UserVerification> optionalVerification = user.getVerifications().stream()
                .filter(v -> v.getExpiryDate().isAfter(LocalDateTime.now()))
                .findFirst();
        if (optionalVerification.isPresent()) {
            throw new IllegalArgumentException(
                    "You have an active verification token. Please use it or wait for it to expire.");
        }

        try {
            // Create a new verification entity
            UserVerification verification = new UserVerification();
            verification.setToken(generateVerificationCode());
            verification.setExpiryDate(LocalDateTime.now().plusMinutes(15));
            verification.setType(VerificationType.EMAIL);
            verification.setUser(user);

            // Save the changes
            userVerificationRepository.save(verification);
            sendVerificationEmail(user, verification.getToken());
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset token");
        }
    }

    private void sendVerificationEmail(User user, String token) {
        String subject = "AI Recipe Generator - Verification code";

        String htmlContent = "<!DOCTYPE html>"
                + "<html lang='en'>"
                + "  <head>"
                + "    <meta charset='UTF-8' />"
                + "    <meta name='viewport' content='width=device-width, initial-scale=1.0' />"
                + "    <title>Email Verification</title>"
                + "  </head>"
                + "  <body style='font-family: Arial, sans-serif; color: #333; background: #f9f9f9; margin: 0; padding: 0;'>"
                + "    <div style='max-width: 500px; width: 100%; margin: auto; background: #fff; border-radius: 10px; overflow: hidden;'>"
                + "      <div style='text-align: center; padding: 20px; background: #2e86c1; color: white;'>"
                + "        <h2 style='margin: 0;'>AI Recipe Generator</h2>"
                + "      </div>"
                + "      <div style='text-align:center; padding: 20px;'>"
                + "        <img src='https://github.com/NelaniMaluka/AI-Recipe-Generator/blob/main/recipe-search-backend/images/logo.png' alt='AI Recipe Generator Logo' "
                + "             style='width: 120px; height: auto; margin-bottom: 20px;'/>"
                + "      </div>"
                + "      <div style='padding: 0 20px 40px 20px;'>"
                + "        <h3 style='color: #2e86c1;'>Verify Your Email Address</h3>"
                + "        <p style='line-height: 1.6;'>Thank you for signing up for <strong>AI Recipe Generator</strong>! "
                + "        To complete your registration, please verify your email address using the code below:</p>"
                + "        <div style='text-align: center; margin: 30px 0;'>"
                + "          <p style='font-size: 26px; font-weight: bold; color: #2e86c1; letter-spacing: 3px;'>"
                + "            " + token
                + "          </p>"
                + "        </div>"
                + "        <p style='line-height: 1.6;'>Enter this verification code in the app or website to activate your account.</p>"
                + "        <hr style='margin: 30px 0; border: none; border-top: 1px solid #ccc' />"
                + "        <p style='font-size: 13px; color: #666; text-align: center;'>Didn’t create an account? Please ignore this email.</p>"
                + "        <p style='font-size: 12px; color: #999; text-align: center; margin-top: 15px;'>© "
                + "            " + java.time.Year.now().getValue() + " AI Recipe Generator – All rights reserved.</p>"
                + "      </div>"
                + "    </div>"
                + "  </body>"
                + "</html>";

        // Email the validation token to the provided email
        emailService.sendEmail(user.getEmail(), subject, htmlContent);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(9000000) + 100000;
        return String.valueOf(code);
    }
}
