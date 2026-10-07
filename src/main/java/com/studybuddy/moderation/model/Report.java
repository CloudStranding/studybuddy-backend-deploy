package com.studybuddy.moderation.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

// The Report entity captures and persists misconduct data submitted by users.
// It serves as the primary data source for the Moderation Dashboard described in Section 5.
// Maintaining these records is essential for tracking user behavior and applying sanctions like bans.
@Entity
@Table(name = "reports")
public class Report {

    // Unique identifier for each report, assigned automatically by the database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The unique ID of the user who initiated the report to flag inappropriate behavior.
    private Long reportingUserId;

    // The unique ID of the user accused of misconduct, used for administrative actions.
    private Long reportedUserId;

    // The context of the study session where the incident occurred, as per Focus Session logic.
    private String meetingId;

    // A textual description or category representing the nature of the violation.
    private String reason;

    // The current lifecycle stage of the report.
    // Defaults to "PENDING" until a moderator reviews and updates it to "ACTIONED" or "INVALID".
    private String status = "PENDING";

    // The exact date and time the report was created in the system.
    private LocalDateTime timestamp;

    // Lifecycle callback to automatically set the creation timestamp before the entity is persisted.
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // Returns the unique database ID of the report.
    public Long getId() {
        return id;
    }

    // Returns the identifier of the whistleblower.
    public Long getReportingUserId() {
        return reportingUserId;
    }

    // Returns the identifier of the user being investigated.
    public Long getReportedUserId() {
        return reportedUserId;
    }

    // Returns the session ID associated with the reported misconduct.
    public String getMeetingId() {
        return meetingId;
    }

    // Returns the reason provided for the report.
    public String getReason() {
        return reason;
    }

    // Returns the current moderation status of the report.
    public String getStatus() {
        return status;
    }

    // Returns the time the report record was generated.
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Manually sets the report ID (typically used for testing purposes).
    public void setId(Long id) {
        this.id = id;
    }

    // Configures the ID of the reporting user.
    public void setReportingUserId(Long reportingUserId) {
        this.reportingUserId = reportingUserId;
    }

    // Configures the ID of the reported user.
    public void setReportedUserId(Long reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    // Links the report to a specific meeting or focus session.
    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    // Updates the descriptive reason for the report.
    public void setReason(String reason) {
        this.reason = reason;
    }

    // Updates the workflow status after moderator review.
    public void setStatus(String status) {
        this.status = status;
    }

    // Manually sets the timestamp for the report.
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}