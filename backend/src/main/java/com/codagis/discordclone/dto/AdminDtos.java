package com.codagis.discordclone.dto;

import jakarta.validation.constraints.NotNull;

public class AdminDtos {

    /** ADMIN concede acesso de um usuario a um servidor (sem precisar de link de convite). */
    public record GrantAccessRequest(@NotNull Long userId) {}
}
