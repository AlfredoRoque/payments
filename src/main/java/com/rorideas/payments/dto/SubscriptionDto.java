package com.rorideas.payments.dto;

import com.rorideas.payments.entity.Subscription;
import com.rorideas.payments.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

/**
 * Data Transfer Object (DTO) for Subscription entity, used to transfer subscription data between different layers of the application.
 * It includes fields for subscription details such as id, stripe subscription id, user id, customer id, price id, status, start and end period dates, and the associated plan.
 * The class implements Serializable to allow for easy serialization and deserialization of subscription data when transferring it over the network or storing it in a session. It also includes constructors for creating instances from Subscription entities and PlanDto objects.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDto  implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Id for subscription", example = "123")
    private Long id;

    @Schema(description = "Id stripe for subscription", example = "sub_123")
    private String stripeSubscriptionId ;

    @Schema(description = "User id", example = "123")
    private Long userId;

    @Schema(description = "Stripe customer id", example = "cus_123")
    private String customerId;

    @Schema(description = "Stripe price id", example = "price_123")
    private String priceId;

    @Schema(description = "Subscription status", example = "PAGADO")
    private PaymentStatus status;

    @Schema(description = "Start period subscription", example = "2024-01-01T00:00:00Z")
    private Instant startPeriodDate;

    @Schema(description = "End period subscription", example = "2024-01-31T23:59:59Z")
    private Instant endPeriodDate;

    @Schema(description = "Plan for subscription", example = "Basic Plan")
    private PlanDto plan;

    /**
     * Constructor to create a SubscriptionDto from a Subscription entity and a PlanDto.
     * @param subscription the Subscription entity containing the subscription details
     * @param plan the PlanDto containing the plan details associated with the subscription
     */
    public SubscriptionDto(Subscription subscription, PlanDto plan) {
        this.id = subscription.getId();
        this.stripeSubscriptionId = subscription.getStripeSubscriptionId();
        this.userId = subscription.getUserId();
        this.customerId = subscription.getCustomerId();
        this.priceId = subscription.getPriceId();
        this.status = subscription.getStatus();
        this.startPeriodDate = subscription.getStartPeriodDate();
        this.endPeriodDate = subscription.getEndPeriodDate();
        this.plan = plan;
    }

    /**
     * Constructor to create a SubscriptionDto with only a PlanDto, used when subscription details are not available or needed.
     * @param planDto the PlanDto containing the plan details to be associated with the subscription
     */
    public SubscriptionDto(PlanDto planDto) {
        this.plan = planDto;
    }
}
