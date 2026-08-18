package com.codagis.discordclone.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 32)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String avatarUrl;

    // So o ADMIN pode criar outros usuarios e servidores; USER so acessa o que o ADMIN liberar.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Role role = Role.USER;

    // Preferencia do proprio usuario de aparecer "offline" pros outros mesmo estando
    // conectado (igual ao status "invisivel" do Discord) - ver OnlinePresenceService.
    @Column(nullable = false)
    @Builder.Default
    private boolean invisible = false;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
