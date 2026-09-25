package com.ridelink.account_service.controller;

import com.ridelink.account_service.dto.RegisterRequest;
import com.ridelink.account_service.dto.UserResponse;
import com.ridelink.account_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
        summary = "Register a new account",
        description = "Registers only PASSENGER or DRIVER accounts"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Account registered successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    implementation = UserResponse.class
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid details or invalid role",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "An account already exists with this email",
            content = @Content
        )
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody RegisterRequest request) {

        UserResponse response =
                userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>>
            handleIllegalArgumentException(
                    IllegalArgumentException exception) {

        Map<String, String> errorResponse = Map.of(
                "error", exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>>
            handleIllegalStateException(
                    IllegalStateException exception) {

        Map<String, String> errorResponse = Map.of(
                "error", exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(errorResponse);
    }
}