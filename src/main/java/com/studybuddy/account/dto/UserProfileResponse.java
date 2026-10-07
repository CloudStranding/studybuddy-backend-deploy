package com.studybuddy.account.dto;

import com.studybuddy.account.model.User;
import java.time.LocalDateTime;

// UserProfileResponse is a Data Transfer Object (DTO) used to securely share user details with the frontend.
// It supports the profile visualization requirements described in the Requirements Document Section 3.2.1.
// This class ensures that sensitive data like hashed passwords are excluded from the public profile view.
public class UserProfileResponse {

    // The unique internal identifier for the user record.
    private Long id;

    // The display name of the user, used throughout the platform for matching and identification.
    private String username;

    // The contact email of the user, used for system communications and account management.
    private String email;

    // A flag indicating if the user has administrative privileges.
    // This allows the frontend to toggle the Moderator/Admin interface as per Section 3.2.6.
    private boolean isAdmin;

    // Moderation status: indicates if the user is currently restricted from platform activities.
    // This aligns with the "Ban" logic described in the Moderation section of the requirements.
    private boolean isBanned;

    // The timestamp indicating when a temporary ban will expire.
    // Supports the "Ban 3 Days" and "Perm Ban" logic defined in Section 5 (Moderation).
    private LocalDateTime bannedUntil;

    // The cumulative performance metric of the user based on study sessions.
    // This contributes to the platform's reliability and matching quality.
    private double rating;

    // Static factory method to convert a User entity into a UserProfileResponse DTO.
    // This mapping logic centralizes how user data is prepared for the presentation layer.
    public static UserProfileResponse fromUser(User user) {
        UserProfileResponse r = new UserProfileResponse();
        // Extracting basic identity information.
        r.id = user.getId();
        r.username = user.getUsername();
        r.email = user.getEmail();

        // Mapping roles and moderation status for frontend logic control.
        r.isAdmin = user.isAdmin();
        r.isBanned = user.isBanned();
        r.bannedUntil = user.getBannedUntil();

        // Mapping the user's reputation score.
        r.rating = user.getRating();
        return r;
    }

    // Standard getter for the user ID.
    public Long getId() {
        return id;
    }

    // Standard getter for the username.
    public String getUsername() {
        return username;
    }

    // Standard getter for the email.
    public String getEmail() {
        return email;
    }

    // Standard getter for the admin status flag.
    public boolean isAdmin() {
        return isAdmin;
    }

    // Standard getter for the ban status flag.
    public boolean isBanned() {
        return isBanned;
    }

    // Standard getter for the ban expiration timestamp.
    public LocalDateTime getBannedUntil() {
        return bannedUntil;
    }

    // Standard getter for the user's rating score.
    public double getRating() {
        return rating;
    }
}
