package com.studybuddy.task.dto;

// CreateTaskRequest is a Data Transfer Object (DTO) used to capture the details of a new user goal.
// It supports the "Task Management" requirement during a Focus Session as defined in Section 3.2.3.
// This request allows users to define specific objectives they intend to accomplish while studying.
public class CreateTaskRequest {

    // The mandatory headline or name of the study objective.
    // This title is displayed on the Focus Session screen (UI-3.1) to remind the user of their goal.
    // As per the system logic, this field is required to successfully persist a new task record.
    private String title;

    // An optional descriptive note providing more context or sub-steps for the study task.
    // This field allows users to break down complex topics into manageable units of work.
    private String note;

    // Standard getter for the task's main title.
    // The TaskController uses this to populate the primary identity of the Task entity.
    public String getTitle() {
        return title;
    }

    // Standard setter for the task's title.
    // This is typically populated from the "Add Task" modal in the Study Buddy mobile app.
    public void setTitle(String title) {
        this.title = title;
    }

    // Standard getter for the task's supplementary notes.
    // This allows the system to store additional details without cluttering the main task list view.
    public String getNote() {
        return note;
    }

    // Standard setter for the task's notes.
    // If provided by the user, these details are persisted alongside the title for history tracking.
    public void setNote(String note) {
        this.note = note;
    }
}