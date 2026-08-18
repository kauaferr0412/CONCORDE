package com.codagis.discordclone.controller;

import com.codagis.discordclone.dto.AdminDtos.GrantAccessRequest;
import com.codagis.discordclone.dto.AdminDtos.UpdateUserRequest;
import com.codagis.discordclone.dto.AuthDtos.CreateUserRequest;
import com.codagis.discordclone.dto.AuthDtos.UserResponse;
import com.codagis.discordclone.security.CurrentUser;
import com.codagis.discordclone.service.AuthService;
import com.codagis.discordclone.service.ServerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Todo endpoint aqui exige que quem chama seja ADMIN (checado dentro dos services via AdminGuard). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AuthService authService;
    private final ServerService serverService;
    private final CurrentUser currentUser;

    public AdminController(AuthService authService, ServerService serverService, CurrentUser currentUser) {
        this.authService = authService;
        this.serverService = serverService;
        this.currentUser = currentUser;
    }

    /** Cria uma conta de usuario. So o ADMIN pode fazer isso - nao ha cadastro publico. */
    @PostMapping("/users")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest req) {
        return authService.createUserAsAdmin(currentUser.id(), req);
    }

    /** Lista todos os usuarios (para o admin escolher a quem liberar acesso a um servidor). */
    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return authService.listUsersAsAdmin(currentUser.id());
    }

    /** Edita username/email/role/senha de uma conta. */
    @PutMapping("/users/{userId}")
    public UserResponse updateUser(@PathVariable Long userId, @Valid @RequestBody UpdateUserRequest req) {
        return authService.updateUserAsAdmin(currentUser.id(), userId, req);
    }

    /** Exclui uma conta (e as associações de servidor dela). */
    @DeleteMapping("/users/{userId}")
    public void deleteUser(@PathVariable Long userId) {
        authService.deleteUserAsAdmin(currentUser.id(), userId);
    }

    /** Libera o acesso de um usuario a um servidor (equivalente a "aceitar convite", mas direto). */
    @PostMapping("/servers/{serverId}/members")
    public void grantAccess(@PathVariable Long serverId, @Valid @RequestBody GrantAccessRequest req) {
        serverService.grantAccessAsAdmin(currentUser.id(), serverId, req.userId());
    }
}
