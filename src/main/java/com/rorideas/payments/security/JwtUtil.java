package com.rorideas.payments.security;

import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for handling JWT (JSON Web Token) operations, such as extracting the username from a token.
 * This class uses the JJWT library to parse and validate JWT tokens.
 * The secret key for signing the JWT is injected from the application properties.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET;

    /**
     * Extracts the username from the given JWT token.
     *
     * @param token The JWT token from which the username is extracted.
     * @return The username contained in the token's subject.
     */
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
