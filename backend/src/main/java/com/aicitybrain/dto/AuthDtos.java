package com.aicitybrain.dto;

import com.aicitybrain.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
    ) {}

    public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 40) String username,
        @NotBlank @Size(min = 8) String password,
        @NotBlank @Size(max = 150) String fullName,
        @NotBlank @Email String email
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
