package com.studybuddy.moderation.dto;

// ReportRequest is a Data Transfer Object (DTO) used to capture misconduct details from a user.
// It directly supports the "Real-Time Flag" and "Reporting" requirements mentioned in Section 3.2.6.
// This object carries the necessary data from the UI to the backend to initiate a moderation review.
public class ReportRequest {

    // The unique identifier of the user who is submitting the report.
    // This allows administrators to track the source of the flag and prevent report spamming.
    private Long reportingUserId;

    // The unique identifier of the user being reported for inappropriate behavior.
    // This ID is used by the moderator to apply sanctions like "BAN_3_DAYS" or "BAN_PERMANENT".
    private Long reportedUserId;

    // The identifier of the specific study session where the incident occurred.
    // Linking the report to a meetingId provides context for the moderator during the investigation.
    private String meetingId;

    // A detailed description or category of the misconduct (e.g., harassment, inappropriate content).
    // This field helps the moderator determine the severity of the action needed.
    private String reason;

    // The exact time when the report was generated on the client side.
    // This is used for logging and auditing purposes within the Moderation Dashboard.
    private Long timestamp;

    // Returns the ID of the user filing the report.
    public Long getReportingUserId() {
        return reportingUserId;
    }

    // Returns the ID of the user being flagged for review.
    public Long getReportedUserId() {
        return reportedUserId;
    }

    // Returns the meeting context associated with this misconduct report.
    public String getMeetingId() {
        return meetingId;
    }

    // Returns the textual reason or category for the report.
    public String getReason() {
        return reason;
    }

    // Returns the epoch timestamp of the report submission.
    public Long getTimestamp() {
        return timestamp;
    }

    // Sets the reporting user's identifier.
    public void setReportingUserId(Long reportingUserId) {
        this.reportingUserId = reportingUserId;
    }

    // Sets the reported user's identifier for moderation tracking.
    public void setReportedUserId(Long reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    // Associates the report with a specific Focus Session ID.
    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    // Sets the specific reason for the moderation request.
    public void setReason(String reason) {
        this.reason = reason;
    }

    // Records the time of the incident for the moderation log.
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}