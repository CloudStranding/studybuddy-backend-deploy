package com.studybuddy.account.dto;

// LoginResponse is a Data Transfer Object (DTO) that carries authentication metadata back to the client.
// It is generated after a successful login to provide the necessary credentials for subsequent API calls.
// This supports the account management and security flow described in the Requirements Document Section 3.2.1.
public class LoginResponse {

    // JWT (JSON Web Token) returned after successful login.
    // This token is used by the frontend for Bearer authentication in the Authorization header.
    // It ensures stateless session management, aligning with modern software maintainability standards[cite: 8].
    private String token;

    // The unique internal identifier for the authenticated user.
    // This ID is used by the frontend to fetch specific profile data or manage personal Focus Sessions.
    private Long userId;

    // The display name of the user, used for personalizing the UI dashboard as seen in UI-1.1.
    private String username;

    // ACC-4: Boolean flag used by the frontend to determine the user's role.
    // If true, the frontend renders the Moderator/Admin interface (as per Section 3.2.6 & 5).
    // If false, the standard "Student" UI is displayed for regular study buddy interactions.
    private boolean isAdmin;

    // Constructor to initialize the login response with all necessary session data.
    // This allows the AuthService to easily wrap the result of a successful authentication.
    public LoginResponse(String token, Long userId, String username, boolean isAdmin) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.isAdmin = isAdmin;
    }

    // Standard getter for the authentication token.
    // Essential for the client to store the token securely (e.g., in LocalStorage or a Secure Cookie).
    public String getToken() {
        return token;
    }

    // Standard getter for the user ID.
    // Provides a reference for the client-side logic to perform user-specific operations.
    public Long getUserId() {
        return userId;
    }

    // Standard getter for the username.
    // Helps in displaying the user's name immediately after redirecting from the login page.
    public String getUsername() {
        return username;
    }

    // Standard getter for the admin status.
    // Directly supports the UI-logic requirement to separate student and moderator functionalities.
    public boolean isAdmin() {
        return isAdmin;
    }
}
