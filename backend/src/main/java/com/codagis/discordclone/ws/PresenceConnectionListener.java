package com.codagis.discordclone.ws;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Liga a presenca global (OnlinePresenceService) ao ciclo de vida do WebSocket de chat -
 * conecta assim que o app abre (mesmo sem entrar em nenhum canal de voz), desconecta ao
 * fechar a aba/perder internet. O usuario (Principal) ja vem setado pelo
 * StompAuthChannelInterceptor no momento do CONNECT.
 */
@Component
public class PresenceConnectionListener {

    private final OnlinePresenceService presenceService;

    public PresenceConnectionListener(OnlinePresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Long userId = extractUserId(accessor);
        if (userId != null) {
            presenceService.connect(accessor.getSessionId(), userId);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        presenceService.disconnect(accessor.getSessionId());
    }

    private Long extractUserId(StompHeaderAccessor accessor) {
        if (accessor.getUser() instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
