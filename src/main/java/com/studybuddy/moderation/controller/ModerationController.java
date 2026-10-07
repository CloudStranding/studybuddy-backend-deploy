package com.studybuddy.moderation.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import com.studybuddy.moderation.dto.ReportRequest;
import com.studybuddy.moderation.dto.ReportResponse;
import com.studybuddy.moderation.model.Report;
import com.studybuddy.moderation.repository.ReportRepository;

// ModerationController facilitates the platform's safety and reporting mechanisms.
// It implements the requirements for "Moderation" and "Real-Time Flagging" defined in Section 3.2.6.
// This controller allows students to report misconduct and enables administrators to take corrective actions.
@RestController
@RequestMapping("/api/moderation")
public class ModerationController {

    // Dependency on ReportRepository for managing user-submitted misconduct reports.
    @Autowired
    private ReportRepository reportRepository;

    // Dependency on UserRepository to apply moderation actions directly to user account profiles.
    @Autowired
    private UserRepository userRepository;

    // Submits a new misconduct report based on user interaction during or after a session.
    // Aligns with the "Reporting" logic where users can flag buddies for inappropriate behavior.
    @PostMapping("/report")
    public ResponseEntity<?> submitReport(@RequestBody ReportRequest request) {
        // Create a new report instance and populate it with data from the request DTO.
        Report report = new Report();
        report.setReportingUserId(request.getReportingUserId());
        report.setReportedUserId(request.getReportedUserId());
        report.setMeetingId(request.getMeetingId());
        report.setReason(request.getReason());

        // Persist the report into the database with a default "PENDING" status.
        reportRepository.save(report);
        // Confirmation to the reporting user that their flag has been successfully recorded.
        return ResponseEntity.ok("Report submitted successfully");
    }

    // Applies a specific administrative action to a user account based on a moderator's decision.
    // This supports the tiered sanction system (Kick, 3-Day Ban, Permanent Ban) defined in Section 5.
    @PutMapping("/action/{userId}/{action}")
    public ResponseEntity<?> applyAdminAction(@PathVariable Long userId,
                                              @PathVariable String action) {
        // Verify the existence of the reported user before attempting to apply a sanction.
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

        User user = optionalUser.get();

        // Switch logic to handle different levels of severity as per the platform policy[cite: 5].
        switch (action) {
            case "BAN_PERMANENT":
                // MOD-8, MOD-9: Sets the permanent ban flag on the user's profile.
                user.setBanned(true);
                break;
            case "BAN_3_DAYS":
                // MOD-7, MOD-11: Restricts account access for a specific 72-hour window.
                user.setBannedUntil(LocalDateTime.now().plusDays(3));
                break;
            case "KICK":
                // Kicks the user by applying a very short-term restriction, effectively ending their current session.
                user.setBannedUntil(LocalDateTime.now().plusSeconds(30));
                break;
            default:
                // Error handling for unrecognized administrative commands.
                return ResponseEntity.badRequest().body("Unknown action: " + action);
        }

        // Save the updated user profile to persist the ban or restriction.
        userRepository.save(user);

        // Update all pending reports for this user to "ACTIONED" to maintain an clean moderation queue.
        markReportsForUser(userId, "ACTIONED");
        return ResponseEntity.ok("Action applied: " + action);
    }

    // Updates the internal status of a single report to either 'ACTIONED' or 'DISMISSED'.
    // This allows moderators to manage their workflow without necessarily banning a user.
    @PutMapping("/report/{reportId}/status/{status}")
    public ResponseEntity<?> updateReportStatus(@PathVariable Long reportId,
                                                @PathVariable String status) {
        // Validation of status transitions to ensure data consistency in the moderation log.
        if (!"ACTIONED".equals(status) && !"DISMISSED".equals(status)) {
            return ResponseEntity.badRequest().body("Invalid status. Use ACTIONED or DISMISSED.");
        }

        // Fetch the report record to be updated.
        Optional<Report> opt = reportRepository.findById(reportId);
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().body("Report not found");
        }

        Report report = opt.get();
        // Transition the report status.
        report.setStatus(status);
        reportRepository.save(report);
        return ResponseEntity.ok("Report status updated to " + status);
    }

    // Retrieves a comprehensive list of all reports for display in the Administrator Dashboard (UI-5.1).
    // The method enriches raw report data with actual usernames for better readability for moderators.
    @GetMapping("/reports")
    public ResponseEntity<?> getAllReports() {
        List<Report> allReports = reportRepository.findAll();
        List<ReportResponse> enriched = new ArrayList<>();

        // Map each report to a response DTO while resolving human-readable usernames.
        for (Report r : allReports) {
            String reportingName = resolveUsername(r.getReportingUserId());
            String reportedName = resolveUsername(r.getReportedUserId());

            enriched.add(new ReportResponse(
                    r.getId(),
                    r.getReportingUserId(), reportingName,
                    r.getReportedUserId(), reportedName,
                    r.getMeetingId(), r.getReason(),
                    r.getStatus(), r.getTimestamp()
            ));
        }

        // Return the full list of enriched reports to the admin frontend.
        return ResponseEntity.ok(enriched);
    }

    // Helper: Resolves a numeric User ID into a string username to provide context to administrators.
    // Handles edge cases such as deleted accounts or null references gracefully.
    private String resolveUsername(Long userId) {
        if (userId == null) return "Unknown";
        return userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("Deleted User #" + userId);
    }

    // Helper: Bulk updates the status of all pending reports for a specific user.
    // This is typically called after an administrator has applied a global ban or restriction.
    private void markReportsForUser(Long reportedUserId, String status) {
        List<Report> reports = reportRepository.findAll();
        for (Report r : reports) {
            // Only update "PENDING" reports to avoid overwriting previously dismissed actions.
            if (reportedUserId.equals(r.getReportedUserId()) && "PENDING".equals(r.getStatus())) {
                r.setStatus(status);
                reportRepository.save(r);
            }
        }
    }
}