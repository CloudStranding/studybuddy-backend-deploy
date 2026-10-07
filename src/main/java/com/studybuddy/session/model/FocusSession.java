package com.studybuddy.session.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// The FocusSession entity represents a single recorded study interval for a user.
// It serves as the primary data structure for tracking productivity and calculating ratings.
// This model supports the core functionality of the "Focus Session" module described in Section 3.2.3.
@Entity
@Table(name = "focus_sessions")
public class FocusSession {

    // Unique database identifier for the specific focus session record.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key reference to the User who performed this study session.
    // This linkage is essential for generating user-specific history and statistics.
    @Column(nullable = false)
    private Long userId;

    // The planned length of the session in minutes, as selected during the matching phase.
    // This value is used to benchmark the user's progress against their intended study goal.
    @Column(nullable = false)
    private int durationMinutes;

    // FS-6: Completion flag determining if the user successfully finished the session.
    // True indicates a successful session; False or Null indicates it was interrupted or left early.
    private Boolean completed;

    // The unique identifier for the communication channel used during this session.
    // Provides a historical audit trail linking the session record to the matching context.
    private String channelName;

    // Timestamp marking the exact start time of the study session.
    // Automatically populated via the @PrePersist callback to ensure data integrity.
    @Column(nullable = false)
    private LocalDateTime startedAt;

    // Timestamp marking when the session concluded (either successfully or by leaving).
    // The gap between startedAt and endedAt reflects the actual time spent in focus.
    private LocalDateTime endedAt;

    // Lifecycle callback to automatically set the start time before the entity is persisted.
    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
    }

    // Returns the unique session ID.
    public Long getId() {
        return id;
    }

    // Returns the ID of the user associated with this record.
    public Long getUserId() {
        return userId;
    }

    // Sets the user identifier for this session.
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Returns the scheduled duration of the session.
    public int getDurationMinutes() {
        return durationMinutes;
    }

    // Sets the intended duration for the study period.
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    // Returns the completion status used for rating calculations.
    public Boolean getCompleted() {
        return completed;
    }

    // Updates the completion status based on session outcome.
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    // Returns the associated communication channel name.
    public String getChannelName() {
        return channelName;
    }

    // Sets the channel name for session tracking.
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    // Returns the recorded start time of the session.
    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    // Returns the recorded end time of the session.
    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    // Sets the completion or interruption time of the session.
    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }
}