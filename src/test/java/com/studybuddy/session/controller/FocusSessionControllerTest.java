package com.studybuddy.session.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import com.studybuddy.matching.repository.MeetingRepository;
import com.studybuddy.security.JwtUtil;
import com.studybuddy.session.model.FocusSession;
import com.studybuddy.session.repository.FocusSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for FocusSessionController.
 * Tests session lifecycle: start, complete, incomplete, history, and statistics.
 * Uses JWT authentication for all protected endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class FocusSessionControllerTest {

    /** MockMvc instance for simulating HTTP requests. */
    @Autowired
    private MockMvc mockMvc;

    /** ObjectMapper for converting objects to JSON strings. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Repository for user database access during tests. */
    @Autowired
    private UserRepository userRepository;

    /** Repository for session database access during tests. */
    @Autowired
    private FocusSessionRepository sessionRepository;

    /** Repository for meeting database access during tests. */
    @Autowired
    private MeetingRepository meetingRepository;

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

    /**
     * Sets up a test user and generates a JWT token before each test.
     * Clears all related tables to ensure a clean state.
     */
    @BeforeEach
    void setUp() {
        // Delete in correct order to avoid foreign key violations
        sessionRepository.deleteAll();
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
     * Tests that a user can start a new focus session successfully.
     * Expects HTTP 200 and a session ID in the response.
     */
    @Test
    void startSession_Success() throws Exception {
        // Arrange: prepare start session request
        Map<String, Object> request = Map.of(
                "durationMinutes", 30,
                "channelName", "study_30_abc123"
        );

        // Act & Assert: start session and verify session ID is returned
        mockMvc.perform(post("/api/session/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").exists());
    }

    /**
     * Tests that starting a session without token is rejected.
     * Expects HTTP 403 Forbidden.
     */
    @Test
    void startSession_NoToken_ReturnsForbidden() throws Exception {
        // Arrange: prepare start session request
        Map<String, Object> request = Map.of(
                "durationMinutes", 30,
                "channelName", "study_30_abc123"
        );

        // Act & Assert: request without token should be rejected
        mockMvc.perform(post("/api/session/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that a user can complete a session successfully.
     * Expects HTTP 200 and success message.
     */
    @Test
    void completeSession_Success() throws Exception {
        // Arrange: create a session directly in the database
        FocusSession session = new FocusSession();
        session.setUserId(testUser.getId());
        session.setDurationMinutes(30);
        session.setChannelName("study_30_abc123");
        session = sessionRepository.save(session);

        // Act & Assert: complete the session
        mockMvc.perform(put("/api/session/" + session.getId() + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Session marked as completed"));
    }

    /**
     * Tests that completing a non-existent session returns bad request.
     * Expects HTTP 400 and error message.
     */
    @Test
    void completeSession_NotFound_ReturnsBadRequest() throws Exception {
        // Act & Assert: complete non-existent session
        mockMvc.perform(put("/api/session/99999/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Session not found"));
    }

    /**
     * Tests that completing another user's session is rejected.
     * Expects HTTP 403 Forbidden.
     */
    @Test
    void completeSession_NotOwner_ReturnsForbidden() throws Exception {
        // Arrange: create another user and their session
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("Test@123"));
        anotherUser = userRepository.save(anotherUser);

        FocusSession session = new FocusSession();
        session.setUserId(anotherUser.getId());
        session.setDurationMinutes(30);
        session.setChannelName("study_30_xyz789");
        session = sessionRepository.save(session);

        // Act & Assert: try to complete another user's session
        mockMvc.perform(put("/api/session/" + session.getId() + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that a user can mark a session as incomplete successfully.
     * Expects HTTP 200 and success message.
     */
    @Test
    void incompleteSession_Success() throws Exception {
        // Arrange: create a session in the database
        FocusSession session = new FocusSession();
        session.setUserId(testUser.getId());
        session.setDurationMinutes(30);
        session.setChannelName("study_30_abc123");
        session = sessionRepository.save(session);

        // Act & Assert: mark the session as incomplete
        mockMvc.perform(put("/api/session/" + session.getId() + "/incomplete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Session marked as incomplete"));
    }

    /**
     * Tests that marking a non-existent session as incomplete returns bad request.
     * Expects HTTP 400 and error message.
     */
    @Test
    void incompleteSession_NotFound_ReturnsBadRequest() throws Exception {
        // Act & Assert: mark non-existent session as incomplete
        mockMvc.perform(put("/api/session/99999/incomplete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Session not found"));
    }

    /**
     * Tests that a user can retrieve their session history successfully.
     * Expects HTTP 200 and a list of sessions.
     */
    @Test
    void getHistory_Success() throws Exception {
        // Arrange: create some sessions for the test user
        FocusSession session1 = new FocusSession();
        session1.setUserId(testUser.getId());
        session1.setDurationMinutes(30);
        session1.setChannelName("study_30_abc123");
        sessionRepository.save(session1);

        FocusSession session2 = new FocusSession();
        session2.setUserId(testUser.getId());
        session2.setDurationMinutes(60);
        session2.setChannelName("study_60_def456");
        sessionRepository.save(session2);

        // Act & Assert: retrieve history and verify sessions are returned
        mockMvc.perform(get("/api/session/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    /**
     * Tests that history returns empty list when no sessions exist.
     * Expects HTTP 200 and an empty array.
     */
    @Test
    void getHistory_NoSessions_ReturnsEmptyList() throws Exception {
        // Act & Assert: retrieve history with no sessions
        mockMvc.perform(get("/api/session/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * Tests that a user can retrieve their session statistics successfully.
     * Expects HTTP 200 and correct statistics data.
     */
    @Test
    void getStatistics_Success() throws Exception {
        // Arrange: create completed and incomplete sessions
        FocusSession completed = new FocusSession();
        completed.setUserId(testUser.getId());
        completed.setDurationMinutes(30);
        completed.setChannelName("study_30_abc123");
        completed.setCompleted(true);
        sessionRepository.save(completed);

        FocusSession incomplete = new FocusSession();
        incomplete.setUserId(testUser.getId());
        incomplete.setDurationMinutes(60);
        incomplete.setChannelName("study_60_def456");
        incomplete.setCompleted(false);
        sessionRepository.save(incomplete);

        // Act & Assert: verify statistics calculation
        mockMvc.perform(get("/api/session/statistics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.completedCount").value(1));
    }

    /**
     * Tests that statistics returns zero values when no sessions exist.
     * Expects HTTP 200 and zero values for all fields.
     */
    @Test
    void getStatistics_NoSessions_ReturnsZeroValues() throws Exception {
        // Act & Assert: verify zero statistics when no sessions exist
        mockMvc.perform(get("/api/session/statistics")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.completedCount").value(0))
                .andExpect(jsonPath("$.rating").value(0.0));
    }
}