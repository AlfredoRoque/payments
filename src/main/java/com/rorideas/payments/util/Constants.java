package com.rorideas.payments.util;

import lombok.experimental.UtilityClass;

/**
 * Utility class that contains constant values used throughout the application, such as subscription plan names and messages related to subscription management.
 */
@UtilityClass
public class Constants {

    public static final String FREE_SUBSCRIPTION = "FREE";
    public static final String SPECIAL_PLAN = "SPECIAL";
    public static final String NOT_FOUND_PLAN = "Plan not found";
    public static final String INVALID_SIGNATURE = "Invalid signature";
    public static final String ALREADY_PROCESS = "Already processed";
    public static final String NOT_FOUND_SUBSCRIPTION = "Subscription not found";
    public static final String CANCEL_PLAN = "Suscripcion cancelada, sigue con los beneficios hasta el final del periodo actual.";
    public static final String UPDATE_PLAN_SUBSCRIPTION = "Se actualizo el plan de suscripción correctamente.";
    public static final String CREATE_NEW_SUBSCRIPTION = "Create new subscription";
    public static final String SAVE_NEW_SUBSCRIPTION = "new subscription saved";
    public static final String UPDATE_PERIOD = "update period dates";
    public static final String CANCEL_SUBSCRIPTION = "cancel subscription";
    public static final String CANCEL_AT_END_PERIOD = "cancel at end period";
    public static final String PENDING_CONFIRM_SUBSCRIPTION = "pending to confirm subscription";
    public static final String PAY_CONFIRM_SUBSCRIPTION = "subscription has been paid";
    public static final String PAYMENT_SUBSCRIPTION_FAILED = "Payment subscription failed";

    //Stripe events
    public static final String CHECKOUT_SESSION_COMPLETED = "checkout.session.completed";
    public static final String CUSTOMER_SUBSCRIPTION_DELETED = "customer.subscription.deleted";
    public static final String CUSTOMER_SUBSCRIPTION_UPDATED = "customer.subscription.updated";
    public static final String INVOICE_PAYMENT_SUCCESS = "invoice.payment_succeeded";
    public static final String INVOICE_PAYMENT_FAILED = "invoice.payment_failed";


    public static final String UNHANDLED_EVENT = "Unhandled event type: {}";
    public static final String PAID_STATUS = "paid";
    public static final String NOT_FOUND_SUBSCRIPTION_ID_INVOICE = "No subscriptionId found for invoice {}";
}
