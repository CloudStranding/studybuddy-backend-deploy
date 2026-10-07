package com.studybuddy.account.service;

import com.studybuddy.account.dto.*;
import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import com.studybuddy.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// AuthService handles the core authentication and authorization logic for the platform.
// It implements the requirements for secure account creation and access control as per Section 3.2.1.
// This service acts as the bridge between raw user requests and the persistent data layer.
@Service
public class AuthService {

    // Repository for performing database operations on the User entity.
    @Autowired
    private UserRepository userRepository;

    // Component used to securely hash and verify passwords using the BCrypt algorithm.
    // This ensures no plain-text credentials are ever stored or compared directly.
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Utility class for generating and validating JSON Web Tokens (JWT) for stateless sessions.
    @Autowired
    private JwtUtil jwtUtil;

    // ACC-1: Register a new user account.
    // This process validates the uniqueness of user identity before persisting a new profile.
    // It aligns with the registration flow described in the Requirements Document.
    public void register(RegisterRequest request) {

        // Check if the provided email is already associated with an existing account.
        // Uniqueness is required to prevent duplicate profiles and ensure clear notification channels.
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        // Check if the requested username is already taken by another student.
        // Usernames must be unique to maintain consistent identity in Focus Sessions (Section 3.2.3).
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        // Create a new User entity and map the fields from the validated registration request.
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // Securely encode the password before saving to fulfill security compliance requirements.
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Persist the new user record into the database.
        userRepository.save(user);
    }

    // ACC-2: Login with username or email and password.
    // This method handles the primary entry point for users to access their dashboard and sessions.
    // It includes security checks for banned or restricted accounts as defined in the Moderation section.
    public LoginResponse login(LoginRequest request) {

        // Search for the user using either their unique username or email address.
        // This flexibility supports the multi-identifier login requirement for better UX.
        User user = userRepository
                .findByUsernameOrEmail(
                        request.getUsernameOrEmail(),
                        request.getUsernameOrEmail()
                )
                .orElseThrow(() -> new RuntimeException("User not found"));

        // MOD-8, MOD-9: Check if the account has been permanently banned by a moderator.
        // Permanent bans restrict all access to the platform due to severe policy violations (Section 5).
        if (user.isBanned()) {
            throw new RuntimeException("Account is permanently banned");
        }

        // MOD-7, MOD-11: Check if the account is under a temporary restriction (e.g., 3-day ban).
        // If the current time is before the 'bannedUntil' timestamp, the login attempt is rejected.
        if (user.getBannedUntil() != null &&
                user.getBannedUntil().isAfter(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Account is temporarily restricted until "
                    + user.getBannedUntil());
        }

        // Verify the provided password against the stored BCrypt hash.
        // If the credentials do not match, an exception is thrown to prevent unauthorized access.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // ACC-3: Generate a JWT token upon successful authentication.
        // This token allows the client to perform subsequent requests without re-authenticating.
        String token = jwtUtil.generateToken(user);

        // Return the session details, including the admin flag to determine UI visibility (ACC-4).
        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.isAdmin()
        );
    }
}