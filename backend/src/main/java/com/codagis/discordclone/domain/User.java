package com.codagis.discordclone.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

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

    // Status que o proprio usuario escolhe (Online/Ausente/Nao perturbe/Invisivel) -
    // ver OnlinePresenceService pra como isso vira o que os OUTROS enxergam.
    // @ColumnDefault e' essencial aqui: como essa coluna e' nova e a tabela "users" ja tem
    // gente cadastrada em producao, o Postgres recusa um ALTER TABLE ADD COLUMN NOT NULL
    // sem um DEFAULT (nao tem como preencher as linhas que ja existem sem isso).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @ColumnDefault("'ONLINE'")
    @Builder.Default
    private UserStatus status = UserStatus.ONLINE;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
