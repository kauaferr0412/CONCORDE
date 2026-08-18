package com.codagis.discordclone.controller;

import com.codagis.discordclone.dto.MessageDtos.ChatMessage;
import com.codagis.discordclone.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/channels")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /** Historico das ultimas mensagens do canal (novas mensagens chegam via WebSocket). */
    @GetMapping("/{channelId}/messages")
    public List<ChatMessage> history(@PathVariable Long channelId) {
        return messageService.history(channelId);
    }
}
