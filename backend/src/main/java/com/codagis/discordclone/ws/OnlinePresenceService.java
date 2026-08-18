package com.codagis.discordclone.ws;

import com.codagis.discordclone.domain.UserStatus;
import com.codagis.discordclone.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro em memoria de "quem esta com o app aberto agora" (WebSocket de chat conectado),
 * pra mostrar o status de cada um pra qualquer membro em comum de um servidor - diferente
 * do VoicePresenceService, que so' sabe quem esta DENTRO de uma call de voz especifica.
 *
 * O status EFETIVO que os outros veem (ver PresenceStatus) depende de duas coisas: se tem
 * alguma sessao WebSocket ativa (esta com o app aberto) E da preferencia do proprio usuario
 * (UserStatus, salva no banco) - Invisivel sempre vira OFFLINE pros outros, mesmo conectado.
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

    /** Chamado quando o proprio usuario muda o status em Configuracoes. */
    public void onStatusChanged(Long userId) {
        broadcast(userId);
    }

    private boolean hasActiveSession(Long userId) {
        Set<String> sessions = sessionsByUser.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    public PresenceStatus effectiveStatus(Long userId) {
        if (!hasActiveSession(userId)) {
            return PresenceStatus.OFFLINE;
        }
        UserStatus preference = userRepository.findById(userId).map(u -> u.getStatus()).orElse(UserStatus.ONLINE);
        return switch (preference) {
            case ONLINE -> PresenceStatus.ONLINE;
            case AWAY -> PresenceStatus.AWAY;
            case DND -> PresenceStatus.DND;
            case INVISIBLE -> PresenceStatus.OFFLINE;
        };
    }

    /** Usado pra montar a lista de membros de um servidor com o status de cada um de uma vez. */
    public Map<Long, PresenceStatus> effectiveStatusOf(Collection<Long> userIds) {
        Map<Long, PresenceStatus> result = new HashMap<>();
        for (Long userId : userIds) {
            result.put(userId, effectiveStatus(userId));
        }
        return result;
    }

    private void broadcast(Long userId) {
        messagingTemplate.convertAndSend("/topic/presence", new PresenceEvent(userId, effectiveStatus(userId)));
    }

    public record PresenceEvent(Long userId, PresenceStatus status) {}
}
