package com.codagis.discordclone.dto;

import com.codagis.discordclone.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record LoginRequest(
            @NotBlank String usernameOrEmail,
            @NotBlank String password
    ) {}

    public record UserResponse(Long id, String username, String email, String avatarUrl, Role role) {}

    public record AuthResponse(String token, UserResponse user) {}

    /** Usado pelo ADMIN para criar contas - nao existe mais cadastro publico. */
    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 32) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6) String password
    ) {}
}
