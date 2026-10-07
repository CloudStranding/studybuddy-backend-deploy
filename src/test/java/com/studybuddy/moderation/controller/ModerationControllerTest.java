package com.studybuddy.moderation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import com.studybuddy.matching.repository.MeetingRepository;
import com.studybuddy.moderation.model.Report;
import com.studybuddy.moderation.repository.ReportRepository;
import com.studybuddy.security.JwtUtil;
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
 * Integration tests for ModerationController.
 * Tests report submission, admin actions, report status updates, and report retrieval.
 * Uses JWT authentication for all protected endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class ModerationControllerTest {

    /** MockMvc instance for simulating HTTP requests. */
    @Autowired
    private MockMvc mockMvc;

    /** ObjectMapper for converting objects to JSON strings. */
    @Autowired
    private ObjectMapper objectMapper;

    /** Repository for user database access during tests. */
    @Autowired
    private UserRepository userRepository;

    /** Repository for report database access during tests. */
    @Autowired
    private ReportRepository reportRepository;

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

    /** Test reporting user. */
    private User reportingUser;

    /** Test reported user. */
    private User reportedUser;

    /** JWT token for the reporting user. */
    private String token;

    /**
     * Sets up test users and clears database before each test.
     * Deletes in correct order to avoid foreign key violations.
     */
    @BeforeEach
    void setUp() {
        // Delete in correct order to avoid foreign key violations
        reportRepository.deleteAll();
        sessionRepository.deleteAll();
        meetingRepository.deleteAll();
        userRepository.deleteAll();

        // Create reporting user with admin privileges
        reportingUser = new User();
        reportingUser.setUsername("reportinguser");
        reportingUser.setEmail("reporting@example.com");
        reportingUser.setPassword(passwordEncoder.encode("Test@123"));
        reportingUser.setAdmin(true);
        reportingUser = userRepository.save(reportingUser);

        // Create reported user
        reportedUser = new User();
        reportedUser.setUsername("reporteduser");
        reportedUser.setEmail("reported@example.com");
        reportedUser.setPassword(passwordEncoder.encode("Test@123"));
        reportedUser = userRepository.save(reportedUser);

        // Generate JWT token for reporting user
        token = jwtUtil.generateToken(reportingUser);
    }

    /**
     * Tests that a user can submit a report successfully.
     * Expects HTTP 200 and success message.
     */
    @Test
    void submitReport_Success() throws Exception {
        // Arrange: prepare report request
        Map<String, Object> request = Map.of(
                "reportingUserId", reportingUser.getId(),
                "reportedUserId", reportedUser.getId(),
                "meetingId", "study_30_abc123",
                "reason", "Inappropriate behavior"
        );

        // Act & Assert: submit report and verify success
        mockMvc.perform(post("/api/moderation/report")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Report submitted successfully"));
    }

    /**
     * Tests that submitting a report without token is rejected.
     * Expects HTTP 403 Forbidden.
     */
    @Test
    void submitReport_NoToken_ReturnsForbidden() throws Exception {
        // Arrange: prepare report request
        Map<String, Object> request = Map.of(
                "reportingUserId", reportingUser.getId(),
                "reportedUserId", reportedUser.getId(),
                "reason", "Inappropriate behavior"
        );

        // Act & Assert: request without token should be rejected
        mockMvc.perform(post("/api/moderation/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests that an admin can permanently ban a user.
     * Expects HTTP 200 and success message.
     */
    @Test
    void applyAdminAction_BanPermanent_Success() throws Exception {
        // Act & Assert: permanently ban the reported user
        mockMvc.perform(put("/api/moderation/action/"
                        + reportedUser.getId() + "/BAN_PERMANENT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Action applied: BAN_PERMANENT"));
    }

    /**
     * Tests that an admin can apply a 3-day ban to a user.
     * Expects HTTP 200 and success message.
     */
    @Test
    void applyAdminAction_Ban3Days_Success() throws Exception {
        // Act & Assert: apply 3-day ban to the reported user
        mockMvc.perform(put("/api/moderation/action/"
                        + reportedUser.getId() + "/BAN_3_DAYS")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Action applied: BAN_3_DAYS"));
    }

    /**
     * Tests that an admin can kick a user.
     * Expects HTTP 200 and success message.
     */
    @Test
    void applyAdminAction_Kick_Success() throws Exception {
        // Act & Assert: kick the reported user
        mockMvc.perform(put("/api/moderation/action/"
                        + reportedUser.getId() + "/KICK")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Action applied: KICK"));
    }

    /**
     * Tests that applying action to non-existent user returns bad request.
     * Expects HTTP 400 and error message.
     */
    @Test
    void applyAdminAction_UserNotFound_ReturnsBadRequest() throws Exception {
        // Act & Assert: apply action to non-existent user
        mockMvc.perform(put("/api/moderation/action/99999/BAN_PERMANENT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }

    /**
     * Tests that applying an unknown action returns bad request.
     * Expects HTTP 400 and error message.
     */
    @Test
    void applyAdminAction_UnknownAction_ReturnsBadRequest() throws Exception {
        // Act & Assert: apply unknown action
        mockMvc.perform(put("/api/moderation/action/"
                        + reportedUser.getId() + "/UNKNOWN_ACTION")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Unknown action: UNKNOWN_ACTION"));
    }

    /**
     * Tests that a moderator can update report status to ACTIONED.
     * Expects HTTP 200 and success message.
     */
    @Test
    void updateReportStatus_Actioned_Success() throws Exception {
        // Arrange: create a report in the database
        Report report = new Report();
        report.setReportingUserId(reportingUser.getId());
        report.setReportedUserId(reportedUser.getId());
        report.setReason("Inappropriate behavior");
        report = reportRepository.save(report);

        // Act & Assert: update report status to ACTIONED
        mockMvc.perform(put("/api/moderation/report/"
                        + report.getId() + "/status/ACTIONED")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Report status updated to ACTIONED"));
    }

    /**
     * Tests that a moderator can update report status to DISMISSED.
     * Expects HTTP 200 and success message.
     */
    @Test
    void updateReportStatus_Dismissed_Success() throws Exception {
        // Arrange: create a report in the database
        Report report = new Report();
        report.setReportingUserId(reportingUser.getId());
        report.setReportedUserId(reportedUser.getId());
        report.setReason("Inappropriate behavior");
        report = reportRepository.save(report);

        // Act & Assert: update report status to DISMISSED
        mockMvc.perform(put("/api/moderation/report/"
                        + report.getId() + "/status/DISMISSED")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Report status updated to DISMISSED"));
    }

    /**
     * Tests that updating report status with invalid status returns bad request.
     * Expects HTTP 400 and error message.
     */
    @Test
    void updateReportStatus_InvalidStatus_ReturnsBadRequest() throws Exception {
        // Arrange: create a report in the database
        Report report = new Report();
        report.setReportingUserId(reportingUser.getId());
        report.setReportedUserId(reportedUser.getId());
        report.setReason("Inappropriate behavior");
        report = reportRepository.save(report);

        // Act & Assert: update with invalid status
        mockMvc.perform(put("/api/moderation/report/"
                        + report.getId() + "/status/INVALID_STATUS")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid status. Use ACTIONED or DISMISSED."));
    }

    /**
     * Tests that updating non-existent report status returns bad request.
     * Expects HTTP 400 and error message.
     */
    @Test
    void updateReportStatus_ReportNotFound_ReturnsBadRequest() throws Exception {
        // Act & Assert: update non-existent report
        mockMvc.perform(put("/api/moderation/report/99999/status/ACTIONED")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Report not found"));
    }

    /**
     * Tests that all reports can be retrieved successfully.
     * Expects HTTP 200 and a list of reports.
     */
    @Test
    void getAllReports_Success() throws Exception {
        // Arrange: create some reports in the database
        Report report1 = new Report();
        report1.setReportingUserId(reportingUser.getId());
        report1.setReportedUserId(reportedUser.getId());
        report1.setReason("Harassment");
        reportRepository.save(report1);

        Report report2 = new Report();
        report2.setReportingUserId(reportedUser.getId());
        report2.setReportedUserId(reportingUser.getId());
        report2.setReason("Spam");
        reportRepository.save(report2);

        // Act & Assert: retrieve all reports
        mockMvc.perform(get("/api/moderation/reports")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    /**
     * Tests that empty report list is returned when no reports exist.
     * Expects HTTP 200 and empty array.
     */
    @Test
    void getAllReports_NoReports_ReturnsEmptyList() throws Exception {
        // Act & Assert: retrieve reports when none exist
        mockMvc.perform(get("/api/moderation/reports")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}