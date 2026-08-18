package com.codagis.discordclone.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Gera o token de acesso que o cliente React usa para conectar direto no servidor LiveKit
 * (SFU responsavel por audio/video/compartilhamento de tela via WebRTC).
 *
 * Formato: JWT assinado com o "api-secret" do LiveKit, contendo um "video grant"
 * (https://docs.livekit.io/home/get-started/authentication/). O Spring Boot NUNCA
 * carrega midia; ele so autoriza quem pode entrar em qual "room" (= canal de voz).
 */
@Service
public class LiveKitService {

    private static final ObjectMapper JSON = new ObjectMapper();

    private final SecretKey key;
    private final String apiKey;
    private final String wsUrl;
    private final long ttlMinutes;

    public LiveKitService(@Value("${app.livekit.api-key}") String apiKey,
                           @Value("${app.livekit.api-secret}") String apiSecret,
                           @Value("${app.livekit.ws-url}") String wsUrl,
                           @Value("${app.livekit.token-ttl-minutes}") long ttlMinutes) {
        this.apiKey = apiKey;
        this.wsUrl = wsUrl;
        this.ttlMinutes = ttlMinutes;
        this.key = Keys.hmacShaKeyFor(apiSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String getWsUrl() {
        return wsUrl;
    }

    /**
     * @param roomName normalmente "channel-{channelId}"
     * @param identity  identificador unico do usuario na room (ex: "user-42")
     * @param displayName nome mostrado na call (username)
     * @param avatarUrl URL da foto de perfil (ou null) - vai no "metadata" do participante,
     *                  assim quem esta na call ve a foto de todo mundo (ver VoiceCallContext.jsx)
     */
    public String generateAccessToken(String roomName, String identity, String displayName, String avatarUrl) {
        Instant now = Instant.now();

        Map<String, Object> videoGrant = new HashMap<>();
        videoGrant.put("room", roomName);
        videoGrant.put("roomJoin", true);
        videoGrant.put("canPublish", true);
        videoGrant.put("canSubscribe", true);
        videoGrant.put("canPublishData", true);
        // canPublishSources controla explicitamente microfone, camera e compartilhamento de tela (com audio)
        videoGrant.put("canPublishSources", new String[]{"camera", "microphone", "screen_share", "screen_share_audio"});

        var builder = Jwts.builder()
                .issuer(apiKey)
                .subject(identity)
                .claim("name", displayName)
                .claim("video", videoGrant)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlMinutes * 60)));

        if (avatarUrl != null && !avatarUrl.isBlank()) {
            builder.claim("metadata", toMetadataJson(avatarUrl));
        }

        // O LiveKit exige HS256 especificamente - sem isso, o jjwt escolhe o algoritmo
        // sozinho com base no tamanho da chave (HS512 pra chaves de 64+ bytes, como as
        // geradas com "openssl rand -hex 32"), e o LiveKit rejeita o token como invalido
        // por nao reconhecer o algoritmo.
        return builder.signWith(key, Jwts.SIG.HS256).compact();
    }

    private String toMetadataJson(String avatarUrl) {
        try {
            return JSON.writeValueAsString(Map.of("avatarUrl", avatarUrl));
        } catch (Exception e) {
            return "{}";
        }
    }
}
