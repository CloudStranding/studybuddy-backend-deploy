package com.studybuddy.matching.model;

import com.studybuddy.account.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// The Meeting entity represents a synchronized study session between paired users.
// It stores the necessary session metadata to facilitate real-time communication as per Section 3.2.3.
// This model is essential for tracking active Focus Sessions and managing buddy connections.
@Entity
public class Meeting {

    // Unique database identifier for the study session record.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingId;

    // A unique identifier used to link participants to a specific real-time communication channel.
    // This channelName allows the frontend to synchronize audio/video streams between buddies.
    @Column(nullable = false, unique = true)
    private String channelName;

    // The length of the session in minutes, chosen by the user during the matching phase.
    // This value is used to initialize the countdown timer in the Focus Session UI-3.1.
    @Column(nullable = false)
    private int duration;

    // Timestamp recorded when the meeting is first created for auditing and session tracking.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // A collection of User entities currently participating in this specific meeting.
    // This relationship ensures that the system can identify which "Buddies" are paired together.
    // EAGER fetching is used to ensure participant data is immediately available for matching responses.
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "meeting_users",
            joinColumns = @JoinColumn(name = "meeting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id", unique = true)
    )
    private Set<User> users = new HashSet<>();

    // Tracks the current number of active students in the session.
    // This is used by the matching logic to determine if a meeting is full or available for a new buddy.
    @Column(nullable = false)
    private int participantCount;

    // Life-cycle callback to automatically set the session start time before database persistence.
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Updates the count of active participants, used during join/leave operations.
    public void setParticipantCount(int participantCount) {
        this.participantCount = participantCount;
    }

    // Standard getter for the unique meeting ID.
    public Long getMeetingId() {
        return meetingId;
    }

    // Returns the channel identifier required for the client-side communication bridge.
    public String getChannelName() {
        return channelName;
    }

    // Returns the total duration of the session as defined by the matching criteria.
    public int getDuration() {
        return duration;
    }

    // Returns the creation timestamp of the meeting record.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Returns the set of users currently linked to this study session.
    public Set<User> getUsers() {
        return users;
    }

    // Returns the number of students currently in the session.
    public int getParticipantCount() {
        return participantCount;
    }

    // Adds a new student to the meeting's participant list during the matching process.
    public void addParticipant(User user) {
        users.add(user);
    }

    // Removes a student from the meeting, typically triggered by the "Leave" action.
    public void removeParticipant(User user) {
        users.remove(user);
    }

    // Sets the unique channel name for the real-time session.
    public void setChannelName(String s) {
        channelName = s;
    }

    // Configures the intended duration for the study session.
    public void setDuration(int duration) {
        this.duration = duration;
    }
}