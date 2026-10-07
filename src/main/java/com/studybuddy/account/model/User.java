package com.studybuddy.account.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// The User entity represents the core account holder within the Study Buddy platform.
// It stores persistent data required for authentication, matching, and moderation.
// This model is central to the requirements defined in Section 3.2.1 (Account Management).
@Entity
@Table(name = "users")
public class User {

    // Unique identifier for the user, automatically incremented by the database.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ACC-1: Username must be unique and cannot be null to ensure identifiable interactions.
    // This is used for Buddy Matching and Focus Session displays as per Section 3.2.2.
    @Column(unique = true, nullable = false)
    private String username;

    // ACC-1: Email must be unique and cannot be null for account recovery and notifications.
    // It serves as a primary contact point for moderation alerts as described in Section 3.2.6.
    @Column(unique = true, nullable = false)
    private String email;

    // ACC-2: Stores the bcrypt hashed password to ensure security during the login process.
    // Plain text is never stored, aligning with the platform's security and privacy standards.
    @Column(nullable = false)
    private String password;

    // ACC-4: Boolean flag that determines if a user has administrative or moderator privileges.
    // If true, the user gains access to the Moderation dashboard to manage reports (Section 5).
    @Column(nullable = false)
    private boolean isAdmin = false;

    // MOD-8, MOD-9: Indicates if the account is currently subject to a permanent ban.
    // Banned users are restricted from joining focus sessions or searching for buddies.
    @Column(nullable = false)
    private boolean isBanned = false;

    // MOD-7, MOD-11: Stores the end time for temporary restrictions (e.g., a 3-day ban).
    // The system checks this timestamp to restore access automatically once the period expires.
    private LocalDateTime bannedUntil;

    // FS-6: Represents the user's reputation based on their behavior in focus sessions.
    // Defaulting to 3.0, this rating is adjusted based on session performance and reports.
    @Column(nullable = false)
    private double rating = 3.0;

    // Audit field to track when the account was initially created for administrative records.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Life-cycle callback to automatically populate the creation timestamp before persistence.
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Returns the unique database ID of the user.
    public Long getId() {
        return id;
    }

    // Returns the user's display name.
    public String getUsername() {
        return username;
    }

    // Updates the user's display name, typically during a profile update (ACC-6).
    public void setUsername(String username) {
        this.username = username;
    }

    // Returns the user's registered email address.
    public String getEmail() {
        return email;
    }

    // Updates the user's email address in the persistent storage.
    public void setEmail(String email) {
        this.email = email;
    }

    // Returns the encoded password hash for authentication verification.
    public String getPassword() {
        return password;
    }

    // Updates the user's password with a new encoded hash (ACC-7).
    public void setPassword(String password) {
        this.password = password;
    }

    // Checks if the user has moderator/admin authority.
    public boolean isAdmin() {
        return isAdmin;
    }

    // Configures the administrative status of the user account.
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    // Checks if the user is currently under any form of ban.
    public boolean isBanned() {
        return isBanned;
    }

    // Updates the ban status based on moderator actions (Section 5).
    public void setBanned(boolean banned) {
        isBanned = banned;
    }

    // Returns the expiration time for temporary bans.
    public LocalDateTime getBannedUntil() {
        return bannedUntil;
    }

    // Sets the duration of a temporary restriction.
    public void setBannedUntil(LocalDateTime bannedUntil) {
        this.bannedUntil = bannedUntil;
    }

    // Returns the user's study performance rating.
    public double getRating() {
        return rating;
    }

    // Updates the rating after a session or a verified moderator report.
    public void setRating(float rating) {
        this.rating = rating;
    }

    // Returns the account creation timestamp.
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
