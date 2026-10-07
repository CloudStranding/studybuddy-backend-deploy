package com.studybuddy.matching.controller;

import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import com.studybuddy.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import com.studybuddy.matching.repository.MeetingRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for MatchingController.
 * Tests join and leave meeting endpoints.
 * Uses JWT authentication for protected endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class MatchingControllerTest {

    /** MockMvc instance for simulating HTTP requests. */
    @Autowired
    private MockMvc mockMvc;

    /** Repository for direct database access during tests. */
    @Autowired
    private UserRepository userRepository;

    /** Password encoder for creating test users. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** JWT utility for generating test tokens. */
    @Autowired
    private JwtUtil jwtUtil;

    /** Test user used across multiple tests. */
    private User testUser;

    /** JWT token for the test user. */
    private String token;

    /** Repository for meeting database access during tests. */
    @Autowired
    private MeetingRepository meetingRepository;

    /**
     * Sets up a test user and generates a JWT token before each test.
     * Clears meetings first, then users to avoid foreign key violations.
     */
    @BeforeEach
    void setUp() {
        // Delete meetings first to avoid foreign key constraint violations
        meetingRepository.deleteAll();
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
     * Tests that an authenticated user can join a meeting successfully.
     * Expects HTTP 200 and a valid meeting response.
     */
    @Test
    void joinMeeting_Success() throws Exception {
        // Act & Assert: join a meeting with 30 minute duration
        mockMvc.perform(post("/api/matching/join/30")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.channelName").exists())
                .andExpect(jsonPath("$.duration").value(30));
    }

    /**
     * Tests that joining a meeting without token is rejected.
     * Expects HTTP 403 Forbidden.
     */
    @Test
    void joinMeeting_NoToken_ReturnsForbidden() throws Exception {
        // Act & Assert: request without token should be rejected
        mockMvc.perform(post("/api/matching/join/30"))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that an authenticated user can join meetings with different durations.
     * Expects HTTP 200 for each valid duration.
     */
    @Test
    void joinMeeting_DifferentDurations_Success() throws Exception {
        // Act & Assert: test with 60 minute duration
        mockMvc.perform(post("/api/matching/join/60")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration").value(60));
    }

    /**
     * Tests that an authenticated user can leave a meeting successfully.
     * Expects HTTP 200 and success message.
     */
    @Test
    void leaveMeeting_Success() throws Exception {
        // Arrange: first join a meeting to get channel name
        String response = mockMvc.perform(post("/api/matching/join/30")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract channel name from response
        String channelName = response.split("\"channelName\":\"")[1].split("\"")[0];

        // Act & Assert: leave the meeting
        mockMvc.perform(post("/api/matching/leave/" + channelName)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * Tests that leaving a non-existent meeting still returns success.
     * The controller silently handles this case.
     * Expects HTTP 200.
     */
    @Test
    void leaveMeeting_NonExistentChannel_ReturnsSuccess() throws Exception {
        // Act & Assert: leaving non-existent channel should still succeed
        mockMvc.perform(post("/api/matching/leave/nonexistent_channel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * Tests that leaving a meeting without token is rejected.
     * Expects HTTP 403 Forbidden.
     */
    @Test
    void leaveMeeting_NoToken_ReturnsForbidden() throws Exception {
        // Act & Assert: request without token should be rejected
        mockMvc.perform(post("/api/matching/leave/some_channel"))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that a user joining twice gets moved to the new meeting.
     * The service removes user from existing meeting before joining new one.
     * Expects HTTP 200 for both joins.
     */
    @Test
    void joinMeeting_JoinTwice_Success() throws Exception {
        // First join
        mockMvc.perform(post("/api/matching/join/30")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Second join - should move user to new meeting
        mockMvc.perform(post("/api/matching/join/45")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration").value(45));
    }
}
