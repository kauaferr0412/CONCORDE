package com.codagis.discordclone.dto;

import com.codagis.discordclone.domain.Role;
import com.codagis.discordclone.domain.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record LoginRequest(
            @NotBlank String usernameOrEmail,
            @NotBlank String password
    ) {}

    public record UserResponse(Long id, String username, String email, String avatarUrl, Role role, UserStatus status,
                                String nickname, String bio) {}

    public record AuthResponse(String token, UserResponse user) {}

    /** Usado pelo ADMIN para criar contas - nao existe mais cadastro publico. */
    public record CreateUserRequest(
            @NotBlank @Size(min = 3, max = 32) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6) String password
    ) {}

    /** Usuario troca seu proprio status (Online/Ausente/Nao perturbe/Invisivel). */
    public record StatusRequest(@NotNull UserStatus status) {}

    /** Usuario edita o proprio perfil (apelido/bio) - ambos opcionais, string vazia limpa o campo. */
    public record ProfileRequest(@Size(max = 32) String nickname, @Size(max = 190) String bio) {}
}
