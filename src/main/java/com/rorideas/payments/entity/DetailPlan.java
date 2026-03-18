package com.rorideas.payments.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing the details of a plan, including its icon, description, and association with a specific plan.
 */
@Entity
@Table(name = "detalle_plan",
        indexes = {
                @Index(name = "idx_detail_plan_id_detail_plan", columnList = "id_detail_plan"),
                @Index(name = "idx_detail_plan_id_plan", columnList = "id_plan")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetailPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detail_plan", nullable = false, unique = true)
    private Long id;

    @Column(name = "icono", nullable = false, length = 50)
    private String icon;

    @Column(name = "descripcion", nullable = false)
    private String description;

    @Column(name = "id_plan", nullable = false)
    private Long planId;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plan", insertable = false, updatable = false)
    private Plan plan;
}
