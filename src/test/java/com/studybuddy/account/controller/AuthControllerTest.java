package com.studybuddy.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studybuddy.account.dto.LoginRequest;
import com.studybuddy.account.dto.RegisterRequest;
import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AuthController.
 * Tests ACC-1 (register), ACC-2 (login), and ACC-3 (logout) endpoints.
 * Uses MockMvc to simulate HTTP requests without starting a real server.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    /** MockMvc instance for simulating HTTP requests. */
    @Autowired
    private MockMvc mockMvc;

    /** ObjectMapper for converting objects to JSON strings. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Repository for direct database access during tests. */
    @Autowired
    private UserRepository userRepository;

    /** Password encoder for creating test users with hashed passwords. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Clears the database before each test to ensure a clean state.
     * Removes all users except any pre-initialized admin accounts.
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    /**
     * ACC-1: Tests that a new user can register successfully.
     * Expects HTTP 200 and success message.
     */
    @Test
    void register_Success() throws Exception {
        // Arrange: prepare a valid register request
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("Test@123");

        // Act & Assert: send POST request and verify response
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Account created successfully"));
    }

    /**
     * ACC-1: Tests that registration fails when email is already taken.
     * Expects HTTP 400 and error message.
     */
    @Test
    void register_EmailAlreadyExists_ReturnsBadRequest() throws Exception {
        // Arrange: create existing user with same email
        User existingUser = new User();
        existingUser.setUsername("existinguser");
        existingUser.setEmail("test@example.com");
        existingUser.setPassword(passwordEncoder.encode("Test@123"));
        userRepository.save(existingUser);

        // Attempt to register with same email
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setEmail("test@example.com");
        request.setPassword("Test@123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is already registered"));
    }

    /**
     * ACC-1: Tests that registration fails when username is already taken.
     * Expects HTTP 400 and error message.
     */
    @Test
    void register_UsernameAlreadyExists_ReturnsBadRequest() throws Exception {
        // Arrange: create existing user with same username
        User existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("Test@123"));
        userRepository.save(existingUser);

        // Attempt to register with same username
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("new@example.com");
        request.setPassword("Test@123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already taken"));
    }

    /**
     * ACC-2: Tests that a user can login successfully with correct credentials.
     * Expects HTTP 200 and a valid JWT token in the response.
     */
    @Test
    void login_Success() throws Exception {
        // Arrange: create a user in the database
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("Test@123"));
        userRepository.save(user);

        // Prepare login request
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("Test@123");

        // Act & Assert: verify token is returned
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    /**
     * ACC-2: Tests that login fails when user does not exist.
     * Expects HTTP 400 and error message.
     */
    @Test
    void login_UserNotFound_ReturnsBadRequest() throws Exception {
        // Arrange: prepare login request for non-existent user
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("nonexistent");
        request.setPassword("Test@123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }

    /**
     * ACC-2: Tests that login fails when password is incorrect.
     * Expects HTTP 400 and error message.
     */
    @Test
    void login_WrongPassword_ReturnsBadRequest() throws Exception {
        // Arrange: create user with known password
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("Test@123"));
        userRepository.save(user);

        // Attempt login with wrong password
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("testuser");
        request.setPassword("WrongPassword@123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid password"));
    }

    /**
     * ACC-2: Tests that login fails when account is permanently banned.
     * Expects HTTP 400 and error message.
     */
    @Test
    void login_BannedAccount_ReturnsBadRequest() throws Exception {
        // Arrange: create a banned user
        User user = new User();
        user.setUsername("banneduser");
        user.setEmail("banned@example.com");
        user.setPassword(passwordEncoder.encode("Test@123"));
        user.setBanned(true);
        userRepository.save(user);

        // Attempt login
        LoginRequest request = new LoginRequest();
        request.setUsernameOrEmail("banneduser");
        request.setPassword("Test@123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Account is permanently banned"));
    }

    /**
     * ACC-3: Tests that logout endpoint returns success message.
     * JWT is stateless so logout just confirms the client should discard the token.
     */
    @Test
    void logout_Success() throws Exception {
        // Act & Assert: logout should always succeed
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logged out successfully"));
    }
}