package com.aicitybrain.service;

import com.aicitybrain.domain.User;
import com.aicitybrain.dto.AuthDtos;
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

    public AuthDtos.CurrentUserResponse currentUser(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));
        return new AuthDtos.CurrentUserResponse(user.getUsername(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
