package com.studybuddy.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// UpdateProfileRequest is a Data Transfer Object (DTO) used for modifying existing user details.
// It directly implements the profile management requirements specified in ACC-6 of the Requirements Document.
// This class facilitates the secure transition of user-provided data from the frontend to the backend service layer.
public class UpdateProfileRequest {

    // ACC-6: The new username that the user wishes to adopt for their profile.
    // This field must not be blank to ensure the user maintains a valid identity within the platform's social modules.
    // Validation ensures that any change to the username follows the platform's naming constraints.
    @NotBlank(message = "Username is required")
    private String username;

    // ACC-6: The new email address to be associated with the user's account.
    // It must follow a valid email format to ensure the user can still receive critical system notifications and moderation alerts.
    // The @Email annotation provides an initial layer of validation before the service layer checks for database uniqueness.
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    // Standard getter for the updated username field.
    // This allows the UserService to retrieve the proposed name and perform necessary availability checks.
    public String getUsername() {
        return username;
    }

    // Standard setter for the username field.
    // Enables the Spring framework to populate this DTO from the JSON payload sent via the HTTP PUT request.
    public void setUsername(String username) {
        this.username = username;
    }

    // Standard getter for the updated email field.
    // Provides the service layer with the new contact information required for account record synchronization.
    public String getEmail() {
        return email;
    }

    // Standard setter for the email field.
    // Maps the incoming 'email' string from the user's profile edit screen to this object.
    public void setEmail(String email) {
        this.email = email;
    }
}
