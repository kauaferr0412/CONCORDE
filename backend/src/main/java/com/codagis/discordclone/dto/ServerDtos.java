package com.codagis.discordclone.dto;

import com.codagis.discordclone.domain.ChannelType;
import com.codagis.discordclone.domain.Role;
import com.codagis.discordclone.ws.PresenceStatus;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class ServerDtos {

    public record CreateServerRequest(@NotBlank String name) {}

    public record ServerResponse(Long id, String name, Long ownerId, String iconUrl) {}

    public record CreateChannelRequest(@NotBlank String name, ChannelType type) {}

    public record ChannelResponse(Long id, Long serverId, String name, ChannelType type) {}

    public record ServerWithChannels(ServerResponse server, List<ChannelResponse> channels) {}

    public record MemberResponse(Long userId, String username, String avatarUrl, PresenceStatus status, Role role) {}
}
