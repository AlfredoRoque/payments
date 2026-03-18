package com.rorideas.payments.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing the parameters associated with a subscription plan, including limits on patients, balances, vital signs, medicines, and history days.
 * This entity is linked to a specific plan and contains the necessary information to enforce the limits defined by the subscription plan.
 */
@Entity
@Table(name = "parametro_plan",
        indexes = {
                @Index(name = "idx_parametro_plan_id_parametro_plan", columnList = "id_parametro_plan"),
                @Index(name = "idx_parametro_plan_id_plan", columnList = "id_plan")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParametersPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametro_plan", nullable = false, unique = true)
    private Long id;

    @Column(name = "numero_pacientes",  nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer maxPatient = 0;

    @Column(name = "numero_balances",  nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer maxBalance = 0;

    @Column(name = "numero_signos_vitales",  nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer maxVitalSigna = 0;

    @Column(name = "numero_medicamentos",  nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer maxMedicines = 0;

    @Column(name = "dias_historial",  nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer historyDays = 0;

    @Column(name = "id_plan", nullable = false)
    private Long planId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan", insertable = false, updatable = false)
    private Plan plan;
}
