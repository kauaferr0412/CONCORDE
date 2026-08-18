package com.codagis.discordclone.ws;

/** Snapshot de quem esta em um canal de voz agora - transmitido via /topic/channel.{id}.voice */
public record VoiceParticipantInfo(Long userId, String username, String avatarUrl, boolean micEnabled, boolean deafened) {}
