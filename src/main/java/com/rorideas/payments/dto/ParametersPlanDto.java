package com.rorideas.payments.dto;

import com.rorideas.payments.entity.ParametersPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data Transfer Object (DTO) for representing the parameters of a subscription plan, including limits on patients, balance, vital signs, medicines, and history days.
 * This DTO is used to transfer plan parameters between different layers of the application, such as from the service layer to the presentation layer, and to provide a structured format for API responses.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ParametersPlanDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Maximum number of patients allowed under the plan", example = "10")
    private Integer maxPatient;

    @Schema(description = "Maximum balance allowed under the plan", example = "100")
    private Integer maxBalance;

    @Schema(description = "Maximum number of vital signs allowed under the plan", example = "50")
    private Integer maxVitalSigna;

    @Schema(description = "Maximum number of medicines allowed under the plan", example = "20")
    private Integer maxMedicines;

    @Schema(description = "Days for max balance history of the plan", example = "365")
    private Integer historyDays;

    /**
     * Constructor that initializes the ParametersPlanDto based on a ParametersPlan entity, mapping the relevant fields from the entity to the DTO.
     * @param parametersPlan the ParametersPlan entity from which to create the DTO, containing the plan parameters to be transferred
     */
    public ParametersPlanDto(ParametersPlan parametersPlan) {
        this.maxPatient = parametersPlan.getMaxPatient();
        this.maxBalance = parametersPlan.getMaxBalance();
        this.maxVitalSigna = parametersPlan.getMaxVitalSigna();
        this.maxMedicines = parametersPlan.getMaxMedicines();
        this.historyDays = parametersPlan.getHistoryDays();
    }
}
