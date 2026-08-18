package com.codagis.discordclone.ws;

import com.codagis.discordclone.repository.UserRepository;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Cliente publica em /app/channel.{channelId}.voice.join|leave|mic quando entra/sai/muta
 * um canal de voz. Todo mundo que tem o canal na tela recebe o snapshot atualizado em
 * /topic/channel.{channelId}.voice - mesmo sem ter entrado na call (so quem entrou ve
 * quem esta falando de verdade, isso e' so "quem esta conectado").
 */
@Controller
public class VoicePresenceController {

    private final VoicePresenceService presenceService;
    private final UserRepository userRepository;

    public VoicePresenceController(VoicePresenceService presenceService, UserRepository userRepository) {
        this.presenceService = presenceService;
        this.userRepository = userRepository;
    }

    public record MicStatePayload(boolean micEnabled) {}
    public record DeafenStatePayload(boolean deafened) {}

    @MessageMapping("/channel.{channelId}.voice.join")
    public void join(@DestinationVariable Long channelId, @Header("simpSessionId") String sessionId, Principal principal) {
        Long userId = (Long) ((Authentication) principal).getPrincipal();
        var user = userRepository.findById(userId);
        String username = user.map(u -> u.getUsername()).orElse("user-" + userId);
        String avatarUrl = user.map(u -> u.getAvatarUrl()).orElse(null);
        presenceService.join(channelId, sessionId, userId, username, avatarUrl);
    }

    @MessageMapping("/channel.{channelId}.voice.leave")
    public void leave(@DestinationVariable Long channelId, @Header("simpSessionId") String sessionId) {
        presenceService.leaveBySession(sessionId);
    }

    @MessageMapping("/channel.{channelId}.voice.mic")
    public void mic(@DestinationVariable Long channelId, MicStatePayload payload, @Header("simpSessionId") String sessionId) {
        presenceService.setMicEnabled(sessionId, payload.micEnabled());
    }

    @MessageMapping("/channel.{channelId}.voice.deafen")
    public void deafen(@DestinationVariable Long channelId, DeafenStatePayload payload, @Header("simpSessionId") String sessionId) {
        presenceService.setDeafened(sessionId, payload.deafened());
    }
}
