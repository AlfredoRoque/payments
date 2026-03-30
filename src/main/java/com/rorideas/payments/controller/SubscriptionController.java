package com.rorideas.payments.controller;

import com.rorideas.payments.dto.PaymentSubscriptionResponseDto;
import com.rorideas.payments.dto.SubscriptionDto;
import com.rorideas.payments.service.StripeSubscriptionService;
import com.rorideas.payments.service.SubscriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for managing subscriptions, including creating, canceling, changing plans, and retrieving subscription information.
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "Subscription Service", description = "Service for managing Subscription, including retrieval of Subscription information and processing of Subscription.")
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Creates a new subscription based on the provided price ID and returns the response containing the subscription information.
     * @param priceId the ID of the price for the subscription to be created
     * @return a ResponseEntity containing the PaymentSubscriptionResponseDto with the subscription information
     * @throws Exception if there is an error during the subscription creation process
     */
    @PostMapping("/create")
    public ResponseEntity<PaymentSubscriptionResponseDto> createSubscription(@RequestParam String priceId) throws Exception {
        return ResponseEntity.ok(subscriptionService.createCheckoutSession(priceId));
    }

    /**
     * Cancels the current subscription at the end of the billing period and returns the response containing the updated subscription information.
     * @return a ResponseEntity containing the PaymentSubscriptionResponseDto with the updated subscription information after cancellation
     * @throws Exception if there is an error during the subscription cancellation process
     */
    @PostMapping("/cancel")
    public ResponseEntity<PaymentSubscriptionResponseDto> cancelSubscription() throws Exception {
        return ResponseEntity.ok(subscriptionService.cancelAtPeriodEnd());
    }

    /**
     * Changes the current subscription plan to a new plan based on the provided price ID and returns the response containing the updated subscription information.
     * @param newPriceId the ID of the new price for the subscription plan to be changed to
     * @return a ResponseEntity containing the PaymentSubscriptionResponseDto with the updated subscription information after changing the plan
     * @throws Exception if there is an error during the subscription plan change process
     */
    @PostMapping("/change-plan")
    public ResponseEntity<PaymentSubscriptionResponseDto> changePlan(
            @RequestParam String newPriceId) throws Exception {
        return ResponseEntity.ok(subscriptionService.changePlan(newPriceId));
    }

    /**
     * Changes the payment method for the current subscription and returns the response containing the updated subscription information.
     * @return a ResponseEntity containing the PaymentSubscriptionResponseDto with the updated subscription information after changing the payment method
     * @throws Exception if there is an error during the payment method change process
     */
    @PostMapping("/change-cards")
    public ResponseEntity<PaymentSubscriptionResponseDto> changeCards() throws Exception {
        return ResponseEntity.ok(subscriptionService.changeCards());
    }

    /**
     * Checks if there is an existing subscription for the user and returns the response containing the subscription information if it exists.
     * @return a ResponseEntity containing the PaymentSubscriptionResponseDto with the subscription information if an existing subscription is found, or indicating that no subscription exists
     */
    @GetMapping("/exist-subscription")
    public ResponseEntity<PaymentSubscriptionResponseDto> existSubscription() {
        return ResponseEntity.ok(subscriptionService.existSubscription());
    }

    /**
     * Retrieves the active subscription information for the specified user ID and returns it in the response.
     * @param userId the ID of the user for whom to retrieve the active subscription information
     * @return a ResponseEntity containing the SubscriptionDto with the active subscription information for the specified user ID
     * @throws Exception if there is an error during the retrieval of the active subscription information
     */
    @GetMapping("/users/active")
    public ResponseEntity<SubscriptionDto> getUserSubscriptionActive(@RequestParam Long userId) throws Exception {
        return ResponseEntity.ok(subscriptionService.getUserSubscriptionActive(userId));
    }
}