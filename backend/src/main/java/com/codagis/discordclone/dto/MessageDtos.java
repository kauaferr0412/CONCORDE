package com.codagis.discordclone.dto;

import java.time.Instant;

public class MessageDtos {

    /** Enviado pelo cliente via STOMP em /app/channel.{channelId}.send - content ou imageUrl (ou os dois). */
    public record OutgoingChatMessage(String content, String imageUrl) {}

    /** Enviado pelo cliente via STOMP em /app/channel.{channelId}.edit */
    public record EditChatMessage(Long messageId, String content) {}

    /** Enviado pelo cliente via STOMP em /app/channel.{channelId}.delete */
    public record DeleteChatMessage(Long messageId) {}

    /** Uma mensagem, retornada no historico (REST) e dentro dos eventos do WebSocket. */
    public record ChatMessage(Long id, Long channelId, Long authorId, String authorUsername, String authorAvatarUrl,
                               String content, String imageUrl, Instant createdAt, Instant editedAt) {}

    /**
     * O que realmente trafega em /topic/channel.{channelId}: um envelope com o tipo do evento,
     * assim o cliente sabe se deve adicionar, atualizar ou remover uma mensagem da lista.
     */
    public record ChatEvent(String type, ChatMessage message, Long messageId) {
        public static ChatEvent created(ChatMessage message) {
            return new ChatEvent("CREATED", message, null);
        }
        public static ChatEvent updated(ChatMessage message) {
            return new ChatEvent("UPDATED", message, null);
        }
        public static ChatEvent deleted(Long messageId) {
            return new ChatEvent("DELETED", null, messageId);
        }
    }

    /** Resposta do upload de imagem (POST /api/channels/{channelId}/attachments) */
    public record AttachmentResponse(String url) {}
}
