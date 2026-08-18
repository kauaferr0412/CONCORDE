package com.codagis.discordclone.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long channelId;

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false, length = 4000)
    private String content;

    /** URL publica no Google Cloud Storage, se a mensagem tiver uma imagem anexada. */
    @Column(length = 1000)
    private String imageUrl;

    @Builder.Default
    private Instant createdAt = Instant.now();

    /** Preenchido quando o autor (ou o admin) edita a mensagem - null = nunca editada. */
    private Instant editedAt;
}
