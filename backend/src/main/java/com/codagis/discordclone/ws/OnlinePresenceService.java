package com.codagis.discordclone.ws;

import com.codagis.discordclone.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Registro em memoria de "quem esta com o app aberto agora" (WebSocket de chat conectado),
 * pra mostrar online/offline pra qualquer membro em comum de um servidor - diferente do
 * VoicePresenceService, que so' sabe quem esta DENTRO de uma call de voz especifica.
 *
 * "Online" pros outros = tem pelo menos uma aba/dispositivo conectado E o usuario nao
 * escolheu aparecer offline (ver User.invisible / onVisibilityChanged).
 */
@Service
public class OnlinePresenceService {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    // userId -> sessionIds ativos (varias abas/dispositivos podem estar conectados ao mesmo tempo)
    private final Map<Long, Set<String>> sessionsByUser = new ConcurrentHashMap<>();
    // sessionId -> userId, pra saber de quem e' a sessao que caiu, sem precisar varrer tudo
    private final Map<String, Long> userBySession = new ConcurrentHashMap<>();

    public OnlinePresenceService(SimpMessagingTemplate messagingTemplate, UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
    }

    public void connect(String sessionId, Long userId) {
        sessionsByUser.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
        userBySession.put(sessionId, userId);
        broadcast(userId);
    }

    public void disconnect(String sessionId) {
        Long userId = userBySession.remove(sessionId);
        if (userId == null) {
            return;
        }
        Set<String> sessions = sessionsByUser.get(userId);
        if (sessions != null) {
            sessions.remove(sessionId);
            if (sessions.isEmpty()) {
                sessionsByUser.remove(userId);
            }
        }
        broadcast(userId);
    }

    /** Chamado quando o proprio usuario muda a preferencia de aparecer online/offline. */
    public void onVisibilityChanged(Long userId) {
        broadcast(userId);
    }

    private boolean hasActiveSession(Long userId) {
        Set<String> sessions = sessionsByUser.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public boolean isOnline(Long userId) {
        return hasActiveSession(userId) && !userRepository.findById(userId).map(u -> u.isInvisible()).orElse(false);
    }

    /** Usado pra montar a lista de membros de um servidor com o status de cada um de uma vez. */
    public Set<Long> onlineAmong(Collection<Long> userIds) {
        return userIds.stream().filter(this::isOnline).collect(Collectors.toSet());
    }

    private void broadcast(Long userId) {
        messagingTemplate.convertAndSend("/topic/presence", new PresenceEvent(userId, isOnline(userId)));
    }

    public record PresenceEvent(Long userId, boolean online) {}
}
