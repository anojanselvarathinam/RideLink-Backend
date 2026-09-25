package com.ridelink.account_service.service;

import com.ridelink.account_service.dto.RegisterRequest;
import com.ridelink.account_service.dto.LoginRequest;
import com.ridelink.account_service.dto.LoginResponse;
import com.ridelink.account_service.dto.UserResponse;
import com.ridelink.account_service.dto.UpdateProfileRequest;
import com.ridelink.account_service.dto.ChangePasswordRequest;
import com.ridelink.account_service.dto.MessageResponse;
import com.ridelink.account_service.entity.AccountStatus;
import com.ridelink.account_service.entity.Role;
import com.ridelink.account_service.entity.User;
import com.ridelink.account_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.ArrayList;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public List<UserResponse> getPassengerAndDriverAccounts() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = new ArrayList<>();

        for (User user : users) {
            if (user.getRole() == Role.PASSENGER || user.getRole() == Role.DRIVER) {
                responses.add(new UserResponse(user));
            }
        }

        return responses;
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));
        return new UserResponse(user);
    }

    public UserResponse updateCurrentUser(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        String fullName = request.getFullName().trim();
        String phoneNumber = request.getPhoneNumber().trim();

        // Check the trimmed name so padding cannot bypass the minimum length.
        if (fullName.length() < 2 || fullName.length() > 100) {
            throw new IllegalArgumentException(
                    "Full name must contain between 2 and 100 characters");
        }

        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);

        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser);
    }

    public MessageResponse changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("Account not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirm password must match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password must differ from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return new MessageResponse("Password changed successfully");
    }

    public LoginResponse loginUser(LoginRequest request) {
        String identifier = request.getIdentifier().trim();
        User user = userRepository.findByEmail(identifier.toLowerCase())
                .orElse(null);

        if (user == null) {
            user = userRepository.findByUsername(identifier).orElse(null);
        }

        if (user == null ||
                !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException(
                    "Invalid email, username, or password"
            );
        }

        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccessDeniedException("Account is not active");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(token, user);
    }

    public UserResponse registerUser(RegisterRequest request) {

        Role selectedRole;

        try {
            selectedRole = Role.valueOf(
                    request.getRole().trim().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Only PASSENGER or DRIVER registration is allowed"
            );
        }

        if (selectedRole != Role.PASSENGER &&
                selectedRole != Role.DRIVER) {

            throw new IllegalArgumentException(
                    "Only PASSENGER or DRIVER registration is allowed"
            );
        }

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException(
                    "An account already exists with this email"
            );
        }

        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        user.setPhoneNumber(
                request.getPhoneNumber().trim()
        );
        user.setRole(selectedRole);
        user.setStatus(AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);

        return new UserResponse(savedUser);
    }
}
