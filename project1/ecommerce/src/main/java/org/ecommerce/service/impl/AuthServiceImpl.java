package org.ecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import org.ecommerce.dto.LoginRequest;
import org.ecommerce.dto.LoginResponse;
import org.ecommerce.dto.RegisterRequest;
import org.ecommerce.dto.RegisterResponse;
import org.ecommerce.exception.EmailAlreadyExistsException;
import org.ecommerce.exception.InvalidCredentialsException;
import org.ecommerce.model.User;
import org.ecommerce.repository.UserRepository;
import org.ecommerce.service.api.AuthService;
import org.ecommerce.utility.AppLogger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService
{
    private static final Logger log = AppLogger.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        log.info("Registration started for email: {}", request.getEmail());

        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                log.warn("Registration failed. Email already exists: {}", request.getEmail());

                throw new EmailAlreadyExistsException("Email already exists");
            }

            User user = new User();

            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setAddress(request.getAddress());

            user.setPassword(passwordEncoder.encode(request.getPassword()));

            user.setRole("CUSTOMER");
            User savedUser = userRepository.save(user);

            log.info("Customer registered successfully. User ID: {}", savedUser.getId());

            return new RegisterResponse(
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    savedUser.getPhone(),
                    savedUser.getAddress(),
                    savedUser.getRole(),
                    savedUser.getCreatedAt(),
                    savedUser.getUpdatedAt()
            );

        } catch (EmailAlreadyExistsException e) {

            throw e;

        } catch (Exception e) {

            log.error("Unexpected error during registration for email: {}", request.getEmail(), e);

            throw new RuntimeException("Unable to register user");
        }
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        log.info("Login attempt for email: {}", request.getEmail());

        try {
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {

                        log.warn("Login failed. User not found: {}", request.getEmail());

                        return new InvalidCredentialsException("Invalid email or password");});

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

                log.warn("Login failed. Invalid password for email: {}", request.getEmail());

                throw new InvalidCredentialsException("Invalid email or password");
            }

            log.info("Login successful. User ID: {}", user.getId());

            return new LoginResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getPhone(),
                    user.getAddress(),
                    user.getRole(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );

        } catch (InvalidCredentialsException e) {

            throw e;

        } catch (Exception e) {

            log.error("Unexpected error during login for email: {}", request.getEmail(), e);

            throw new RuntimeException("Unable to login");
        }
    }


}
