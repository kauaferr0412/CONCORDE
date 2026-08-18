package com.codagis.discordclone.service;

import com.codagis.discordclone.domain.Message;
import com.codagis.discordclone.domain.Role;
import com.codagis.discordclone.domain.User;
import com.codagis.discordclone.dto.MessageDtos.ChatMessage;
import com.codagis.discordclone.dto.MessageDtos.ReplyPreview;
import com.codagis.discordclone.repository.MessageRepository;
import com.codagis.discordclone.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ChatMessage save(Long channelId, Long authorId, String content, String imageUrl, Long replyToId) {
        boolean hasText = content != null && !content.isBlank();
        boolean hasImage = imageUrl != null && !imageUrl.isBlank();
        if (!hasText && !hasImage) {
            throw new IllegalArgumentException("Mensagem vazia - escreva algo ou anexe uma imagem");
        }
        // So aceita responder a uma mensagem que existe DE VERDADE nesse mesmo canal - evita
        // referenciar mensagem de outro canal ou um id inventado.
        Long validReplyToId = null;
        if (replyToId != null) {
            validReplyToId = messageRepository.findById(replyToId)
                    .filter(m -> m.getChannelId().equals(channelId))
                    .map(Message::getId)
                    .orElse(null);
        }
        Message saved = messageRepository.save(Message.builder()
                .channelId(channelId)
                .authorId(authorId)
                .content(hasText ? content : "")
                .imageUrl(hasImage ? imageUrl : null)
                .replyToId(validReplyToId)
                .build());
        return toDto(saved);
    }

    /** So o autor da mensagem ou o ADMIN podem editar. Imagem anexada nao muda na edicao, so o texto. */
    @Transactional
    public ChatMessage edit(Long channelId, Long messageId, Long requesterId, String newContent) {
        Message message = findInChannel(channelId, messageId);
        assertCanModify(message, requesterId);
        if (newContent == null || newContent.isBlank()) {
            throw new IllegalArgumentException("Mensagem nao pode ficar vazia");
        }
        message.setContent(newContent);
        message.setEditedAt(Instant.now());
        return toDto(messageRepository.save(message));
    }

    /** So o autor da mensagem ou o ADMIN podem apagar. */
    @Transactional
    public void delete(Long channelId, Long messageId, Long requesterId) {
        Message message = findInChannel(channelId, messageId);
        assertCanModify(message, requesterId);
        messageRepository.delete(message);
    }

    private Message findInChannel(Long channelId, Long messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Mensagem nao existe"));
        if (!message.getChannelId().equals(channelId)) {
            throw new IllegalArgumentException("Mensagem nao pertence a esse canal");
        }
        return message;
    }

    private void assertCanModify(Message message, Long requesterId) {
        if (message.getAuthorId().equals(requesterId)) {
            return;
        }
        boolean isAdmin = userRepository.findById(requesterId).map(u -> u.getRole() == Role.ADMIN).orElse(false);
        if (!isAdmin) {
            throw new IllegalStateException("Voce so pode editar ou apagar suas proprias mensagens");
        }
    }

    public List<ChatMessage> history(Long channelId) {
        return messageRepository.findTop50ByChannelIdOrderByIdDesc(channelId).stream()
                .sorted(Comparator.comparing(Message::getId))
                .map(this::toDto)
                .toList();
    }

    private ChatMessage toDto(Message m) {
        User author = userRepository.findById(m.getAuthorId()).orElse(null);
        String username = author != null ? author.getUsername() : "desconhecido";
        String avatarUrl = author != null ? author.getAvatarUrl() : null;
        ReplyPreview replyTo = m.getReplyToId() != null ? buildReplyPreview(m.getReplyToId()) : null;
        return new ChatMessage(m.getId(), m.getChannelId(), m.getAuthorId(), username, avatarUrl, m.getContent(),
                m.getImageUrl(), m.getCreatedAt(), m.getEditedAt(), m.getReplyToId(), replyTo);
    }

    /** null quando a mensagem original ja foi apagada - o cliente mostra "mensagem removida". */
    private ReplyPreview buildReplyPreview(Long originalId) {
        return messageRepository.findById(originalId).map(original -> {
            User originalAuthor = userRepository.findById(original.getAuthorId()).orElse(null);
            String originalUsername = originalAuthor != null ? originalAuthor.getUsername() : "desconhecido";
            String originalAvatarUrl = originalAuthor != null ? originalAuthor.getAvatarUrl() : null;
            return new ReplyPreview(original.getId(), originalUsername, originalAvatarUrl, original.getContent(), original.getImageUrl());
        }).orElse(null);
    }
}
