package com.studybuddy.task.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import com.studybuddy.matching.repository.MeetingRepository;
import com.studybuddy.moderation.repository.ReportRepository;
import com.studybuddy.security.JwtUtil;
import com.studybuddy.session.repository.FocusSessionRepository;
import com.studybuddy.task.model.Task;
import com.studybuddy.task.repository.TaskRepository;
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
 * Integration tests for TaskController.
 * Tests task creation, retrieval, completion, and deletion endpoints.
 * Uses JWT authentication for all protected endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerTest {

    /** MockMvc instance for simulating HTTP requests. */
    @Autowired
    private MockMvc mockMvc;

    /** ObjectMapper for converting objects to JSON strings. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Repository for user database access during tests. */
    @Autowired
    private UserRepository userRepository;

    /** Repository for task database access during tests. */
    @Autowired
    private TaskRepository taskRepository;

    /** Repository for session database access during tests. */
    @Autowired
    private FocusSessionRepository sessionRepository;

    /** Repository for meeting database access during tests. */
    @Autowired
    private MeetingRepository meetingRepository;

    /** Repository for report database access during tests. */
    @Autowired
    private ReportRepository reportRepository;

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
        taskRepository.deleteAll();
        reportRepository.deleteAll();
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
     * Tests that a user can create a task successfully.
     * Expects HTTP 200 and the created task data.
     */
    @Test
    void createTask_Success() throws Exception {
        // Arrange: prepare create task request
        Map<String, Object> request = Map.of(
                "title", "Complete homework",
                "note", "Chapter 5 exercises"
        );

        // Act & Assert: create task and verify response
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Complete homework"))
                .andExpect(jsonPath("$.note").value("Chapter 5 exercises"));
    }

    /**
     * Tests that creating a task without a title returns bad request.
     * Expects HTTP 400 and error message.
     */
    @Test
    void createTask_EmptyTitle_ReturnsBadRequest() throws Exception {
        // Arrange: prepare request with empty title
        Map<String, Object> request = Map.of(
                "title", "",
                "note", "Some note"
        );

        // Act & Assert: empty title should be rejected
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Task title is required"));
    }

    /**
     * Tests that creating a task without token is rejected.
     * Expects HTTP 403 Forbidden.
     */
    @Test
    void createTask_NoToken_ReturnsForbidden() throws Exception {
        // Arrange: prepare create task request
        Map<String, Object> request = Map.of("title", "Complete homework");

        // Act & Assert: request without token should be rejected
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that a user can retrieve all their tasks successfully.
     * Expects HTTP 200 and a list of tasks.
     */
    @Test
    void getAllTasks_Success() throws Exception {
        // Arrange: create tasks directly in the database
        Task task1 = new Task();
        task1.setUserId(testUser.getId());
        task1.setTitle("Task 1");
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setUserId(testUser.getId());
        task2.setTitle("Task 2");
        taskRepository.save(task2);

        // Act & Assert: retrieve all tasks
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    /**
     * Tests that empty list is returned when no tasks exist.
     * Expects HTTP 200 and empty array.
     */
    @Test
    void getAllTasks_NoTasks_ReturnsEmptyList() throws Exception {
        // Act & Assert: retrieve tasks when none exist
        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    /**
     * Tests that a user can mark a task as complete successfully.
     * Expects HTTP 200 and success message.
     */
    @Test
    void markComplete_Success() throws Exception {
        // Arrange: create a task in the database
        Task task = new Task();
        task.setUserId(testUser.getId());
        task.setTitle("Complete homework");
        task = taskRepository.save(task);

        // Act & Assert: mark task as complete
        mockMvc.perform(put("/api/tasks/" + task.getId() + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Task marked as complete"));
    }

    /**
     * Tests that completing a non-existent task returns bad request.
     * Expects HTTP 400 and error message.
     */
    @Test
    void markComplete_TaskNotFound_ReturnsBadRequest() throws Exception {
        // Act & Assert: complete non-existent task
        mockMvc.perform(put("/api/tasks/99999/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Task not found"));
    }

    /**
     * Tests that completing another user's task is rejected.
     * Expects HTTP 403 Forbidden.
     */
    @Test
    void markComplete_NotOwner_ReturnsForbidden() throws Exception {
        // Arrange: create another user and their task
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("Test@123"));
        anotherUser = userRepository.save(anotherUser);

        Task task = new Task();
        task.setUserId(anotherUser.getId());
        task.setTitle("Another user's task");
        task = taskRepository.save(task);

        // Act & Assert: try to complete another user's task
        mockMvc.perform(put("/api/tasks/" + task.getId() + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that a user can mark a task as incomplete successfully.
     * Expects HTTP 200 and success message.
     */
    @Test
    void markIncomplete_Success() throws Exception {
        // Arrange: create a completed task in the database
        Task task = new Task();
        task.setUserId(testUser.getId());
        task.setTitle("Complete homework");
        task.setCompleted(true);
        task = taskRepository.save(task);

        // Act & Assert: mark task as incomplete
        mockMvc.perform(put("/api/tasks/" + task.getId() + "/incomplete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Task unchecked"));
    }

    /**
     * Tests that a user can delete a task successfully.
     * Expects HTTP 200 and success message.
     */
    @Test
    void deleteTask_Success() throws Exception {
        // Arrange: create a task in the database
        Task task = new Task();
        task.setUserId(testUser.getId());
        task.setTitle("Task to delete");
        task = taskRepository.save(task);

        // Act & Assert: delete task successfully
        mockMvc.perform(delete("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Task deleted"));
    }

    /**
     * Tests that deleting a non-existent task returns bad request.
     * Expects HTTP 400 and error message.
     */
    @Test
    void deleteTask_NotFound_ReturnsBadRequest() throws Exception {
        // Act & Assert: delete non-existent task
        mockMvc.perform(delete("/api/tasks/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Task not found"));
    }

    /**
     * Tests that deleting another user's task is rejected.
     * Expects HTTP 403 Forbidden.
     */
    @Test
    void deleteTask_NotOwner_ReturnsForbidden() throws Exception {
        // Arrange: create another user and their task
        User anotherUser = new User();
        anotherUser.setUsername("anotheruser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("Test@123"));
        anotherUser = userRepository.save(anotherUser);

        Task task = new Task();
        task.setUserId(anotherUser.getId());
        task.setTitle("Another user's task");
        task = taskRepository.save(task);

        // Act & Assert: try to delete another user's task
        mockMvc.perform(delete("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that tasks for a specific session can be retrieved successfully.
     * Expects HTTP 200 and a list of tasks for that session.
     */
    @Test
    void getTasksForSession_Success() throws Exception {
        // Arrange: create tasks linked to a specific session
        Task task = new Task();
        task.setUserId(testUser.getId());
        task.setTitle("Session task");
        task.setSessionId(1L);
        taskRepository.save(task);

        // Act & Assert: retrieve tasks for session
        mockMvc.perform(get("/api/tasks/session/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}