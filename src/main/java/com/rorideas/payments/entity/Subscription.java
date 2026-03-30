package com.rorideas.payments.entity;

import com.rorideas.payments.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Entity class representing a subscription in the payment system. This class is mapped to the "suscripcion" table in the database and contains information about the subscription, including its status, associated user, and Stripe details.
 */
@Entity
@Table(name = "suscripcion",
        indexes = {
                @Index(name = "idx_suscripcion_id_suscripcion", columnList = "id_suscripcion"),
                @Index(name = "idx_suscripcion_id_usuario", columnList = "id_usuario"),
                @Index(name = "idx_suscripcion_id_stripe_suscripcion", columnList = "id_stripe_suscripcion")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_suscripcion", nullable = false, unique = true)
    private Long id;

    @Column(name = "id_stripe_suscripcion", nullable = false)
    private String stripeSubscriptionId ;

    @Column(name = "id_usuario", nullable = false, unique = true)
    private Long userId;

    @Column(name = "id_stripe_cliente", nullable = false)
    private String customerId;

    @Column(name = "id_stripe_price", nullable = false)
    private String priceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estatus", nullable = false, length = 50)
    private PaymentStatus status;

    @Column(name = "fecha_inicio_periodo")
    private Instant startPeriodDate;

    @Column(name = "fecha_fin_periodo")
    private Instant endPeriodDate;
}
