package com.studybuddy.account.dto;

import jakarta.validation.constraints.NotBlank;

// ChangePasswordRequest is a Data Transfer Object (DTO) used during the password update process.
// It supports the security requirements defined in the Requirements Document Section 3.2.1.
// By capturing both current and new passwords, it enables the system to verify ownership before modification.
public class ChangePasswordRequest {

    // The existing password of the user used for identity verification.
    // As per ACC-7, providing the current password is a mandatory security measure to prevent unauthorized changes.
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    // The target password the user wishes to set for future authentications.
    // This field is validated to ensure it is not empty before being processed by the encryption service.
    @NotBlank(message = "New password is required")
    private String newPassword;

    // Standard getter for the current password field.
    // This allows the AuthService to retrieve the raw string for comparison with the hashed version in the database.
    public String getCurrentPassword() {
        return currentPassword;
    }

    // Standard setter for the current password field.
    // Used by the framework to bind incoming JSON data from the HTTP request body.
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    // Standard getter for the new password field.
    // Provides access to the new credential that will be hashed and stored upon successful verification.
    public String getNewPassword() {
        return newPassword;
    }

    // Standard setter for the new password field.
    // Ensures the new password data is correctly mapped from the client's update request.
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
