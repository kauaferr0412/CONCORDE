package com.codagis.discordclone.security;

import com.codagis.discordclone.domain.Role;
import com.codagis.discordclone.repository.UserRepository;
import org.springframework.stereotype.Component;

/**
 * So o usuario com role ADMIN pode criar outros usuarios, criar servidores e liberar
 * acesso de alguem a um servidor. Verifica direto no banco (nao confia em claim do JWT)
 * para que revogar o admin de alguem tenha efeito imediato, sem esperar o token expirar.
 */
@Component
public class AdminGuard {

    private final UserRepository userRepository;

    public AdminGuard(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void assertAdmin(Long userId) {
        boolean isAdmin = userRepository.findById(userId)
                .map(u -> u.getRole() == Role.ADMIN)
                .orElse(false);
        if (!isAdmin) {
            throw new IllegalStateException("Somente o administrador pode fazer isso");
        }
    }
}
