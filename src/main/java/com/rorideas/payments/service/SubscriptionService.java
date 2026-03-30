package com.rorideas.payments.service;

import com.rorideas.payments.dto.PaymentSubscriptionResponseDto;
import com.rorideas.payments.dto.SubscriptionDto;
import com.rorideas.payments.entity.StripeEvent;
import com.rorideas.payments.entity.Subscription;
import com.rorideas.payments.enums.PaymentStatus;
import com.rorideas.payments.repository.StripeEventRepository;
import com.rorideas.payments.repository.SubscriptionRepository;
import com.rorideas.payments.util.Constants;
import com.rorideas.payments.util.SecurityUtils;
import com.rorideas.payments.util.Utility;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

/**
 * Service for managing user subscriptions, including creating checkout sessions, handling Stripe webhooks, and updating subscription statuses based on payment events.
 * This service interacts with the Stripe API to manage subscription lifecycle events and updates the local database accordingly. It also provides methods for users to change their subscription plans and payment methods.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SubscriptionService {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final StripeEventRepository stripeEventRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanService planService;
    private final StripeSubscriptionService stripeSubscriptionService;

    /**
     * Creates a Stripe checkout session for the specified price ID and returns the session URL for the user to complete the subscription process.
     * @param priceId the ID of the Stripe price for the subscription plan the user wants to subscribe to
     * @return a PaymentSubscriptionResponseDto containing the URL of the Stripe checkout session and a flag indicating whether the session was successfully created
     * @throws Exception if there is an error creating the checkout session or if the price ID is invalid
     */
    public PaymentSubscriptionResponseDto createCheckoutSession(String priceId) throws Exception {
        return new PaymentSubscriptionResponseDto(stripeSubscriptionService.createCheckoutSession(priceId),true);
    }

    /**
     * Cancels the user's subscription at the end of the current billing period. This method updates the subscription status to indicate that it is pending cancellation and will be canceled at the end of the billing cycle.
     * @return a PaymentSubscriptionResponseDto containing the updated subscription information and a flag indicating whether the cancellation was successfully scheduled
     * @throws Exception if there is an error finding the user's subscription or if the subscription is not found
     */
    public PaymentSubscriptionResponseDto cancelAtPeriodEnd() throws Exception {
        Optional<Subscription> subscription = this.findByUserIdAndStatus(SecurityUtils.getUserId());
        subscription.orElseThrow(() -> new Exception(Constants.NOT_FOUND_SUBSCRIPTION));
        return new PaymentSubscriptionResponseDto(stripeSubscriptionService.cancelAtPeriodEnd(subscription.get()), false);
    }

    /**
     * Changes the user's subscription plan to a new plan specified by the new price ID. This method updates the subscription to reflect the new plan and handles any necessary proration or billing adjustments.
     * @param newPriceId the ID of the new Stripe price for the subscription plan the user wants to switch to
     * @return a PaymentSubscriptionResponseDto containing the updated subscription information and a flag indicating whether the plan change was successfully processed
     * @throws Exception if there is an error finding the user's subscription, if the subscription is not found, or if there is an error changing the subscription plan in Stripe
     */
    public PaymentSubscriptionResponseDto changePlan(String newPriceId) throws Exception  {
        Optional<Subscription> subscription = this.findByUserIdAndStatus(SecurityUtils.getUserId());
        subscription.orElseThrow(() -> new Exception(Constants.NOT_FOUND_SUBSCRIPTION));
        return new PaymentSubscriptionResponseDto(stripeSubscriptionService.changePlan(newPriceId,subscription.get()), false);
    }

    /**
     * Changes the user's payment method for their subscription. This method updates the subscription to use a new payment method and handles any necessary billing adjustments.
     * @return a PaymentSubscriptionResponseDto containing the updated subscription information and a flag indicating whether the payment method change was successfully processed
     * @throws Exception if there is an error finding the user's subscription, if the subscription is not found, or if there is an error changing the payment method in Stripe
     */
    public PaymentSubscriptionResponseDto changeCards() throws Exception {
        Optional<Subscription> subscription = this.findByUserIdAndStatus(SecurityUtils.getUserId());
        subscription.orElseThrow(() -> new Exception(Constants.NOT_FOUND_SUBSCRIPTION));
        return  new PaymentSubscriptionResponseDto(stripeSubscriptionService.changeCards(subscription.get()),true);
    }

    /**
     * Handles incoming Stripe webhook events related to subscription lifecycle events such as checkout session completion, subscription updates, and payment successes or failures. This method verifies the webhook signature, processes the event based on its type, and updates the local database accordingly to reflect the current state of the user's subscription.
     * @param payload the raw JSON payload of the Stripe webhook event
     * @param sigHeader the Stripe signature header used to verify the authenticity of the webhook event
     * @throws Exception if there is an error verifying the webhook signature, if the event has already been processed, or if there is an error handling the specific event type
     */
    public void handleWebhook(String payload, String sigHeader) throws Exception{
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (Exception e) {
            throw new Exception(Constants.INVALID_SIGNATURE);
        }

        if(stripeEventRepository.findByEvent(event.getId()).isPresent()){
            throw new Exception(Constants
                    .ALREADY_PROCESS);
        }

        switch (event.getType()) {
            case Constants.CHECKOUT_SESSION_COMPLETED:
                this.saveEvent(event);
                handleCheckoutCompleted(event);
                break;
            case Constants.CUSTOMER_SUBSCRIPTION_DELETED:
                this.saveEvent(event);
                handleSubscriptionCanceled(event);
                break;
            case Constants.CUSTOMER_SUBSCRIPTION_UPDATED:
                this.saveEvent(event);
                handleSubscriptionUpdated(event);
                break;
            case Constants.INVOICE_PAYMENT_SUCCESS:
                this.saveEvent(event);
                handleSuccessPaymentUpdatedSubscription(event);
                break;
            case Constants.INVOICE_PAYMENT_FAILED:
                this.saveEvent(event);
                handlePaymentFail(event);
                break;
            default:
                log.info(Constants.UNHANDLED_EVENT, event.getType());
        }
    }

    /**
     * Handles the Stripe webhook event for a completed checkout session. This method retrieves the session information from the event, checks the payment status, and if the payment was successful, it creates a new subscription in the local database with the relevant details such as user ID, subscription ID, zone, customer ID, and price ID.
     * @param event the Stripe event object containing the details of the completed checkout session
     * @throws StripeException if there is an error retrieving the session information from Stripe or if there is an error creating the subscription in the local database
     */
    private void handleCheckoutCompleted(Event event) throws StripeException {
        log.info("handleCheckoutCompleted");
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        Session session = (Session) deserializer.getObject().orElse(null);

        if (session == null) {
            session = ApiResource.GSON.fromJson(
                    deserializer.getRawJson(),
                    Session.class
            );
            if (session == null) { return;}
        }

        if(!Constants.PAID_STATUS.equals(session.getPaymentStatus())) return;

        Long userId = Long.valueOf(session.getClientReferenceId());
        String zone = session.getMetadata().get("zone");

        com.stripe.model.Subscription sub =
                com.stripe.model.Subscription.retrieve(session.getSubscription());
        String priceId =
                sub.getItems()
                        .getData()
                        .get(0)
                        .getPrice()
                        .getId();

        createSubscription(userId, session.getSubscription(), zone,session.getCustomer(),priceId);

    }

    /**
     * Handles the Stripe webhook event for a canceled subscription. This method retrieves the subscription information from the event and updates the local database to mark the subscription as canceled based on the subscription ID provided in the event.
     * @param event the Stripe event object containing the details of the canceled subscription
     * @throws Exception if there is an error retrieving the subscription information from Stripe or if there is an error updating the subscription status in the local database
     */
    private void handleSubscriptionCanceled(Event event) throws Exception {
        log.info("handleSubscriptionCanceled");
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        com.stripe.model.Subscription subscription = (com.stripe.model.Subscription) deserializer.getObject().orElse(null);

        if (subscription == null) {
            subscription = ApiResource.GSON.fromJson(
                    deserializer.getRawJson(),
                    com.stripe.model.Subscription.class
            );
            if (subscription == null) { return;}
        }

        cancelSubscription(subscription.getId());
    }

    /**
     * Handles the Stripe webhook event for an updated subscription. This method retrieves the subscription information from the event, checks if the subscription plan has changed, and updates the local database to reflect the new subscription status and price ID based on the subscription ID provided in the event.
     * @param event the Stripe event object containing the details of the updated subscription
     * @throws Exception if there is an error retrieving the subscription information from Stripe or if there is an error updating the subscription status in the local database
     */
    private void handleSubscriptionUpdated(Event event) throws Exception {
        log.info("handleSubscriptionUpdated");
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

        com.stripe.model.Subscription subscription = (com.stripe.model.Subscription) deserializer.getObject().orElse(null);

        if (subscription == null) {
            subscription = ApiResource.GSON.fromJson(
                    deserializer.getRawJson(),
                    com.stripe.model.Subscription.class
            );
            if (subscription == null) { return;}
        }


        String userId = subscription.getMetadata().get("userId");

        Optional<Subscription> subscriptionUser = subscriptionRepository.findByUserId(Long.valueOf(userId));
        subscriptionUser.orElseThrow(() -> new Exception(Constants.NOT_FOUND_SUBSCRIPTION));

        boolean hasPay = false;
        String latestInvoiceId = subscription.getLatestInvoice();
        if (Objects.isNull(latestInvoiceId)) {
            hasPay = true;
        }else{
            Invoice invoice = Invoice.retrieve(latestInvoiceId);
            if (invoice.getAmountDue()==0||Constants.PAID_STATUS.equals(invoice.getStatus())) {
                hasPay = true;
            }
        }

        String newPriceId =
                subscription.getItems().getData().get(0).getPrice().getId();
        boolean samePlan = subscriptionUser.get().getPriceId().equals(newPriceId);
        updatedSubscription(subscription.getId(), subscription.getCancelAtPeriodEnd()&&samePlan,samePlan?subscriptionUser.get().getPriceId():newPriceId,hasPay);
    }

    /**
     * Handles the Stripe webhook event for a successful invoice payment. This method retrieves the invoice information from the event, checks if the invoice is associated with a subscription, and updates the local database to reflect the successful payment and update the subscription period based on the subscription ID provided in the event.
     * @param event the Stripe event object containing the details of the successful invoice payment
     * @throws Exception if there is an error retrieving the invoice information from Stripe or if there is an error updating the subscription status and period in the local database
     */
    private void handleSuccessPaymentUpdatedSubscription(Event event) throws Exception {

        EventDataObjectDeserializer dataObjectDeserializer =
                event.getDataObjectDeserializer();

        StripeObject stripeObject;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            stripeObject = dataObjectDeserializer.deserializeUnsafe();
        }

        Invoice invoice = (Invoice) stripeObject;
        if(invoice == null){
            return;
        }

        String subscriptionId = invoice.getSubscription();
        if(subscriptionId == null){
            String customerId = invoice.getCustomer();
            Map<String, Object> params = new HashMap<>();
            params.put("customer", customerId);
            params.put("limit", 1);

            SubscriptionCollection subscriptions =
                    com.stripe.model.Subscription.list(params);

            if(!subscriptions.getData().isEmpty()){
                subscriptionId = subscriptions.getData().get(0).getId();
            }
        }

        if(subscriptionId == null){
            log.error(Constants.NOT_FOUND_SUBSCRIPTION_ID_INVOICE, invoice.getId());
            return;
        }
        com.stripe.model.Subscription subscription =
                com.stripe.model.Subscription.retrieve(subscriptionId);

        String zone = subscription.getMetadata().get("zone");
        boolean isProration = invoice.getLines()
                .getData()
                .stream()
                .anyMatch(l -> Boolean.TRUE.equals(l.getProration()));
        SuccessPaymentUpdatedSubscription(subscriptionId,zone,isProration);
    }

    /**
     * Handles the Stripe webhook event for a failed invoice payment. This method retrieves the invoice information from the event, checks if the invoice is associated with a subscription, and updates the local database to reflect the failed payment and mark the subscription as failed based on the subscription ID provided in the event.
     * @param event the Stripe event object containing the details of the failed invoice payment
     * @throws Exception if there is an error retrieving the invoice information from Stripe or if there is an error updating the subscription status in the local database
     */
    private void handlePaymentFail(Event event) throws Exception {
        log.info("handlePaymentFail");
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Invoice invoice =
                (Invoice) event.getDataObjectDeserializer()
                        .getObject()
                        .orElse(null);

        if(invoice == null){return;}
        String subscriptionId = invoice.getSubscription();

        com.stripe.model.Subscription subscription =
                com.stripe.model.Subscription.retrieve(subscriptionId);

        if (subscription == null) {
            subscription = ApiResource.GSON.fromJson(
                    deserializer.getRawJson(),
                    com.stripe.model.Subscription.class
            );
            if (subscription == null) { return;}
        }

        failSubscription(subscription.getId());
    }

    /**
     * Creates a new subscription in the local database based on the provided user ID, Stripe subscription ID, zone, customer ID, and price ID. This method is called when a checkout session is completed successfully and a new subscription needs to be created for the user.
     * @param userId the ID of the user for whom the subscription is being created
     * @param stripeId the ID of the Stripe subscription associated with the new subscription
     * @param zone the time zone associated with the subscription, used for calculating subscription periods
     * @param customerId the ID of the Stripe customer associated with the subscription
     * @param priceId the ID of the Stripe price associated with the subscription plan
     */
    public void createSubscription(Long userId, String stripeId, String zone, String customerId, String priceId) {
        log.info("createSubscription");
        Subscription sub = new Subscription();
        sub.setUserId(userId);
        sub.setStripeSubscriptionId(stripeId);
        sub.setStatus(PaymentStatus.PENDIENTE_CONFIRMAR);
        sub.setCustomerId(customerId);
        sub.setPriceId(priceId);
        log.info(Constants.CREATE_NEW_SUBSCRIPTION);
        subscriptionRepository.save(sub);
    }

    /**
     * Updates the subscription in the local database to reflect a successful payment. This method retrieves the subscription based on the Stripe subscription ID, updates the subscription status to "paid", and adjusts the subscription period based on whether it is a proration or not. If the subscription does not have a start period date, it creates a new period starting from the current date. If the subscription already has a period and it is not a proration, it updates the period to start from the current date and end one month later.
     * @param stripeSubscriptionId the ID of the Stripe subscription associated with the subscription to be updated
     * @param zone the time zone associated with the subscription, used for calculating subscription periods
     * @param isProration a boolean flag indicating whether the payment is a proration, which affects how the subscription period is updated
     * @throws Exception if there is an error finding the subscription in the local database or if the subscription is not found
     */
    private void SuccessPaymentUpdatedSubscription(String stripeSubscriptionId, String zone, boolean isProration) throws Exception {
        log.info("updateSubscription");
        Optional<Subscription> sub =
                subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId);
        sub.orElseThrow(() -> new Exception(Constants.NOT_FOUND_SUBSCRIPTION));
        sub.get().setStripeSubscriptionId(stripeSubscriptionId);
        sub.get().setStatus(PaymentStatus.PAGADO);

        //If user doesn't have subscription create new period
        if (Objects.isNull(sub.get().getStartPeriodDate())) {
            log.info(Constants.SAVE_NEW_SUBSCRIPTION);
            sub.get().setStartPeriodDate(Utility.startDayWhitZone(Instant.now(), ZoneId.of(zone)));
            sub.get().setEndPeriodDate(Utility.plusMonth(Utility.endDayWhitZone(Instant.now(), ZoneId.of(zone)),1,ZoneId.of(zone)));
        }else{
            //If: subscription period ends update period, else(is Proration): period is same
            if(!isProration){
                log.info(Constants.UPDATE_PERIOD);
                sub.get().setStartPeriodDate(Utility.startDayWhitZone(Instant.now(), ZoneId.of(zone)));
                sub.get().setEndPeriodDate(Utility.plusMonth(Utility.endDayWhitZone(Instant.now(), ZoneId.of(zone)),1,ZoneId.of(zone)));
            }
        }
        subscriptionRepository.save(sub.get());
    }

    /**
     * Cancels the subscription in the local database based on the provided Stripe subscription ID. This method retrieves the subscription, updates its status to "canceled", and saves the updated subscription back to the database. This is typically called when a subscription is canceled either by the user or through a Stripe webhook event.
     * @param stripeId the ID of the Stripe subscription associated with the subscription to be canceled
     * @throws Exception if there is an error finding the subscription in the local database or if the subscription is not found
     */
    public void cancelSubscription(String stripeId) throws Exception {
        log.info("cancelSubscription");
        Optional<Subscription> sub =
                subscriptionRepository.findByStripeSubscriptionId(stripeId);
        sub.orElseThrow(() -> new Exception(Constants.NOT_FOUND_SUBSCRIPTION));

        sub.get().setStatus(PaymentStatus.CANCELADO);
        log.info(Constants.CANCEL_SUBSCRIPTION);
        subscriptionRepository.save(sub.get());
    }

    /**
     * Updates the subscription in the local database based on the provided Stripe subscription ID, cancellation status at the end of the period, and new price ID. This method retrieves the subscription, updates its status based on whether it is set to cancel at the end of the period or not, updates the price ID, and saves the updated subscription back to the database. This is typically called when a subscription is updated either by the user or through a Stripe webhook event.
     *
     * @param stripeId          the ID of the Stripe subscription associated with the subscription to be updated
     * @param cancelAtEndPeriod a boolean flag indicating whether the subscription is set to cancel at the end of the current billing period, which affects how the subscription status is updated
     * @param priceId           the ID of the new Stripe price associated with the subscription plan, which is updated in the subscription record
     * @param hasPay
     * @throws Exception if there is an error finding the subscription in the local database or if the subscription is not found
     */
    public void updatedSubscription(String stripeId, boolean cancelAtEndPeriod, String priceId, boolean hasPay) throws Exception {
        log.info("updatedSubscription");
        Optional<Subscription> sub =
                subscriptionRepository.findByStripeSubscriptionId(stripeId);
        sub.orElseThrow(() -> new Exception(Constants.NOT_FOUND_SUBSCRIPTION));

        if (cancelAtEndPeriod){
            log.info(Constants.CANCEL_AT_END_PERIOD);
            sub.get().setStatus(PaymentStatus.PENDIENTE_CANCELAR);
        }else {
            if (hasPay) {
                log.info(Constants.PAY_CONFIRM_SUBSCRIPTION);
                sub.get().setStatus(PaymentStatus.PAGADO);
            }else {
                log.info(Constants.PENDING_CONFIRM_SUBSCRIPTION);
                sub.get().setStatus(PaymentStatus.PENDIENTE_CONFIRMAR);
            }
        }
        sub.get().setPriceId(priceId);
        subscriptionRepository.save(sub.get());
    }

    /**
     * Updates the subscription in the local database to reflect a failed payment. This method retrieves the subscription based on the Stripe subscription ID, updates the subscription status to "failed", and saves the updated subscription back to the database. This is typically called when a payment for a subscription fails, either through a Stripe webhook event or other payment processing error.
     * @param stripeId the ID of the Stripe subscription associated with the subscription to be marked as failed
     * @throws Exception if there is an error finding the subscription in the local database or if the subscription is not found
     */
    private void failSubscription(String stripeId) throws Exception {
        log.info("failSubscription");
        Optional<Subscription> sub =
                subscriptionRepository.findByStripeSubscriptionId(stripeId);
        sub.orElseThrow(() -> new Exception(Constants.NOT_FOUND_SUBSCRIPTION));

        log.info(Constants.PAYMENT_SUBSCRIPTION_FAILED);
        sub.get().setStatus(PaymentStatus.FALLIDO);
        subscriptionRepository.save(sub.get());
    }

    /**
     * Finds a subscription in the local database based on the user ID. This method retrieves the subscription associated with the specified user ID and returns it as an Optional. This is typically used to check if a user has an active subscription or to retrieve subscription details for a user.
     * @param userId the ID of the user for whom to find the subscription
     * @return an Optional containing the Subscription associated with the specified user ID, or an empty Optional if no subscription is found
     * @throws Exception if there is an error retrieving the subscription from the local database
     */
    public Optional<Subscription> findByUserId(Long userId) throws Exception {
        log.info("findByUserId");
        return subscriptionRepository.findByUserId(userId);

    }

    /**
     * Finds a subscription in the local database based on the user ID and subscription status. This method retrieves the subscription associated with the specified user ID that has a status of either "paid" or "pending cancellation" and returns it as an Optional. This is typically used to check if a user has an active subscription or to retrieve subscription details for a user while filtering by specific subscription statuses.
     * @param userId the ID of the user for whom to find the subscription based on status
     * @return an Optional containing the Subscription associated with the specified user ID and having a status of either "paid" or "pending cancellation", or an empty Optional if no such subscription is found
     * @throws Exception if there is an error retrieving the subscription from the local database
     */
    public Optional<Subscription> findByUserIdAndStatus(Long userId) throws Exception {
        log.info("findByUserIdAndStatus");
        return subscriptionRepository.findByUserIdAndStatusIn(userId, List.of(PaymentStatus.PAGADO, PaymentStatus.PENDIENTE_CANCELAR));

    }

    /**
     * Saves a Stripe event in the local database to keep track of processed events and prevent duplicate processing. This method creates a new StripeEvent entity based on the event ID from the Stripe event and saves it to the database using the StripeEventRepository. This is typically called when handling Stripe webhook events to ensure that each event is processed only once.
     * @param event the Stripe event object containing the details of the event to be saved in the local database
     */
    private void saveEvent(Event event) {
        stripeEventRepository.save(new StripeEvent(event.getId()));
    }

    /**
     * Checks if the user has an active subscription by attempting to find a subscription associated with the user's ID. This method returns a PaymentSubscriptionResponseDto indicating whether a subscription exists for the user. If an exception occurs while trying to find the subscription, it catches the exception and returns a response indicating that no subscription exists.
     * @return a PaymentSubscriptionResponseDto containing a boolean flag indicating whether the user has an active subscription, and any relevant information about the subscription if it exists
     */
    public PaymentSubscriptionResponseDto existSubscription() {
        try {
            return new PaymentSubscriptionResponseDto(Objects.nonNull(this.findByUserId(SecurityUtils.getUserId())));
        } catch (Exception e) {
            return new PaymentSubscriptionResponseDto(false);
        }
    }

    /**
     * Retrieves the active subscription for the user based on their user ID. This method attempts to find a subscription associated with the user's ID that has a status of either "paid" or "pending cancellation". If such a subscription is found, it returns a SubscriptionDto containing the subscription details and the associated plan information. If no active subscription is found, it returns a SubscriptionDto containing the details of the free subscription plan.
     * @param userId the ID of the user for whom to retrieve the active subscription
     * @return a SubscriptionDto containing the details of the user's active subscription and the associated plan information, or the details of the free subscription plan if no active subscription is found
     * @throws Exception if there is an error retrieving the subscription from the local database
     */
    public SubscriptionDto getUserSubscriptionActive(Long userId) throws Exception {
        Optional<Subscription> subscription = this.findByUserIdAndStatus(userId);
        return subscription.map(value
                -> new SubscriptionDto(value, planService.getPlanByPriceId(value.getPriceId()))).orElseGet(()
                -> new SubscriptionDto(planService.getPlanByName(Constants.FREE_SUBSCRIPTION)));
    }
}
