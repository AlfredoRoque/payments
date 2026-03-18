package com.rorideas.payments.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * configuration class for Stripe, it initializes the Stripe API key from the application properties file and sets it to the Stripe API client.
 */
@Configuration
public class StripeConfig {

    @Value("${stripe.secret.key}")
    private String secretKey;

    /**
     * initializes the Stripe API key from the application properties file and sets it to the Stripe API client.
     */
    @PostConstruct
    public void init(){
        Stripe.apiKey = secretKey;
    }
}
