package com.ridelink.account_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class UpdateAccountStatusRequest {

    @Schema(description = "ACTIVE or BLOCKED (case-insensitive)", example = "BLOCKED")
    @NotBlank(message = "Status is required")
    private String status;

    public UpdateAccountStatusRequest() {
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
