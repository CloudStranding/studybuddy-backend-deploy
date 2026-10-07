package com.studybuddy.account.controller;

import com.studybuddy.account.dto.*;
import com.studybuddy.account.model.User;
import com.studybuddy.account.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// UserController manages user-specific data and account lifecycle operations.
// It implements functionalities described in the Requirements Document Section 3.2.1.
// These endpoints ensure users can manage their profiles and maintain account security.
@RestController
@RequestMapping("/api/user")
public class UserController {

    // Service layer dependency for executing user-related business logic.
    // Separating the controller from the service layer improves maintainability and testability[cite: 4, 15].
    @Autowired
    private UserService userService;

    // Retrieves the current authenticated user's profile information.
    // This corresponds to the user dashboard view where personal details are displayed.
    // Uses the @AuthenticationPrincipal to securely identify the logged-in user session.
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal User user) {
        try {
            // Fetch the user entity from the database using the unique identifier.
            User profile = userService.getProfile(user.getId());
            // Converts the User entity to a UserProfileResponse DTO to hide sensitive internal data.
            return ResponseEntity.ok(UserProfileResponse.fromUser(profile));
        } catch (RuntimeException e) {
            // Provides an error message if the profile cannot be retrieved.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ACC-6: Update username and email.
    // Allows users to modify their basic account information as part of profile management.
    // Ensures data integrity by validating the UpdateProfileRequest before processing.
    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {
        try {
            // Calls the service to update the user record in the database.
            userService.updateProfile(user.getId(), request);
            // Confirms successful update to the client.
            return ResponseEntity.ok("Profile updated successfully");
        } catch (RuntimeException e) {
            // Handles potential conflicts such as duplicate emails or usernames.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ACC-7: Change password.
    // Provides a secure mechanism for users to update their credentials for account protection.
    // Aligns with security requirements to ensure that only the account owner can change the password.
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            // Verifies the current password before setting the new one to prevent unauthorized changes.
            userService.changePassword(user.getId(),
                    request.getCurrentPassword(), request.getNewPassword());
            // Success response after the new password has been securely hashed and saved.
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            // Returns an error if the current password check fails or if the new password lacks strength.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ACC-5: Delete account with password confirmation.
    // Implements the account termination process described in the Requirements Document.
    // Requiring a password confirmation adds a layer of safety against accidental account deletion.
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteAccount(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DeleteAccountRequest request) {
        try {
            // Executes the deletion logic, ensuring all associated user data is handled correctly.
            userService.deleteAccount(user.getId(), request);
            // Informs the user that the account and its associated session have been permanently removed.
            return ResponseEntity.ok("Account deleted successfully");
        } catch (RuntimeException e) {
            // Returns an error if password confirmation fails or if the user record is not found.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
