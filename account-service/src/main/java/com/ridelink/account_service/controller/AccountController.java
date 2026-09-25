package com.ridelink.account_service.controller;

import com.ridelink.account_service.dto.RegisterRequest;
import com.ridelink.account_service.dto.LoginRequest;
import com.ridelink.account_service.dto.LoginResponse;
import com.ridelink.account_service.dto.UserResponse;
import com.ridelink.account_service.dto.UpdateProfileRequest;
import com.ridelink.account_service.dto.UpdateAccountStatusRequest;
import com.ridelink.account_service.dto.ChangePasswordRequest;
import com.ridelink.account_service.dto.MessageResponse;
import com.ridelink.account_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import java.util.NoSuchElementException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.MethodArgumentNotValidException;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

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

    @Operation(
        summary = "Log in to an account",
        description = "Passengers and drivers use their registered email; admins use their " +
                "configured username (for example, Admin). Only ACTIVE accounts can log in. " +
                "Returns a JWT token with tokenType Bearer."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Request validation error", content = @Content),
        @ApiResponse(responseCode = "401", description = "Invalid email, username, or password", content = @Content),
        @ApiResponse(responseCode = "403", description = "Account is not active", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.loginUser(request));
    }

    @Operation(
        summary = "Get the current account profile",
        description = "Returns the account belonging to the JWT subject. " +
                "Use Authorize to enter the token returned by login.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile returned",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUser(authentication.getName()));
    }

    @Operation(
        summary = "Update the current account profile",
        description = "Updates only the full name and phone number of the account " +
                "identified by the JWT. Use Authorize to enter the token returned by login.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid profile information", content = @Content),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateCurrentUser(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Change the current account password",
        description = "Verifies the current password and changes only the password of the " +
                "account identified by the JWT. The new password must contain at least 8 " +
                "characters, including uppercase, lowercase, a number, and a special character.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Password changed successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = MessageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid new password or confirmation mismatch", content = @Content),
        @ApiResponse(responseCode = "401", description = "Missing/invalid JWT or incorrect current password", content = @Content),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PutMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(userService.changePassword(authentication.getName(), request));
    }

    @Operation(
        summary = "List Passenger and Driver accounts",
        description = "Requires an ADMIN JWT. Returns Passenger and Driver profiles only, " +
                "or an empty array when no matching accounts exist.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account list returned successfully",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "ADMIN role required", content = @Content)
    })
    @GetMapping("/admin/users")
    public ResponseEntity<List<UserResponse>> getPassengerAndDriverAccounts() {
        return ResponseEntity.ok(userService.getPassengerAndDriverAccounts());
    }

    @Operation(
        summary = "Get a Passenger or Driver account by ID",
        description = "Requires an ACTIVE ADMIN account and a valid JWT. " +
                "Obtain accountId from GET /api/accounts/admin/users. " +
                "Returns a Passenger or Driver profile regardless of the target account status. " +
                "ADMIN accounts cannot be returned. Blocked or inactive callers receive HTTP 403.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account returned successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "ADMIN role required or requested account is ADMIN",
            content = @Content),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @GetMapping("/admin/users/{accountId}")
    public ResponseEntity<UserResponse> getPassengerOrDriverAccount(@PathVariable String accountId) {
        return ResponseEntity.ok(userService.getPassengerOrDriverAccount(accountId));
    }

    @Operation(
        summary = "Block or unblock a Passenger or Driver account",
        description = "Requires an ADMIN JWT. Changes only the target account status. " +
                "Accepts ACTIVE or BLOCKED case-insensitively; INACTIVE is not allowed. " +
                "ADMIN accounts cannot be updated. Obtain accountId from GET /api/accounts/admin/users.",
        security = @SecurityRequirement(name = "bearerAuth"),
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UpdateAccountStatusRequest.class),
                examples = {
                    @ExampleObject(name = "Block account", value = "{\"status\":\"BLOCKED\"}"),
                    @ExampleObject(name = "Unblock account", value = "{\"status\":\"ACTIVE\"}")
                }))
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account status updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid status", content = @Content),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT", content = @Content),
        @ApiResponse(responseCode = "403", description = "ADMIN role required or target is an ADMIN", content = @Content),
        @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @PatchMapping("/admin/users/{accountId}/status")
    public ResponseEntity<UserResponse> updateAccountStatus(
            @PathVariable String accountId,
            @Valid @RequestBody UpdateAccountStatusRequest request) {
        return ResponseEntity.ok(userService.updateAccountStatus(accountId, request));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handlePasswordValidation(
            MethodArgumentNotValidException exception) throws MethodArgumentNotValidException {
        if (!(exception.getBindingResult().getTarget() instanceof ChangePasswordRequest)) {
            throw exception;
        }

        // Do not expose or log rejected password values from validation errors.
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid password information"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleAccountNotFound(
            NoSuchElementException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentialsException(
            BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(
            AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", exception.getMessage()));
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
