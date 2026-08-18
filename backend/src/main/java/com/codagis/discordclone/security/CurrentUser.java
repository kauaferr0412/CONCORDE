package com.codagis.discordclone.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Le o id do usuario autenticado, colocado no SecurityContext pelo JwtAuthFilter. */
@Component
public class CurrentUser {

    public Long id() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Long userId) {
            return userId;
        }
        throw new IllegalStateException("Usuario nao autenticado");
    }
}
