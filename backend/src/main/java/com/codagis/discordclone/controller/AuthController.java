package com.codagis.discordclone.controller;

import com.codagis.discordclone.dto.AuthDtos.*;
import com.codagis.discordclone.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** Nao ha mais cadastro publico - contas so sao criadas pelo ADMIN (ver AdminController). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
