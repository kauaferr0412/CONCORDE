package com.codagis.discordclone.controller;

import com.codagis.discordclone.domain.User;
import com.codagis.discordclone.dto.AuthDtos.UserResponse;
import com.codagis.discordclone.repository.UserRepository;
import com.codagis.discordclone.security.CurrentUser;
import com.codagis.discordclone.service.GcsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users/me")
public class AvatarController {

    private final GcsService gcsService;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public AvatarController(GcsService gcsService, UserRepository userRepository, CurrentUser currentUser) {
        this.gcsService = gcsService;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
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

        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getAvatarUrl(), user.getRole());
    }
}
