package com.studybuddy.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// RegisterRequest is a Data Transfer Object (DTO) used to capture new user information.
// It directly supports the Account Creation process described in the Requirements Document Section 3.2.1.
// This class ensures that all incoming data meets the platform's security and integrity standards before a user is created.
public class RegisterRequest {

    // ACC-1: The unique identifier the user chooses for their profile.
    // Username cannot be blank to ensure every student has a visible identity within the matching and focus session modules.
    @NotBlank(message = "Username is required")
    private String username;

    // The primary contact method and unique identifier for account recovery and notifications.
    // Must be a valid email format to ensure the system can send critical updates or moderation alerts as per Section 3.2.6.
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    // Security requirement for account protection.
    // Password must be at least 6 characters, contains uppercase, lowercase, number and special character.
    // This strict pattern ensures high entropy for user credentials, protecting the Study Buddy platform from unauthorized access.
    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{6,}$",
            message = "Password must be at least 6 characters long and include uppercase, lowercase, a number, and a special character"
    )
    private String password;

    // Standard getter for the username field.
    // Allows the AuthService to extract the name for uniqueness checks in the database.
    public String getUsername() {
        return username;
    }

    // Standard setter for the username field.
    // Used by the web framework to bind the 'username' field from the registration JSON body.
    public void setUsername(String username) {
        this.username = username;
    }

    // Standard getter for the email field.
    // Provides access to the email string for validation and persistence during the sign-up flow.
    public String getEmail() {
        return email;
    }

    // Standard setter for the email field.
    // Ensures the email provided in the UI-1.1 registration screen is correctly mapped to the DTO.
    public void setEmail(String email) {
        this.email = email;
    }

    // Standard getter for the password field.
    // Provides the raw password string to the security service for hashing before storage.
    public String getPassword() {
        return password;
    }

    // Standard setter for the password field.
    // Binds the secure credential input from the client to the backend request object.
    public void setPassword(String password) {
        this.password = password;
    }
}
