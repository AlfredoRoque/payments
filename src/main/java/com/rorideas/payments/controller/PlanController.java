package com.rorideas.payments.controller;

import com.rorideas.payments.dto.PlanDto;
import com.rorideas.payments.service.PlanService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing Plans, providing endpoints to retrieve Plan information.
 */
@RequiredArgsConstructor
@RestController
@Tag(name = "Plan Service", description = "Service for managing Plans, including retrieval of Plan information.")
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    /**
     * Endpoint to retrieve a list of all available Plans.
     * @return List<PlanDto> containing the details of all available Plans.
     */
    @GetMapping
    public ResponseEntity<List<PlanDto>> getPlans(){
        return ResponseEntity.ok(planService.getPlans());
    }
}
