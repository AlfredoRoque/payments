package com.rorideas.payments.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a subscription plan, including details such as name, price, Stripe price ID, and description.
 * It also maintains relationships with DetailPlan and ParametersPlan entities.
 */
@Entity
@Table(name = "plan",
        indexes = {
                @Index(name = "idx_plan_id_plan", columnList = "id_plan"),
                @Index(name = "idx_plan_id_precio_stripe", columnList = "id_precio_stripe")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Plan {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_plan", nullable = false, unique = true)
        private Long id;

        @Column(name = "nombre", nullable = false, length = 50)
        private String name;

        @Column(name = "precio", nullable = false)
        private BigDecimal price;

        @Column(name = "id_precio_stripe", nullable = false, length = 50)
        private String stripePriceId;

        @Column(name = "descripcion", length = 254)
        private String description;

        @JsonManagedReference
        @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<DetailPlan> detailPlans = new ArrayList<>();

        @OneToOne(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
        private ParametersPlan parametersPlan;
}
