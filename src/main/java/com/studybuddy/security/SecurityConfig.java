package com.studybuddy.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// SecurityConfig is the central security authority for the Study Buddy application.
// It defines access control policies, password encryption standards, and filter chains.
// This configuration ensures compliance with the security requirements in Section 3.2.1.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Custom filter to intercept and validate JWTs before processing any business logic.
    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    // Defines the Security Filter Chain which dictates how HTTP requests are secured.
    // It configures endpoint permissions, session management, and CSRF protection.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (Cross-Site Request Forgery) protection as it's redundant for JWT-based stateless APIs.
                // This simplifies integration with the mobile and web frontends as per Section 4.
                .csrf(AbstractHttpConfigurer::disable)

                // Enforce a STATELESS session policy. No HttpSession will be created or used.
                // This aligns with the requirement for scalable, token-based authentication.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure request-level authorization based on URL patterns and user roles.
                .authorizeHttpRequests(auth -> auth
                        // Public Endpoints: Allow anyone to register or login without a token.
                        .requestMatchers("/api/auth/**").permitAll()

                        // Database Console: Allow access to the H2 console for development and debugging purposes.
                        .requestMatchers("/h2-console/**").permitAll()

                        // Admin Restricted Area: Only users with ROLE_ADMIN can access general admin endpoints.
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Moderation Control: Protects the sanction logic (banning/kicking) as defined in Section 5.
                        // Only verified administrators can execute these restrictive actions.
                        .requestMatchers("/api/moderation/action/**").hasRole("ADMIN")

                        // Report Management: Restricts report status updates to authorized personnel only.
                        .requestMatchers("/api/moderation/report/*/status/**").hasRole("ADMIN")

                        // Audit & Dashboard: Only admins can view the complete list of misconduct reports (UI-5.1).
                        .requestMatchers("/api/moderation/reports").hasRole("ADMIN")

                        // All other endpoints (Matching, Profile, Focus Sessions) require a valid JWT.
                        .anyRequest().authenticated()
                )

                // Frame Options: Allow the H2 console to be displayed in an iframe on the same origin.
                // This is necessary for browser-based database management during the testing phase.
                .headers(headers ->
                        headers.frameOptions(frame -> frame.sameOrigin()))

                // Add the JwtAuthFilter before the standard UsernamePasswordAuthenticationFilter.
                // This ensures the SecurityContext is populated with user details before Spring checks for authority.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Bean for BCryptPasswordEncoder, used across the application to hash and verify user passwords.
    // BCrypt is chosen for its strength against brute-force attacks, meeting the project's security standards.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}