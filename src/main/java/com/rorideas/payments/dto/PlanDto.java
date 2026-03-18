package com.rorideas.payments.dto;

import com.rorideas.payments.entity.Plan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object (DTO) for representing a subscription plan, including its details and parameters.
 * This class is used to transfer plan data between different layers of the application, such as from the service layer to the presentation layer.
 * It includes fields for the plan's ID, price, name, description, details, and parameters, along with constructors and getter/setter methods.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PlanDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Id for price", example = "123")
    private Long id;

    @Schema(description = "Price identifier", example = "price_123")
    private String priceId;

    @Schema(description = "Price value", example = "9.99")
    private BigDecimal price;

    @Schema(description = "Plan name", example = "Basic Plan")
    private String name;

    @Schema(description = "Plan description", example = "This is the basic plan with limited features.")
    private String description;

    @Schema(description = "Plan details", example = "[{\"feature\": \"Feature 1\", \"value\": \"Value 1\"}, {\"feature\": \"Feature 2\", \"value\": \"Value 2\"}]")
    private List<DetailPlanDto> details;

    @Schema(description = "Plan parameters", example = "{\"maxPatient\": 100, \"maxBalance\": 10}, {\"maxPatient\": 200, \"maxBalance\": 20}")
    private ParametersPlanDto parametersPlan;

    /**
     * Constructor that initializes the PlanDto based on a Plan entity. It maps the fields from the Plan entity to the corresponding fields in the PlanDto, including converting the list of DetailPlan entities to a list of DetailPlanDto objects and handling the ParametersPlan entity if it exists.
     * @param plan the Plan entity from which to create the PlanDto
     */
    public PlanDto(Plan plan) {
        this.id = plan.getId();
        this.priceId = plan.getStripePriceId();
        this.price = plan.getPrice();
        this.name = plan.getName();
        this.description = plan.getDescription();
        this.details = plan.getDetailPlans().stream().map(DetailPlanDto::new).toList();
        this.parametersPlan = Objects.nonNull(plan.getParametersPlan()) ? new ParametersPlanDto(plan.getParametersPlan()) : null;
    }
}
