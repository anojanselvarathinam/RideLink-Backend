package com.ridelink.account_service.service;

import com.ridelink.account_service.dto.RegisterRequest;
import com.ridelink.account_service.dto.UserResponse;
import com.ridelink.account_service.entity.AccountStatus;
import com.ridelink.account_service.entity.Role;
import com.ridelink.account_service.entity.User;
import com.ridelink.account_service.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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