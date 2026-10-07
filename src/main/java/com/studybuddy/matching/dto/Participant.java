package com.studybuddy.matching.dto;

// Participant is a simplified Data Transfer Object (DTO) used to represent a user within a study session.
// It aligns with the Requirements Document Section 3.2.3 regarding the Focus Session interface.
// This class ensures that only essential identification data is shared between buddies during a live session.
public class Participant {

    // The unique identifier of the user, used for internal linking and session management.
    // This ID helps the frontend distinguish between the local user and the matched buddy.
    private final Long id;

    // The display name of the participant as seen on the Focus Session screen (UI-3.1).
    // Providing the username allows buddies to identify each other during their synchronized study period.
    private final String username;

    // Constructor to initialize the participant with their core identity attributes.
    // The use of final fields ensures that participant data remains consistent throughout the session's lifecycle.
    public Participant(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    // Standard getter for the participant's unique ID.
    // This is used by the client-side logic to handle user-specific actions like reporting or rating (Section 3.2.6).
    public Long getId() {
        return id;
    }

    // Standard getter for the participant's username.
    // The frontend retrieves this string to render the "Buddy Name" label in the active study room.
    public String getUsername() {
        return username;
    }
}
