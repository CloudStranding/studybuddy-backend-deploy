package com.studybuddy.account.dto;

import jakarta.validation.constraints.NotBlank;

// DeleteAccountRequest is a Data Transfer Object (DTO) designed for the account termination process.
// It fulfills the security requirement ACC-5, which mandates password confirmation before a user can delete their account.
// This layer of validation ensures that the destructive action is intentional and authorized by the account owner. [cite: 16]
public class DeleteAccountRequest {

    // ACC-5: password confirmation required before deleting its account.
    // This field captures the user's current password to verify their identity at the moment of deletion.
    // The @NotBlank annotation ensures that the request is rejected early if the password field is missing or empty. [cite: 13, 16]
    @NotBlank(message = "Password is required")
    private String password;

    // Standard getter method for the password field.
    // This is used by the UserService to retrieve the provided password for comparison against the stored hash.
    public String getPassword() {
        return password;
    }

    // Standard setter method for the password field.
    // This allows the Spring framework to map the incoming JSON 'password' field from the HTTP DELETE request body.
    public void setPassword(String password) {
        this.password = password;
    }
}