package com.rorideas.payments.repository;

import com.rorideas.payments.entity.StripeEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing StripeEvent entities, providing methods to perform CRUD operations and custom queries related to Stripe events.
 */
@Repository
public interface StripeEventRepository extends JpaRepository<StripeEvent, Integer> {
    Optional<StripeEvent> findByEvent(String event);
}
