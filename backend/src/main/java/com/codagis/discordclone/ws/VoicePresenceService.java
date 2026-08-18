package com.codagis.discordclone.ws;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro em memoria de "quem esta em qual canal de voz agora", para TODOS os membros
 * do servidor verem isso na barra lateral (nao so quem ja entrou na call, como no LiveKit).
 * Nao precisa de banco - e' presenca efemera, some quando o processo reinicia ou o usuario
 * desconecta o WebSocket.
 */
@Service
public class VoicePresenceService {

    private final SimpMessagingTemplate messagingTemplate;

    // channelId -> (userId -> info)
    private final Map<Long, Map<Long, VoiceParticipantInfo>> byChannel = new ConcurrentHashMap<>();
    // sessionId do STOMP -> canal/usuario, para limpar automaticamente se o socket cair sem "leave" explicito
    private final Map<String, Long> sessionChannel = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUser = new ConcurrentHashMap<>();

    public VoicePresenceService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void join(Long channelId, String sessionId, Long userId, String username, String avatarUrl) {
        byChannel.computeIfAbsent(channelId, k -> new ConcurrentHashMap<>())
                .put(userId, new VoiceParticipantInfo(userId, username, avatarUrl, true, false));
        sessionChannel.put(sessionId, channelId);
        sessionUser.put(sessionId, userId);
        broadcast(channelId);
    }

    public void leaveBySession(String sessionId) {
        Long channelId = sessionChannel.remove(sessionId);
        Long userId = sessionUser.remove(sessionId);
        if (channelId == null || userId == null) {
            return;
        }
        Map<Long, VoiceParticipantInfo> participants = byChannel.get(channelId);
        if (participants != null) {
            participants.remove(userId);
            if (participants.isEmpty()) {
                byChannel.remove(channelId);
            }
        }
        broadcast(channelId);
    }

    public void setMicEnabled(String sessionId, boolean micEnabled) {
        Long channelId = sessionChannel.get(sessionId);
        Long userId = sessionUser.get(sessionId);
        if (channelId == null || userId == null) {
            return;
        }
        Map<Long, VoiceParticipantInfo> participants = byChannel.get(channelId);
        if (participants == null) {
            return;
        }
        VoiceParticipantInfo current = participants.get(userId);
        if (current == null) {
            return;
        }
        participants.put(userId, new VoiceParticipantInfo(userId, current.username(), current.avatarUrl(), micEnabled, current.deafened()));
        broadcast(channelId);
    }

    /** Ensurdecer e' diferente de so mutar: a pessoa nem esta ouvindo ninguem, nao so calada. */
    public void setDeafened(String sessionId, boolean deafened) {
        Long channelId = sessionChannel.get(sessionId);
        Long userId = sessionUser.get(sessionId);
        if (channelId == null || userId == null) {
            return;
        }
        Map<Long, VoiceParticipantInfo> participants = byChannel.get(channelId);
        if (participants == null) {
            return;
        }
        VoiceParticipantInfo current = participants.get(userId);
        if (current == null) {
            return;
        }
        participants.put(userId, new VoiceParticipantInfo(userId, current.username(), current.avatarUrl(), current.micEnabled(), deafened));
        broadcast(channelId);
    }

    public List<VoiceParticipantInfo> snapshot(Long channelId) {
        Map<Long, VoiceParticipantInfo> participants = byChannel.get(channelId);
        return participants == null ? List.of() : new ArrayList<>(participants.values());
    }

    private void broadcast(Long channelId) {
        messagingTemplate.convertAndSend("/topic/channel." + channelId + ".voice", snapshot(channelId));
    }
}
