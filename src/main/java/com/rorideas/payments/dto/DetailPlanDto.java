package com.rorideas.payments.dto;

import com.rorideas.payments.entity.DetailPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data Transfer Object (DTO) for representing the details of a subscription plan, including its ID, icon, and description.
 * This DTO is used to transfer plan detail information between different layers of the application, such as between the service layer and the presentation layer.
 * It implements Serializable to allow for easy serialization and deserialization of the object when transferring data over the network or when storing it in a session. The class includes annotations for generating getters, setters, constructors, and for providing schema information for API documentation.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DetailPlanDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Id for price", example = "123")
    private Long id;

    @Schema(description = "Icon identifier", example = "icon_123")
    private String icon;

    @Schema(description = "Description of the plan detail", example = "Access to basic features")
    private String description;

    /**
     * Constructor that initializes the DetailPlanDto object based on a DetailPlan entity. It extracts the relevant information from the DetailPlan entity and sets the corresponding fields in the DetailPlanDto.
     * @param detailPlan the DetailPlan entity from which to create the DetailPlanDto
     */
    public DetailPlanDto(DetailPlan detailPlan) {
        this.id = detailPlan.getId();
        this.icon = detailPlan.getIcon();
        this.description = detailPlan.getDescription();
    }
}
