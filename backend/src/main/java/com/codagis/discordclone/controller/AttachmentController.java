package com.codagis.discordclone.controller;

import com.codagis.discordclone.domain.Channel;
import com.codagis.discordclone.dto.MessageDtos.AttachmentResponse;
import com.codagis.discordclone.repository.ChannelRepository;
import com.codagis.discordclone.security.CurrentUser;
import com.codagis.discordclone.service.GcsService;
import com.codagis.discordclone.service.ServerService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/channels")
public class AttachmentController {

    private final GcsService gcsService;
    private final ChannelRepository channelRepository;
    private final ServerService serverService;
    private final CurrentUser currentUser;

    public AttachmentController(GcsService gcsService, ChannelRepository channelRepository,
                                 ServerService serverService, CurrentUser currentUser) {
        this.gcsService = gcsService;
        this.channelRepository = channelRepository;
        this.serverService = serverService;
        this.currentUser = currentUser;
    }

    /** Envia uma imagem para o chat de um canal. Salva em potato/chat/{channelId}/... no GCS. */
    @PostMapping(value = "/{channelId}/attachments", consumes = "multipart/form-data")
    public AttachmentResponse uploadAttachment(@PathVariable Long channelId, @RequestParam("file") MultipartFile file) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Canal nao existe"));
        serverService.assertMember(channel.getServerId(), currentUser.id());

        String url = gcsService.upload(file, "chat/" + channelId);
        return new AttachmentResponse(url);
    }
}
