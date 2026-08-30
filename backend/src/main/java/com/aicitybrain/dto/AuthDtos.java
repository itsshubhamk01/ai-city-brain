package com.aicitybrain.dto;

import com.aicitybrain.domain.Role;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
    ) {}

    public record LoginResponse(
        String token,
        String username,
        String fullName,
        Role role,
        Instant expiresAt
    ) {}

    public record CurrentUserResponse(
        String username,
        String fullName,
        String email,
        Role role
    ) {}
}
