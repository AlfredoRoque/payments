package com.rorideas.payments.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rorideas.payments.dto.UserSessionModel;
import com.rorideas.payments.enums.UserRol;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Filter that intercepts incoming HTTP requests to validate the JWT token present in the "Authorization" header.
 * If a valid token is found, it extracts the username and sets the authentication context for the request.
 * The filter also handles any exceptions that may occur during the token validation process and clears the security context if the token is invalid or an unexpected error occurs.
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Intercepts incoming HTTP requests to validate the JWT token present in the "Authorization" header.
     * If a valid token is found, it extracts the username and sets the authentication context for the request.
     *
     * @param request  The incoming HTTP request containing the JWT token in the "Authorization" header.
     * @param response The HTTP response to be sent back to the client.
     * @param chain    The filter chain to pass the request and response to the next filter in the chain.
     * @throws ServletException If an error occurs during the filtering process.
     * @throws IOException      If an I/O error occurs during the filtering process.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        String userContext = request.getHeader("X-SESSION-USER");

        try {
            if (header != null && header.startsWith("Bearer ")) {
                String token = header.substring(7);

                //Validate token and extract username
                String userName = jwtUtil.extractUsername(token);

                UserSessionModel sessionModel =
                        objectMapper.readValue(userContext, UserSessionModel.class);

                //TODO: para validar el tokenVersion se deberia crear un MS exclusivo para usuario y
                // paciente y asi poder validar el token correctamente, por ahora se omite esta validacion y se confia en la validacion del MS de entrada

                if (sessionModel.getUsername() != null && !sessionModel.getUsername().isEmpty()) {
                    Collection<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_"+sessionModel.getRole().name()));

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(sessionModel, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.info("JWT Token validated for user: {}", userName);
                } else {
                    log.warn("failed to extract username from token");
                }
            }
        } catch (JwtException e) {
            log.error("Validate token error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            log.error("Token unexpected error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
