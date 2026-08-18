package com.codagis.discordclone.dto;

public class VoiceDtos {

    /** Token JWT que o frontend usa para conectar direto no servidor LiveKit dessa sala/canal. */
    public record VoiceTokenResponse(String token, String wsUrl, String room, String identity) {}
}
