package com.studybuddy.session.dto;

// StartSessionRequest is a Data Transfer Object (DTO) used to initialize a formal study record.
// It marks the transition from the "Matching" phase to the "Focus Session" phase.
// This request ensures that the backend can track the specific duration and channel context for each session.
public class StartSessionRequest {

    // The intended length of the study period (e.g., 25, 45, or 60 minutes).
    // This value is used to calculate the session's scheduled end time and verify completion.
    // It must match the duration agreed upon during the buddy matching process.
    private int durationMinutes;

    // The unique identifier for the real-time communication room (e.g., Agora channel).
    // This serves as the primary link between the study session record and the live audio/video session.
    // It ensures that all participants in the same channel are logged under the same meeting context.
    private String channelName;

    // Standard getter for the session's duration.
    // Used by the FocusSessionController to persist the user's study goal for this session.
    public int getDurationMinutes() {
        return durationMinutes;
    }

    // Standard setter for the session's duration.
    // Populated by the frontend when the user successfully matches and moves to the Focus Screen (UI-3.1).
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    // Standard getter for the communication channel name.
    // Provides the necessary reference for tracking logs and managing the session lifecycle.
    public String getChannelName() {
        return channelName;
    }

    // Standard setter for the channel name.
    // Captures the unique channel ID generated during the matching phase.
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}