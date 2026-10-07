package com.studybuddy.security;

import com.studybuddy.account.model.User;
import com.studybuddy.account.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// JwtAuthFilter is a custom security filter that intercepts every HTTP request to validate JWT credentials.
// It ensures that only authenticated users can access protected resources like Focus Sessions and Admin tools.
// This implementation aligns with the stateless session requirement described in Section 3.2.1.
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // Utility class for parsing and validating JSON Web Tokens (JWT).
    @Autowired
    private JwtUtil jwtUtil;

    // Repository to fetch the latest user state (e.g., admin status or ban flag) from the database.
    @Autowired
    private UserRepository userRepository;

    // Core filtering logic that executes once per incoming request.
    // It extracts the bearer token, validates it, and establishes the security context if successful.
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ACC-3: Extract the JWT from the "Authorization" HTTP header.
        // The expected format is "Bearer <token>", which is standard for RESTful API security.
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Strip the "Bearer " prefix to obtain the raw cryptographic token string.
            String token = authHeader.substring(7);

            // Validate the token's signature and expiration using the secret key.
            if (jwtUtil.isTokenValid(token)) {
                // Decode the User ID from the token payload to identify the requester.
                Long userId = jwtUtil.extractUserId(token);

                // Fetch the user entity to ensure the account still exists and to retrieve their current roles.
                User user = userRepository.findById(userId).orElse(null);

                if (user != null) {
                    // ACC-4: Map the user's database role to a Spring Security authority.
                    // This allows the use of @PreAuthorize tags to protect Moderator-specific endpoints.
                    String role = user.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER";

                    // Create an authentication token containing the user details and their granted authorities.
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    List.of(new SimpleGrantedAuthority(role))
                            );

                    // Set the authentication into the SecurityContext.
                    // This makes the user available via @AuthenticationPrincipal in controllers.
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        // Continue the filter chain execution, passing the request to the next filter or the controller.
        filterChain.doFilter(request, response);
    }
}