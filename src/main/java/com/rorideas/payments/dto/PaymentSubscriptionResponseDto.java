package com.rorideas.payments.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * DTO for the response of payment subscription operations, including payment URL, message, and subscription existence status.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSubscriptionResponseDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Payment url", example = "https://checkout.stripe.com/pay/fgf...")
    private String paymentUrl;

    @Schema(description = "Message response", example = "Subscription cancelled successfully")
    private String message;

    @Schema(description = "Exist subscription", example = "true")
    private boolean existSubscription;

    /**
     * Constructor for creating a PaymentSubscriptionResponseDto with either a payment URL or a message, based on the isUrl flag.
     * @param paymentUrl the payment URL or message to be set in the response
     * @param isUrl a boolean flag indicating whether the provided paymentUrl parameter is a URL (true) or a message (false)
     */
    public PaymentSubscriptionResponseDto(String paymentUrl, boolean isUrl) {
        if(isUrl) {
            this.paymentUrl = paymentUrl;
        } else {
            this.message = paymentUrl;
        }
    }

    /**
     * Constructor for creating a PaymentSubscriptionResponseDto with the existence status of a subscription.
     * @param existSubscription a boolean indicating whether a subscription exists (true) or not (false), which will be set in the response
     */
    public PaymentSubscriptionResponseDto(Boolean existSubscription) {
        this.existSubscription = existSubscription;
    }
}
