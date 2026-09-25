package com.ridelink.account_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {

    @Schema(example = "Test Passenger Updated")
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must contain between 2 and 100 characters")
    private String fullName;

    @Schema(example = "+94771234567")
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$",
            message = "Phone number must contain 7 to 15 digits with an optional leading +")
    private String phoneNumber;

    public UpdateProfileRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
