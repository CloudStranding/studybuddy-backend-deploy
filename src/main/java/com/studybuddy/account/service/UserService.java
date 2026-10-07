package com.studybuddy.account.service;

import com.studybuddy.account.dto.DeleteAccountRequest;
import com.studybuddy.account.dto.UpdateProfileRequest;
import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// UserService handles detailed user management operations beyond basic authentication.
// It manages the user lifecycle, including profile updates, security credentials, and account termination.
// These operations align with the Requirements Document Section 3.2.1 regarding Account Management.
@Service
public class UserService {

    // Dependency on UserRepository for direct database interaction and persistence.
    @Autowired
    private UserRepository userRepository;

    // Component used for secure password matching and encryption to maintain data security.
    @Autowired
    private PasswordEncoder passwordEncoder;

    // ACC-6: Update username and email.
    // This method allows existing users to modify their identity and contact details.
    // It includes validation logic to ensure that changes do not conflict with other users' data.
    public void updateProfile(Long userId, UpdateProfileRequest request) {

        // Retrieve the current user from the database to ensure the account exists.
        // Throws an exception if the ID does not match any record, preventing illegal updates.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if the new username requested is already claimed by another student.
        // This ensures the uniqueness of identities in Focus Sessions as required by the platform.
        if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        // Check if the new email provided is already registered under a different account.
        // Maintaining unique emails is critical for secure notifications and moderation alerts.
        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        // Apply the validated changes to the user entity.
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // Persist the updated user profile back to the database.
        userRepository.save(user);
    }

    // ACC-5: Permanently delete account after password confirmation.
    // This implements a destructive action that must be guarded by re-authentication.
    // As per PR-5, this operation permanently removes the user and their associated data.
    public void deleteAccount(Long userId, DeleteAccountRequest request) {

        // Verification of the user's existence before attempting a deletion.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Mandatory password verification to prevent accidental or unauthorized account deletion.
        // This confirms that the person requesting the deletion is indeed the account owner.
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Execute the permanent removal of the User entity from the system.
        // In a production environment, this might also trigger cleanup of associated study records.
        userRepository.delete(user);
    }

    // ACC-7: Change password.
    // This provides a secure way for users to update their credentials from their profile settings.
    // It follows the principle of requiring the 'current' password to authorize the change.
    public void changePassword(Long userId, String currentPassword, String newPassword) {

        // Ensure the user is currently authenticated and valid.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate the current password against the stored hash to authorize the update.
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Encode the new password using BCrypt before saving it to the database.
        // This ensures the system remains compliant with security best practices.
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Retrieve full user profile information for the UI dashboard.
    // This allows the frontend to display data such as username, email, and rating (FS-6).
    public User getProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}