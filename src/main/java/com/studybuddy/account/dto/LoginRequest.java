package com.studybuddy.account.dto;

import jakarta.validation.constraints.NotBlank;

// LoginRequest is a Data Transfer Object (DTO) that encapsulates user credentials for authentication.
// It supports the flexible login requirement where users can identify themselves via username or email.
// This class aligns with the Authentication process described in the Requirements Document Section 3.2.1.
public class LoginRequest {

    // ACC-2: Accept either username or email for login to improve user experience.
    // This field is essential for the initial identification phase of the authentication lifecycle.
    // The @NotBlank annotation ensures that the identification string is provided before the request hits the service.
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    // The secret credential used to verify the identity of the user.
    // As per security best practices, this field remains mandatory to prevent unauthorized access.
    // It is processed through the password encoder in the AuthService to compare with the stored hash.
    @NotBlank(message = "Password is required")
    private String password;

    // Standard getter for the identification field (username or email).
    // Allows the authentication manager to retrieve the identifier for database lookup.
    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }

    // Standard setter for the identification field.
    // Used by the Jackson library to deserialize the incoming JSON request body into this object.
    public void setUsernameOrEmail(String usernameOrEmail) {
        this.usernameOrEmail = usernameOrEmail;
    }

    // Standard getter for the password field.
    // Provides access to the raw password string for the authentication verification process.
    public String getPassword() {
        return password;
    }

    // Standard setter for the password field.
    // Enables the framework to bind the password from the client-side login form to the DTO.
    public void setPassword(String password) {
        this.password = password;
    }
}
