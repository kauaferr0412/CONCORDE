package com.codagis.discordclone.service;

import com.codagis.discordclone.domain.*;
import com.codagis.discordclone.dto.ServerDtos.*;
import com.codagis.discordclone.repository.*;
import com.codagis.discordclone.security.AdminGuard;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServerService {

    private final ServerRepository serverRepository;
    private final ChannelRepository channelRepository;
    private final MembershipRepository membershipRepository;
    private final AdminGuard adminGuard;

    public ServerService(ServerRepository serverRepository, ChannelRepository channelRepository,
                          MembershipRepository membershipRepository, AdminGuard adminGuard) {
        this.serverRepository = serverRepository;
        this.channelRepository = channelRepository;
        this.membershipRepository = membershipRepository;
        this.adminGuard = adminGuard;
    }

    /** So o ADMIN pode criar servidores. */
    @Transactional
    public ServerResponse createServer(Long ownerId, CreateServerRequest req) {
        adminGuard.assertAdmin(ownerId);
        Server server = serverRepository.save(Server.builder()
                .name(req.name())
                .ownerId(ownerId)
                .build());

        membershipRepository.save(Membership.builder().serverId(server.getId()).userId(ownerId).build());

        // Canais padrao, igual ao Discord quando voce cria um servidor novo
        channelRepository.save(Channel.builder().serverId(server.getId()).name("geral").type(ChannelType.TEXT).build());
        channelRepository.save(Channel.builder().serverId(server.getId()).name("Geral").type(ChannelType.VOICE).build());

        return toResponse(server);
    }

    public List<ServerResponse> listServersOfUser(Long userId) {
        return membershipRepository.findByUserId(userId).stream()
                .map(m -> serverRepository.findById(m.getServerId()).orElse(null))
                .filter(s -> s != null)
                .map(this::toResponse)
                .toList();
    }

    /** ADMIN libera o acesso de um usuario a um servidor, sem precisar de link de convite. */
    @Transactional
    public void grantAccessAsAdmin(Long requesterId, Long serverId, Long targetUserId) {
        adminGuard.assertAdmin(requesterId);
        if (!serverRepository.existsById(serverId)) {
            throw new IllegalArgumentException("Servidor nao existe");
        }
        if (!membershipRepository.existsByServerIdAndUserId(serverId, targetUserId)) {
            try {
                membershipRepository.save(Membership.builder().serverId(serverId).userId(targetUserId).build());
            } catch (DataIntegrityViolationException e) {
                // ja virou membro por outra requisicao concorrente - resultado desejado alcancado, ignora
            }
        }
    }

    public void assertMember(Long serverId, Long userId) {
        if (!membershipRepository.existsByServerIdAndUserId(serverId, userId)) {
            throw new IllegalStateException("Usuario nao pertence a esse servidor");
        }
    }

    public List<ChannelResponse> listChannels(Long serverId, Long userId) {
        assertMember(serverId, userId);
        return channelRepository.findByServerIdOrderByIdAsc(serverId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChannelResponse createChannel(Long serverId, Long userId, CreateChannelRequest req) {
        assertMember(serverId, userId);
        Channel channel = channelRepository.save(Channel.builder()
                .serverId(serverId)
                .name(req.name())
                .type(req.type() == null ? ChannelType.TEXT : req.type())
                .build());
        return toResponse(channel);
    }

    private ServerResponse toResponse(Server server) {
        return new ServerResponse(server.getId(), server.getName(), server.getOwnerId(), server.getIconUrl());
    }

    private ChannelResponse toResponse(Channel channel) {
        return new ChannelResponse(channel.getId(), channel.getServerId(), channel.getName(), channel.getType());
    }
}
