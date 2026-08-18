package com.codagis.discordclone.controller;

import com.codagis.discordclone.dto.ServerDtos.*;
import com.codagis.discordclone.security.CurrentUser;
import com.codagis.discordclone.service.ServerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servers")
public class ServerController {

    private final ServerService serverService;
    private final CurrentUser currentUser;

    public ServerController(ServerService serverService, CurrentUser currentUser) {
        this.serverService = serverService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public List<ServerResponse> listMyServers() {
        return serverService.listServersOfUser(currentUser.id());
    }

    @PostMapping
    public ServerResponse create(@Valid @RequestBody CreateServerRequest req) {
        return serverService.createServer(currentUser.id(), req);
    }

    @GetMapping("/{serverId}/members")
    public List<MemberResponse> listMembers(@PathVariable Long serverId) {
        return serverService.listMembers(serverId, currentUser.id());
    }

    @GetMapping("/{serverId}/channels")
    public List<ChannelResponse> listChannels(@PathVariable Long serverId) {
        return serverService.listChannels(serverId, currentUser.id());
    }

    @PostMapping("/{serverId}/channels")
    public ChannelResponse createChannel(@PathVariable Long serverId, @Valid @RequestBody CreateChannelRequest req) {
        return serverService.createChannel(serverId, currentUser.id(), req);
    }
}
