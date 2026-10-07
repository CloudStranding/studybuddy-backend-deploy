package com.studybuddy.account.controller;

import com.studybuddy.account.dto.LoginRequest;
import com.studybuddy.account.dto.LoginResponse;
import com.studybuddy.account.dto.RegisterRequest;
import com.studybuddy.account.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// AuthController handles all authentication-related requests for the Study Buddy platform.
// It aligns with the Requirements Document Section 3.2.1 regarding Account Management.
// This controller ensures that users can securely access the matching and focus session features.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Injecting AuthService to handle the core business logic for user credentials.
    // This separation of concerns ensures maintainability as required by SonarQube standards.
    @Autowired
    private AuthService authService;

    // ACC-1: Register a new account.
    // This endpoint allows new users to join the Study Buddy community.
    // According to the Requirements Document, users must provide valid credentials to create an account.
    // It validates the RegisterRequest to ensure data integrity before calling the service layer.
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Invokes the registration logic which includes password encoding and database persistence.
            authService.register(request);
            // Returns a 200 OK response upon successful creation of the user profile.
            return ResponseEntity.ok("Account created successfully");
        } catch (RuntimeException e) {
            // Returns a 400 Bad Request if the username or email is already taken, or if validation fails.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ACC-2: Login with username or email and password.
    // This facilitates user access to the platform's core features like Focus Sessions and Buddy Matching.
    // It supports the requirement for secure authentication as specified in the UI-1.1 login screen design.
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            // Authenticates the user and generates a JWT (JSON Web Token) for session management.
            LoginResponse response = authService.login(request);
            // Returns the token and user details needed for the frontend to maintain the authenticated state.
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            // Handles authentication failures, such as incorrect passwords or non-existent accounts.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ACC-3: Logout functionality.
    // Since the system uses stateless JWT authentication, the server-side logout is symbolic.
    // The client-side application is responsible for discarding the token to terminate the session.
    // This endpoint provides a standard API confirmation for the logout process.
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }
}
