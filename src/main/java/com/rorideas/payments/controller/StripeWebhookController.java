package com.rorideas.payments.controller;

import com.rorideas.payments.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling Stripe webhook events related to payments and subscriptions. It listens for incoming webhook requests from Stripe and processes them using the SubscriptionService.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final SubscriptionService subscriptionService;

    /**
     * Endpoint to handle incoming Stripe webhook events. It receives the payload and signature header from Stripe, and delegates the processing of the webhook event to the SubscriptionService.
     * @param payload the raw JSON payload sent by Stripe in the webhook request
     * @param sigHeader the Stripe-Signature header containing the signature of the webhook event for verification
     * @return a ResponseEntity with an empty body and HTTP status 200 OK if the webhook event is processed successfully
     * @throws Exception if there is an error during the processing of the webhook event, such as signature verification failure or invalid payload
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) throws Exception {
        subscriptionService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok("");
    }
}
