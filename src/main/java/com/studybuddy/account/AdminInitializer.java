package com.studybuddy.account;

import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// AdminInitializer is a component designed to bootstrap the system with a default administrator.
// This ensures that the platform always has an initial user with the necessary privileges
// to access the Moderation dashboard as defined in Section 5 of the Requirements Document.
@Component
public class AdminInitializer implements CommandLineRunner {

    // Repository used to check for the existence of the admin and persist the new account.
    @Autowired
    private UserRepository userRepository;

    // Component used to securely encode the default admin password before database storage.
    // This follows the security protocols where no plain-text credentials exist in the system.
    @Autowired
    private PasswordEncoder passwordEncoder;

    // The run method is executed automatically by Spring Boot after the application context is loaded.
    // It implements the CommandLineRunner interface to perform essential setup tasks.
    @Override
    public void run(String... args) {

        // Check if the default administrator account already exists in the 'users' table.
        // This prevents the creation of duplicate accounts on subsequent application restarts.
        if (!userRepository.existsByUsername("Admin")) {

            // Create a new User entity specifically for administrative purposes.
            User admin = new User();

            // Setting the default username as "Admin" for internal identification.
            admin.setUsername("Admin");

            // Assigning a system-level email for administrative notifications and record-keeping.
            admin.setEmail("admin@studybuddy.com");

            // Encoding the default password to ensure the system remains secure from the first boot.
            // In a production environment, this password should be updated immediately after setup.
            admin.setPassword(passwordEncoder.encode("Admin@123"));

            // ACC-4: Explicitly granting administrator privileges to this account.
            // This flag allows the user to perform moderation actions like banning (MOD-7, MOD-8).
            admin.setAdmin(true);

            // Persisting the admin user into the database via the JPA repository.
            userRepository.save(admin);

            // Logging the successful creation of the administrator to the system console.
            System.out.println("Default admin account created");
        }
    }
}