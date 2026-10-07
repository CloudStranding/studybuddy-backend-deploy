package com.studybuddy.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studybuddy.account.dto.DeleteAccountRequest;
import com.studybuddy.account.dto.UpdateProfileRequest;
import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import com.studybuddy.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController.
 * Tests ACC-5 (delete account), ACC-6 (update profile), and get profile endpoints.
 * Uses JWT authentication for protected endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

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

    /** JWT utility for generating test tokens. */
    @Autowired
    private JwtUtil jwtUtil;

    /** Test user used across multiple tests. */
    private User testUser;

    /** JWT token for the test user. */
    private String token;

    /**
     * Sets up a test user and generates a JWT token before each test.
     * Clears the database to ensure a clean state.
     */
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create a test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("Test@123"));
        testUser = userRepository.save(testUser);

        // Generate JWT token for the test user
        token = jwtUtil.generateToken(testUser);
    }

    /**
     * Tests that an authenticated user can retrieve their profile.
     * Expects HTTP 200 and correct user details.
     */
    @Test
    void getProfile_Success() throws Exception {
        // Act & Assert: verify profile is returned correctly
        mockMvc.perform(get("/api/user/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    /**
     * Tests that accessing profile without token is rejected.
     * Expects HTTP 403 Forbidden.
     */
    @Test
    void getProfile_NoToken_ReturnsForbidden() throws Exception {
        // Act & Assert: request without token should be rejected
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isForbidden());
    }

    /**
     * ACC-6: Tests that a user can update their username and email successfully.
     * Expects HTTP 200 and success message.
     */
    @Test
    void updateProfile_Success() throws Exception {
        // Arrange: prepare update request with new details
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newusername");
        request.setEmail("newemail@example.com");

        // Act & Assert
        mockMvc.perform(put("/api/user/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile updated successfully"));
    }

    /**
     * ACC-6: Tests that updating profile fails when username is already taken.
     * Expects HTTP 400 and error message.
     */
    @Test
    void updateProfile_UsernameTaken_ReturnsBadRequest() throws Exception {
        // Arrange: create another user with the desired username
        User anotherUser = new User();
        anotherUser.setUsername("takenusername");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("Test@123"));
        userRepository.save(anotherUser);

        // Attempt to update to taken username
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("takenusername");
        request.setEmail("newemail@example.com");

        // Act & Assert
        mockMvc.perform(put("/api/user/update")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username is already taken"));
    }

    /**
     * ACC-5: Tests that a user can delete their account with correct password.
     * Expects HTTP 200 and success message.
     */
    @Test
    void deleteAccount_Success() throws Exception {
        // Arrange: prepare delete request with correct password
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword("Test@123");

        // Act & Assert
        mockMvc.perform(delete("/api/user/delete")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Account deleted successfully"));
    }

    /**
     * ACC-5: Tests that account deletion fails when password is incorrect.
     * Expects HTTP 400 and error message.
     */
    @Test
    void deleteAccount_WrongPassword_ReturnsBadRequest() throws Exception {
        // Arrange: prepare delete request with wrong password
        DeleteAccountRequest request = new DeleteAccountRequest();
        request.setPassword("WrongPassword@123");

        // Act & Assert
        mockMvc.perform(delete("/api/user/delete")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid password"));
    }
}
