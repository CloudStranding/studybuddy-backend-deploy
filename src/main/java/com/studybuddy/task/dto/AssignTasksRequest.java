package com.studybuddy.task.dto;

// AssignTasksRequest is a Data Transfer Object (DTO) used to bulk-associate user tasks
// with a specific study session.
// It supports the "Task Tracking" feature described in Section 3.2.3 of the Requirements Document.
// This request ensures that user achievements are correctly logged under the appropriate session ID.
public class AssignTasksRequest {

    // The unique identifier of the target Focus Session.
    // This ID serves as the foreign key that links a user's task to a specific study interval.
    // It is used by the TaskController to update the 'sessionId' field in the Task entity.
    private Long sessionId;

    // Standard getter for the target session identifier.
    // The backend uses this ID to verify the session's existence before assigning tasks.
    public Long getSessionId() {
        return sessionId;
    }

    // Standard setter for the session identifier.
    // This is typically populated by the frontend when a user selects tasks to include
    // in their current Focus Session (UI-3.1).
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }
}