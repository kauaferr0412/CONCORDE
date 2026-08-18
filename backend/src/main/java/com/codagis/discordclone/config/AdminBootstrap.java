package com.codagis.discordclone.config;

import com.codagis.discordclone.domain.Role;
import com.codagis.discordclone.domain.User;
import com.codagis.discordclone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Garante que sempre existe uma conta ADMIN, criando-a na primeira subida.
 * Sem isso ninguem conseguiria logar, ja que o cadastro publico foi removido -
 * agora so o ADMIN pode criar outros usuarios (ver AdminController).
 */
@Component
public class AdminBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           @Value("${app.admin.username}") String adminUsername,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.ADMIN)) {
            return;
        }
        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
        System.out.println("==============================================================");
        System.out.println(" Conta ADMIN criada: usuario='" + adminUsername + "'");
        System.out.println(" Troque a senha padrao em application.yml (app.admin.password)");
        System.out.println("==============================================================");
    }
}
