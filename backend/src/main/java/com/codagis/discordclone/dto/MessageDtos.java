package com.codagis.discordclone.dto;

import java.time.Instant;

public class MessageDtos {

    /** Enviado pelo cliente via STOMP em /app/channel.{channelId}.send - content ou imageUrl (ou os
     * dois), e opcionalmente replyToId se for uma resposta a outra mensagem desse canal. */
    public record OutgoingChatMessage(String content, String imageUrl, Long replyToId) {}

    /** Enviado pelo cliente via STOMP em /app/channel.{channelId}.edit */
    public record EditChatMessage(Long messageId, String content) {}

    /** Enviado pelo cliente via STOMP em /app/channel.{channelId}.delete */
    public record DeleteChatMessage(Long messageId) {}

    /** Resuminho da mensagem original, embutido em quem responde a ela - assim o cliente nao
     * precisa buscar a mensagem original separada (e ainda funciona se ela for apagada depois,
     * so' o preview some). */
    public record ReplyPreview(Long id, String authorUsername, String authorAvatarUrl, String content, String imageUrl) {}

    /** Uma mensagem, retornada no historico (REST) e dentro dos eventos do WebSocket. */
    public record ChatMessage(Long id, Long channelId, Long authorId, String authorUsername, String authorAvatarUrl,
                               String content, String imageUrl, Instant createdAt, Instant editedAt,
                               Long replyToId, ReplyPreview replyTo) {}

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
