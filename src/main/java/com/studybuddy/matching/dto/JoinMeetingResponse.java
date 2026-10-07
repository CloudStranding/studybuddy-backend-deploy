package com.studybuddy.matching.dto;

import java.util.Set;

// JoinMeetingResponse is a Data Transfer Object (DTO) that provides the necessary metadata
// for a user to successfully connect to a synchronized study session.
// It supports the functional requirement of "Meeting & Matching" described in Section 3.2.2.
// This response bridges the gap between the matching logic and the active Focus Session (Section 3.2.3).
public class JoinMeetingResponse {

    // The unique database identifier for the session, used for internal tracking and logging.
    private final String meetingId;

    // The specific channel identifier required for real-time communication (e.g., via Agora or WebRTC).
    // This allows the frontend to connect both buddies to the same virtual study room.
    private final String channelName;

    // The intended length of the study session in minutes, as selected by the user in UI-2.1.
    // This value is used by the frontend to initialize the countdown timer for the Focus Session.
    private final int duration;

    // A collection of participants currently assigned to this meeting.
    // According to Section 3.2.3, this ensures users can see who they are studying with (the "Buddy").
    private final Set<Participant> participants;

    // Constructor to initialize the response with all mandatory session parameters.
    // The use of 'final' fields ensures immutability, which is a key aspect of thread-safe DTO design.
    public JoinMeetingResponse(String meetingId, String channelName, int duration, Set<Participant> participants) {
        this.meetingId = meetingId;
        this.channelName = channelName;
        this.duration = duration;
        this.participants = participants;
    }

    // Standard getter for the unique meeting ID.
    // Useful for client-side debugging and linking session reports to specific IDs.
    public String getMeetingId() {
        return meetingId;
    }

    // Standard getter for the real-time communication channel name.
    // The frontend uses this string to join the specific audio/video room for the session.
    public String getChannelName() {
        return channelName;
    }

    // Standard getter for the session duration.
    // Allows the client to sync the study clock with the backend's recorded time.
    public int getDuration() {
        return duration;
    }

    // Standard getter for the set of meeting participants.
    // This provides the necessary user details (ID, Username) to display in the Focus Session UI.
    public Set<Participant> getParticipants() {
        return participants;
    }
}