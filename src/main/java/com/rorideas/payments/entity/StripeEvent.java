package com.rorideas.payments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing a Stripe event, which is used to store information about events received from Stripe.
 * Each event has a unique identifier and a name that describes the type of event.
 */
@Entity
@Table(name = "evento_stripe",
        indexes = {
                @Index(name = "idx_evento_stripe_evento", columnList = "evento")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StripeEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evento_stripe", nullable = false, unique = true)
    private Long id;

    @Column(name = "evento", nullable = false, length = 254, unique = true)
    private String event;

    /**
     * Constructor for creating a StripeEvent with the specified event name.
     * @param event the name of the Stripe event to be stored in the database
     */
    public StripeEvent(String event) {
        this.event = event;
    }
}
