package com.rorideas.payments.repository;

import com.rorideas.payments.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Plan entities in the database. Provides methods for retrieving plans based on various criteria.
 */
@Repository
public interface PlanRepository extends JpaRepository<Plan, Integer> {

    List<Plan> findAllByOrderByIdAsc();
    Optional<Plan> findByStripePriceId(String stripePriceId);

    Optional<Plan> findByName(String name);
}
