package com.aicitybrain.service;

import com.aicitybrain.domain.Role;
import com.aicitybrain.domain.User;
import com.aicitybrain.dto.AuthDtos;
import com.aicitybrain.exception.ApiException;
import com.aicitybrain.repository.UserRepository;
import com.aicitybrain.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtService.issueToken(user.getUsername(), user.getRole().name());
        return new AuthDtos.LoginResponse(token, user.getUsername(), user.getFullName(), user.getRole(), jwtService.expiryOf(token));
    }

    /**
     * Real self-registration. New accounts are always created with the CITIZEN role —
     * elevated roles (Operations Manager, etc.) are granted by an administrator, not
     * self-selected at sign-up. Email verification and password-reset-by-email are a
     * later phase (they need a transactional email service); for now the account is
     * usable immediately, same as the seeded demo accounts.
     */
    @Transactional
    public AuthDtos.LoginResponse register(AuthDtos.RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw ApiException.conflict("That username is already taken.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("An account with that email already exists.");
        }

        User user = new User(
            request.username(),
            passwordEncoder.encode(request.password()),
            request.fullName(),
            request.email(),
            Role.CITIZEN
        );
        userRepository.save(user);

        String token = jwtService.issueToken(user.getUsername(), user.getRole().name());
        return new AuthDtos.LoginResponse(token, user.getUsername(), user.getFullName(), user.getRole(), jwtService.expiryOf(token));
    }

    public AuthDtos.CurrentUserResponse currentUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        return new AuthDtos.CurrentUserResponse(user.getUsername(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
