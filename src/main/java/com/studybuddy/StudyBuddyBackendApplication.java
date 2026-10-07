package com.studybuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// StudyBuddyBackendApplication is the primary entry point for the Study Buddy server-side application.
// It initializes the Spring Framework and bootstraps all managed components, including controllers and services.
// This class enables the "Account Management", "Matching", and "Moderation" modules defined in the Requirements Document.
@SpringBootApplication
public class StudyBuddyBackendApplication {

	// The main method serves as the standard Java entry point that launches the application.
	// It triggers the auto-configuration and component scanning needed to run the Focus Session platform.
	public static void main(String[] args) {
		// Starts the embedded web server (e.g., Tomcat) and prepares the API for client requests.
		// Once running, the backend supports real-time study sessions as described in Section 3.2.3.
		SpringApplication.run(StudyBuddyBackendApplication.class, args);
	}

}