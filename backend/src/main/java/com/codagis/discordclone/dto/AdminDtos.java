package com.codagis.discordclone.dto;

import com.codagis.discordclone.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AdminDtos {

    /** ADMIN concede acesso de um usuario a um servidor (sem precisar de link de convite). */
    public record GrantAccessRequest(@NotNull Long userId) {}

    /**
     * ADMIN edita os dados de uma conta. "password" e' opcional - null/vazio mantem a senha
     * atual (nao da pra mandar a senha em texto puro so pra "nao mudar nada").
     */
    public record UpdateUserRequest(
            @NotBlank @Size(min = 3, max = 32) String username,
            @NotBlank @Email String email,
            String password,
            @NotNull Role role
    ) {}
}
