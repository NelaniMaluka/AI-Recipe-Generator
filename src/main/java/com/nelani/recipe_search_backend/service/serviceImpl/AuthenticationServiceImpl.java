package com.nelani.recipe_search_backend.service.serviceImpl;

import com.nelani.recipe_search_backend.dto.LoginUserDto;
import com.nelani.recipe_search_backend.dto.RegisterUserDto;
import com.nelani.recipe_search_backend.dto.VerifyUserDto;
import com.nelani.recipe_search_backend.mapper.UserMapper;
import com.nelani.recipe_search_backend.model.Provider;
import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.model.UserVerification;
import com.nelani.recipe_search_backend.model.VerificationType;
import com.nelani.recipe_search_backend.notifications.EmailService;
import com.nelani.recipe_search_backend.repository.UserAllergyRepository;
import com.nelani.recipe_search_backend.repository.UserRepository;
import com.nelani.recipe_search_backend.repository.UserVerificationRepository;
import com.nelani.recipe_search_backend.response.LoginResponse;
import com.nelani.recipe_search_backend.security.JwtService;
import com.nelani.recipe_search_backend.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final UserAllergyRepository userAllergyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    public AuthenticationServiceImpl(UserRepository userRepository,
            UserVerificationRepository userVerificationRepository, UserAllergyRepository userAllergyRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.userVerificationRepository = userVerificationRepository;
        this.userAllergyRepository = userAllergyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }

    /**
     * Registers a new user with the provided details.
     * <p>
     * Validates that no existing user has the same email, generates a unique
     * username,
     * creates a verification token, saves the user and verification entity,
     * and sends a verification email.
     *
     * @param userDto DTO containing registration details
     * @throws IllegalArgumentException if a user already exists with the given
     *                                  email
     */
    @Override
    @Transactional
    public void signup(RegisterUserDto userDto) {
        // Check if a user exists with the provided email
        Optional<User> optionalUser = userRepository.findByEmail(userDto.getEmail());
        if (optionalUser.isPresent()) {
            throw new IllegalArgumentException("A user already exists with this email.");
        }

        // Create a new user object
        User user = User.builder()
                .firstname(userDto.getFirstname())
                .lastname(userDto.getLastname())
                .email(userDto.getEmail())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .provider(Provider.LOCAL)
                .build();

        // Generate the publicId for the user
        String publicId;
        do {
            int randomNumber = ThreadLocalRandom.current().nextInt(10000000, 100000000);
            publicId = "user-" + randomNumber;
        } while (userRepository.existsByUsername(publicId));

        user.setPublicId(publicId);

        // Generate new verification entity
        UserVerification verification = UserVerification.builder()
                .token(generateVerificationCode())
                .type(VerificationType.EMAIL)
                .user(user)
                .build();

        // Save and send the verification email
        userRepository.save(user);
        userVerificationRepository.save(verification);
        emailService.sendAccountVerificationEmail(user.getEmail(), verification.getToken());
    }

    /**
     * Authenticates a user with the provided email and password.
     * <p>
     * Validates that the user exists, checks if the account is verified,
     * performs authentication, retrieves associated allergies,
     * and returns a {@link LoginResponse} containing a JWT token and user details.
     *
     * @param loginUserDto DTO containing login credentials (email and password)
     * @return {@link LoginResponse} containing JWT token, expiration, and user
     *         details
     * @throws IllegalArgumentException if the user does not exist or account is not
     *                                  verified
     */
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

        // checks if the account is verified
        if (user.getProvider() != Provider.LOCAL) {
            throw new IllegalStateException(
                    "This account is registered via " + user.getProvider() + ". Please login using that provider.");
        }

        // Authenticates the user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginUserDto.getEmail(),
                        loginUserDto.getPassword()));

        // Retrieve all allergy associations for the given user
        var allergyList = userAllergyRepository.findByUser(user);

        // Generate a new user response
        return LoginResponse.builder()
                .token(jwtService.generateToken(user))
                .expiresIn(86400000)
                .user(UserMapper.mapUserWithAllDetails(user, allergyList)).build();
    }

    /**
     * Verifies a user's account using a token, enabling the user if valid and
     * returning a JWT login response.
     *
     * @param verifyUserDto DTO with email and verification token
     * @return LoginResponse containing JWT token and user info
     * @throws ResponseStatusException  if user not found
     * @throws IllegalArgumentException if token is invalid, expired, already used,
     *                                  or not for email verification
     */
    @Override
    @Transactional
    public LoginResponse verifyUser(VerifyUserDto verifyUserDto) {
        // Check if a user exists with the provided email
        var user = userRepository
                .findByEmail(verifyUserDto.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check if token exists
        var verification = userVerificationRepository
                .findByUserAndToken(user, verifyUserDto.getToken())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Verification failed: the token provided does not exist or is invalid."));

        // Check if the token type matches
        if (verification.getType() != VerificationType.EMAIL) {
            throw new IllegalArgumentException(
                    "Verification failed: the token provided is not valid for email verification.");
        }

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

        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstname() + " " + user.getLastname());

        // Generate a new user response
        return LoginResponse.builder()
                .token(jwtService.generateToken(user))
                .expiresIn(86400000)
                .user(UserMapper.mapUserWithAllDetails(user, List.of())).build();
    }

    /**
     * Generates a new email verification token for a user if no active token
     * exists.
     *
     * @param email the email of the user requesting a new verification token
     * @throws ResponseStatusException  if the user is not found
     * @throws IllegalArgumentException if the account is already verified or an
     *                                  active token exists
     * @throws RuntimeException         if generating or sending the new token fails
     */
    @Override
    @Transactional
    public void resetVerificationCode(String email) {
        // Check if a user exists with the provided email
        var user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (user.isEnabled()) {
            throw new IllegalArgumentException("This account has already been verified. No further action is needed.");
        }

        // Checks if there isn't any active tokens?
        List<UserVerification> userVerificationList = userVerificationRepository.findByUser(user);
        Optional<UserVerification> optionalVerification = userVerificationList.stream()
                .filter(v -> v.getExpiryDate().isAfter(LocalDateTime.now()))
                .findFirst();
        if (optionalVerification.isPresent()) {
            throw new IllegalArgumentException(
                    "You have an active verification token. Please use it or wait for it to expire.");
        }

        try {
            // Generate new verification entity
            UserVerification verification = UserVerification.builder()
                    .token(generateVerificationCode())
                    .type(VerificationType.EMAIL)
                    .user(user)
                    .build();

            // Save the changes
            userVerificationRepository.save(verification);
            emailService.sendAccountVerificationEmail(user.getEmail(), verification.getToken());
        } catch (Exception e) {
            throw new RuntimeException("Failed to reset token");
        }
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(9000000) + 100000;
        return String.valueOf(code);
    }
}
