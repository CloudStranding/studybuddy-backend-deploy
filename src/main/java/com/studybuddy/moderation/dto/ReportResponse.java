package com.studybuddy.moderation.dto;

import java.time.LocalDateTime;

// ReportResponse is an enriched Data Transfer Object (DTO) used to display report details in the Admin UI.
// It fulfills the requirements for the Moderator Dashboard described in Section 5 of the Requirements Document.
// This class combines raw report data with human-readable usernames to facilitate quick decision-making by moderators.
public class ReportResponse {

    // Unique identifier for the report record in the database.
    private Long id;

    // The internal ID of the user who filed the complaint.
    private Long reportingUserId;

    // The display name of the reporting user, resolved for administrative clarity.
    private String reportingUsername;

    // The internal ID of the student being accused of misconduct.
    private Long reportedUserId;

    // The display name of the accused user, allowing moderators to identify the individual as per Section 3.2.1.
    private String reportedUsername;

    // The identifier of the specific Focus Session where the incident took place.
    private String meetingId;

    // The specific description of the violation (e.g., Harassment, Inappropriate Behavior).
    private String reason;

    // The current state of the report, such as "PENDING", "ACTIONED", or "DISMISSED".
    private String status;

    // The precise date and time when the report was recorded in the system.
    private LocalDateTime timestamp;

    // Constructor to initialize the response with full context for the Moderation Dashboard.
    // This mapping ensures the frontend receives all data necessary to render the moderation list view.
    public ReportResponse(Long id, Long reportingUserId, String reportingUsername,
                          Long reportedUserId, String reportedUsername,
                          String meetingId, String reason, String status,
                          LocalDateTime timestamp) {
        this.id = id;
        this.reportingUserId = reportingUserId;
        this.reportingUsername = reportingUsername;
        this.reportedUserId = reportedUserId;
        this.reportedUsername = reportedUsername;
        this.meetingId = meetingId;
        this.reason = reason;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Returns the unique report ID.
    public Long getId() {
        return id;
    }

    // Returns the ID of the whistleblower.
    public Long getReportingUserId() {
        return reportingUserId;
    }

    // Returns the name of the whistleblower for the admin interface.
    public String getReportingUsername() {
        return reportingUsername;
    }

    // Returns the ID of the subject of the report.
    public Long getReportedUserId() {
        return reportedUserId;
    }

    // Returns the name of the subject for the admin interface.
    public String getReportedUsername() {
        return reportedUsername;
    }

    // Returns the session ID context for the reported incident.
    public String getMeetingId() {
        return meetingId;
    }

    // Returns the detailed reason for the flag.
    public String getReason() {
        return reason;
    }

    // Returns the current workflow status of the report.
    public String getStatus() {
        return status;
    }

    // Returns the time the report was created.
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}