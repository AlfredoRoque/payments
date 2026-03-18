package com.rorideas.payments.service;

import com.rorideas.payments.entity.Subscription;
import com.rorideas.payments.util.Constants;
import com.rorideas.payments.util.SecurityUtils;
import com.stripe.model.checkout.Session;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Stripe subscription service that handles the creation of checkout sessions, cancellation of subscriptions at the end of the period, changing subscription plans, and changing payment methods.
 * It uses the Stripe API to manage subscriptions and integrates with the application's security context to associate subscriptions with the authenticated user. The service provides methods to create a checkout session for a given price ID, cancel a subscription at the end of the current billing period, change the subscription plan to a new price ID, and change the payment method by redirecting the user to the Stripe billing portal. Each method interacts with the Stripe API and returns appropriate responses based on the actions performed.
 */
@RequiredArgsConstructor
@Service
public class StripeSubscriptionService {

    @Value("${stripe-urls.success-payment.path}")
    private String successPaymentUrl;

    @Value("${stripe-urls.failure-payment.path}")
    private String failurePaymentUrl;

    @Value("${stripe-urls.change-card-return.path}")
    private String changeCardReturnUrl;

    /**
     * Creates a Stripe checkout session for a subscription based on the provided price ID. The method builds the session parameters, including the mode, success and cancel URLs, client reference ID, metadata, subscription data, and line items. It then creates the session using the Stripe API and returns the URL for the checkout session.
     * @param priceId the ID of the price for the subscription to be created in the checkout session
     * @return the URL for the created Stripe checkout session
     * @throws Exception if there is an error while creating the checkout session with the Stripe API
     */
    public String createCheckoutSession(String priceId) throws Exception {
        HashMap<String,String> map = new HashMap<>();
        map.put("zone",SecurityUtils.getUserZone().getId());
        map.put("userId",SecurityUtils.getUserId().toString());
        SessionCreateParams params =
                SessionCreateParams.builder()
                        .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                        .setSuccessUrl(successPaymentUrl)
                        .setCancelUrl(failurePaymentUrl)
                        .setClientReferenceId(SecurityUtils.getUserId().toString())
                        .putAllMetadata(map)
                        .setSubscriptionData(
                                SessionCreateParams.SubscriptionData.builder()
                                        .putAllMetadata(map)
                                        .build()
                        )
                        .addLineItem(
                                SessionCreateParams.LineItem.builder()
                                        .setPrice(priceId)
                                        .setQuantity(1L)
                                        .build())
                        .build();

        Session session = Session.create(params);

        return session.getUrl();
    }


    /**
     * Cancels a Stripe subscription at the end of the current billing period. The method retrieves the subscription using the Stripe API, updates the subscription parameters to set the cancellation at the end of the period, and then updates the subscription with the new parameters. Finally, it returns a message indicating that the subscription has been canceled.
     * @param subscriptionUser the subscription entity representing the user's subscription that is to be canceled at the end of the billing period
     * @return a message indicating that the subscription has been canceled at the end of the period
     * @throws Exception if there is an error while retrieving or updating the subscription with the Stripe API
     */
    public String cancelAtPeriodEnd(Subscription subscriptionUser) throws Exception {
        com.stripe.model.Subscription subscription =
                com.stripe.model.Subscription.retrieve(subscriptionUser.getStripeSubscriptionId());

        SubscriptionUpdateParams params =
                SubscriptionUpdateParams.builder()
                        .setCancelAtPeriodEnd(true)
                        .build();

        subscription.update(params);

        return Constants.CANCEL_PLAN;
    }

    /**
     * Changes the subscription plan to a new price ID. The method retrieves the subscription using the Stripe API, gets the subscription item ID, builds the subscription update parameters to set the new price ID and proration behavior, and then updates the subscription with the new parameters. Finally, it returns a message indicating that the subscription plan has been updated successfully.
     * @param newPriceId the ID of the new price to which the subscription plan should be changed
     * @param sub the subscription entity representing the user's current subscription that is to be updated with the new plan
     * @return a message indicating that the subscription plan has been updated successfully
     * @throws Exception if there is an error while retrieving or updating the subscription with the Stripe API
     */
    public String changePlan(String newPriceId, Subscription sub) throws Exception {
        com.stripe.model.Subscription stripeSub =
                com.stripe.model.Subscription.retrieve(
                        sub.getStripeSubscriptionId()
                );

        String itemId =
                stripeSub.getItems().getData().get(0).getId();

        SubscriptionUpdateParams params =
                SubscriptionUpdateParams.builder()

                        .addItem(
                                SubscriptionUpdateParams.Item.builder()
                                        .setId(itemId)
                                        .setPrice(newPriceId)
                                        .build()
                        )

                        .setProrationBehavior(
                                SubscriptionUpdateParams
                                        .ProrationBehavior
                                        .CREATE_PRORATIONS
                        )
                        .build();

        stripeSub.update(params);

        return  Constants.UPDATE_PLAN_SUBSCRIPTION;
    }

    /**
     * Changes the payment method for the subscription by creating a session in the Stripe billing portal. The method builds the session parameters, including the customer ID and return URL, creates the session using the Stripe API, and returns the URL for the billing portal session where the user can manage their payment methods.
     * @param subscriptionUser the subscription entity representing the user's current subscription for which the payment method is to be changed
     * @return the URL for the Stripe billing portal session where the user can manage their payment methods
     * @throws Exception if there is an error while creating the billing portal session with the Stripe API
     */
    public String changeCards(Subscription subscriptionUser) throws Exception {
        com.stripe.param.billingportal.SessionCreateParams params =
                com.stripe.param.billingportal.SessionCreateParams.builder()
                        .setCustomer(subscriptionUser.getCustomerId())
                        .setReturnUrl(changeCardReturnUrl)
                        .build();

        com.stripe.model.billingportal.Session session = com.stripe.model.billingportal.Session.create(params);

        return session.getUrl();
    }
}
