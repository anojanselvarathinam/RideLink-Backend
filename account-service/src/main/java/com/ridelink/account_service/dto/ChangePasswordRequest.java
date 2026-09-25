package com.ridelink.account_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @Schema(format = "password", example = "CurrentPassword@20", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @Schema(format = "password", example = "NewPassword@21", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must contain at least 8 characters")
    @Pattern(regexp = "(?s)^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^a-zA-Z0-9\\s]).*$",
            message = "New password must contain uppercase, lowercase, a number, and a special character")
    private String newPassword;

    @Schema(format = "password", example = "NewPassword@21", accessMode = Schema.AccessMode.WRITE_ONLY)
    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    public ChangePasswordRequest() {
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
