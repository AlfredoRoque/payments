package com.rorideas.payments.service;

import com.rorideas.payments.dto.PlanDto;
import com.rorideas.payments.repository.PlanRepository;
import com.rorideas.payments.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing subscription plans, including retrieving plan information based on price ID or name.
 */
@RequiredArgsConstructor
@Service
public class PlanService {

    private final PlanRepository planRepository;

    /**
     * Retrieves a list of all subscription plans, ordered by their ID in ascending order, excluding the special plan.
     * @return a list of PlanDto objects representing the available subscription plans, excluding the special plan.
     */
    public List<PlanDto> getPlans() {
        return planRepository.findAllByOrderByIdAsc().stream().map(PlanDto::new).filter(planDto -> !planDto.getName().equals(Constants.SPECIAL_PLAN)).toList();
    }

    /**
     * Retrieves a subscription plan based on the provided Stripe price ID. If no plan is found with the given price ID, a RuntimeException is thrown.
     * @param priceId the Stripe price ID associated with the subscription plan to be retrieved.
     * @return a PlanDto object representing the subscription plan associated with the provided Stripe price ID.
     */
    public PlanDto getPlanByPriceId(String priceId) {
        return planRepository.findByStripePriceId(priceId).map(PlanDto::new).orElseThrow(() -> new RuntimeException(Constants.NOT_FOUND_PLAN));
    }

    /**
     * Retrieves a subscription plan based on the provided name. If no plan is found with the given name, a RuntimeException is thrown.
     * @param name the name of the subscription plan to be retrieved.
     * @return a PlanDto object representing the subscription plan associated with the provided name.
     */
    public PlanDto getPlanByName(String name) {
        return planRepository.findByName(name).map(PlanDto::new).orElseThrow(() -> new RuntimeException(Constants.NOT_FOUND_PLAN));
    }
}
