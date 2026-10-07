package com.studybuddy.matching.dto;

// LeaveMeetingResponse is a Data Transfer Object (DTO) that confirms the result of a disconnection request.
// It supports the functional requirement of "Leaving Early" described in the Requirements Document Section 3.2.3.
// This response ensures the client-side application can gracefully transition back to the dashboard or matching screen.
public class LeaveMeetingResponse {

    // A flag indicating whether the user was successfully removed from the active session on the server.
    // As per the system requirements, this allows the frontend to verify that the study timer has stopped.
    private final boolean success;

    // A descriptive status message detailing the outcome of the leave operation.
    // This provides feedback to the user, confirming that their departure from the Focus Session was acknowledged.
    private final String message;

    // Constructor to initialize the response with the operation status and a descriptive message.
    // The use of final fields maintains immutability, promoting cleaner data flow and thread safety.
    public LeaveMeetingResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Standard getter for the success flag.
    // The frontend uses this to determine if it should clear the local session state.
    public boolean isSuccess() {
        return success;
    }

    // Standard getter for the outcome message.
    // This can be displayed in a toast or snackbar if a specific error or confirmation is needed.
    public String getMessage() {
        return message;
    }
}