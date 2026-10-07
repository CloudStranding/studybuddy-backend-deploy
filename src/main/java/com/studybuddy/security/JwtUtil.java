package com.studybuddy.security;

import com.studybuddy.account.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

// JwtUtil is a utility component responsible for handling the lifecycle of JSON Web Tokens.
// It supports the "Secure Login" and "Stateless Session" requirements defined in Section 3.2.1.
// By centralizing token logic, it ensures consistent security across all API endpoints.
@Component
public class JwtUtil {

    // The secret key used for HMAC-SHA digital signatures, injected from application properties.
    // This key must remain confidential to prevent unauthorized token forging.
    @Value("${jwt.secret}")
    private String secret;

    // The lifespan of the token in milliseconds, determining how long a user stays logged in.
    // This aligns with the platform's session management policy for better security.
    @Value("${jwt.expiration}")
    private long expiration;

    // Derives a secure cryptographic SecretKey from the configured raw secret string.
    // The HMAC-SHA algorithm ensures the integrity of the JWT header and payload.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ACC-3: Generates a signed JWT for a user after a successful authentication process.
    // The token encapsulates the user's identity and privileges, allowing for stateless authorization.
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getId().toString()) // The unique User ID as the primary subject.
                .claim("username", user.getUsername()) // Public identity for UI personalization.
                .claim("isAdmin", user.isAdmin()) // ACC-4: Role flag for frontend UI-logic control.
                .issuedAt(new Date()) // Records the precise time the session was initiated.
                .expiration(new Date(System.currentTimeMillis() + expiration)) // Sets the timeout period.
                .signWith(getSigningKey()) // Applies the digital signature to ensure authenticity.
                .compact();
    }

    // Decodes the token to retrieve the User ID stored in the 'subject' field.
    // This is used by the JwtAuthFilter to identify the requester on every API call.
    public Long extractUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    // Validates the integrity and validity of the provided token string.
    // It checks if the signature is valid and if the expiration date has not yet passed.
    public boolean isTokenValid(String token) {
        try {
            // A token is valid if the current system time is before the 'exp' claim in the payload.
            return getClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            // Any parsing or cryptographic error results in an invalid token status.
            return false;
        }
    }

    // Extracts the full set of Claims (payload) from a signed JWT string.
    // This method performs the actual verification using the pre-shared signing key.
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // Verifies that the token was indeed issued by this server.
                .build()
                .parseSignedClaims(token) // Parses the claims-set while checking the signature.
                .getPayload();
    }
}