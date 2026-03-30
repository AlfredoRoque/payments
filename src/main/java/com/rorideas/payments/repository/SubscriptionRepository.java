package com.rorideas.payments.repository;

import com.rorideas.payments.entity.Subscription;
import com.rorideas.payments.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Subscription entities, providing methods to perform CRUD operations and custom queries based on Stripe subscription ID and user ID.
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Integer> {
    Optional<Subscription> findByStripeSubscriptionId(String stripeId);

    Optional<Subscription> findByUserId(Long userId);
    Optional<Subscription> findByUserIdAndStatusIn(Long userId, List<PaymentStatus> status);
}
