package com.rorideas.payments.config;

import com.rorideas.payments.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * configuration class for Spring Security, it defines the security filter chain and the security settings for the application.
 * It uses JWT for authentication and authorization, and it allows access to the Swagger UI and the webhook endpoint without authentication.
 */
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${without-security.swagger-ui.path}")
    private String swaggerUiPath;

    @Value("${without-security.swagger-ui-enpoints.path}")
    private String swaggerUiEndpointsPath;

    @Value("${without-security.api-docs.path}")
    private String apiDocsPath;

    @Value("${without-security.stripe-webhook.path}")
    private String stripeWebhookPath;

    private final JwtFilter jwtFilter;

    /**
     * Defines the security filter chain for the application, it configures CORS, CSRF, session management, and authorization rules.
     * @param http the HttpSecurity object that allows configuring the security settings for the application.
     * @return the SecurityFilterChain object that defines the security filter chain for the application.
     * @throws Exception if there is an error configuring the security settings for the application.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(swaggerUiPath,
                                swaggerUiEndpointsPath,
                                apiDocsPath,
                                stripeWebhookPath).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
