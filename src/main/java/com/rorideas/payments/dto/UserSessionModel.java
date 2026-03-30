package com.rorideas.payments.dto;

import com.rorideas.payments.enums.UserRol;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * Data Transfer Object (DTO) representing the user session information.
 * This class is used to store and transfer user session details such as username, time zone, user ID, and token version.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserSessionModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "User session name", example = "john_doe")
    private String username;

    @Schema(description = "User session zone", example = "America/New_York")
    private String zone;

    @Schema(description = "User session id", example = "123")
    private Integer userId;

    @Schema(description = "User session token version", example = "1")
    private Integer tokenVersion;

    @Schema(description = "User email", example = "john_doe@gmail.com")
    private String email;

    @Schema(description = "User admin session id", example = "123")
    private Integer userAdminId;

    @Schema(description = "User role", example = "PATIENT")
    private UserRol role;
}
