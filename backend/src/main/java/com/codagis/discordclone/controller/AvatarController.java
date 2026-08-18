package com.codagis.discordclone.controller;

import com.codagis.discordclone.domain.User;
import com.codagis.discordclone.dto.AuthDtos.StatusRequest;
import com.codagis.discordclone.dto.AuthDtos.UserResponse;
import com.codagis.discordclone.repository.UserRepository;
import com.codagis.discordclone.security.CurrentUser;
import com.codagis.discordclone.service.GcsService;
import com.codagis.discordclone.ws.OnlinePresenceService;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/me")
public class AvatarController {

    private final GcsService gcsService;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final OnlinePresenceService presenceService;

    public AvatarController(GcsService gcsService, UserRepository userRepository, CurrentUser currentUser,
                             OnlinePresenceService presenceService) {
        this.gcsService = gcsService;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.presenceService = presenceService;
    }

    /** Troca a foto de perfil do usuario logado. Salva em potato/avatars/{userId}/... no GCS. */
    @PostMapping(value = "/avatar", consumes = "multipart/form-data")
    @Transactional
    public UserResponse uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = currentUser.id();
        String url = gcsService.upload(file, "avatars/" + userId);

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("Usuario nao encontrado"));
        user.setAvatarUrl(url);
        userRepository.save(user);

        return toResponse(user);
    }

    /**
     * Troca o status do usuario logado (Online/Ausente/Nao perturbe/Invisivel). O usuario
     * continua conectado normalmente em qualquer opcao - so' muda o que os OUTROS enxergam
     * (Invisivel aparece como offline pra todo mundo, ver OnlinePresenceService). Reflete
     * na hora pra quem estiver com a lista de membros na tela.
     */
    @PutMapping("/status")
    @Transactional
    public UserResponse setStatus(@Valid @RequestBody StatusRequest req) {
        Long userId = currentUser.id();
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalStateException("Usuario nao encontrado"));
        user.setStatus(req.status());
        userRepository.save(user);
        presenceService.onStatusChanged(userId);
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getAvatarUrl(), user.getRole(), user.getStatus());
    }
}
