package com.codagis.discordclone.service;

import com.codagis.discordclone.domain.Role;
import com.codagis.discordclone.domain.User;
import com.codagis.discordclone.dto.AuthDtos.*;
import com.codagis.discordclone.repository.UserRepository;
import com.codagis.discordclone.security.AdminGuard;
import com.codagis.discordclone.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AdminGuard adminGuard;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                        AdminGuard adminGuard) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.adminGuard = adminGuard;
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.usernameOrEmail())
                .or(() -> userRepository.findByEmail(req.usernameOrEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Credenciais invalidas"));
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciais invalidas");
        }
        return buildAuthResponse(user);
    }

    /** So o ADMIN pode criar contas - nao existe mais cadastro publico (ver AdminController). */
    @Transactional
    public UserResponse createUserAsAdmin(Long requesterId, CreateUserRequest req) {
        adminGuard.assertAdmin(requesterId);
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("Nome de usuario ja esta em uso");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email ja esta em uso");
        }
        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .role(Role.USER)
                .build();
        user = userRepository.save(user);
        return toUserResponse(user);
    }

    public List<UserResponse> listUsersAsAdmin(Long requesterId) {
        adminGuard.assertAdmin(requesterId);
        return userRepository.findAll().stream().map(this::toUserResponse).toList();
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user.getId(), user.getUsername());
        return new AuthResponse(token, toUserResponse(user));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getAvatarUrl(), user.getRole(), user.getStatus());
    }
}
