package com.studybuddy.task.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// The Task entity represents a specific study objective or to-do item created by a user.
// It directly supports the "Task Tracking" feature within the Focus Session module.
// Tasks are used to measure individual productivity during synchronized study sessions with a buddy.
@Entity
@Table(name = "tasks")
public class Task {

    // Unique database identifier for the specific task record.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key reference to the User who owns this task.
    // This ensures that tasks are private to the user and can be retrieved for their personal dashboard.
    @Column(nullable = false)
    private Long userId;

    // Optional reference to a FocusSession ID.
    // When a task is completed during a study period, it is linked to a session for historical reporting.
    // This supports the "History Details" requirement where users see what they achieved per session.
    private Long sessionId;

    // The primary description or title of the study objective (e.g., "Complete Calculus Homework").
    // As per the system logic, this field is mandatory for creating a valid task.
    @Column(nullable = false)
    private String title;

    // Additional descriptive text or context for the task.
    // Useful for storing sub-tasks, textbook page numbers, or specific instructions.
    private String note;

    // Flag indicating whether the task has been accomplished.
    // Completion of tasks during a session is a key indicator of study effectiveness.
    private Boolean completed;

    // Timestamp recording when the task was initially created.
    // Automatically populated via the @PrePersist callback to maintain an accurate audit trail.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Lifecycle callback to automatically set the creation date before the entity is persisted.
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Returns the unique task identifier.
    public Long getId() {
        return id;
    }

    // Returns the ID of the user who created this objective.
    public Long getUserId() {
        return userId;
    }

    // Sets the user identifier for this task.
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Returns the ID of the focus session this task is associated with.
    public Long getSessionId() {
        return sessionId;
    }

    // Links this task to a specific focus session context.
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    // Returns the task's descriptive title.
    public String getTitle() {
        return title;
    }

    // Updates the title of the study objective.
    public void setTitle(String title) {
        this.title = title;
    }

    // Returns the supplementary notes for the task.
    public String getNote() {
        return note;
    }

    // Sets or updates additional task details.
    public void setNote(String note) {
        this.note = note;
    }

    // Returns the current completion status.
    public Boolean getCompleted() {
        return completed;
    }

    // Updates the task status to completed or incomplete.
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    // Returns the exact time the task was first recorded.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}