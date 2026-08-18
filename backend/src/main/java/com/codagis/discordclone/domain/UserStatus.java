package com.codagis.discordclone.domain;

/**
 * Status que o proprio usuario escolhe (Configuracoes > Status). INVISIBLE nunca aparece
 * pros outros como esta' - ver OnlinePresenceService.effectiveStatus, que traduz isso pra
 * OFFLINE na hora de mostrar pra qualquer outra pessoa.
 */
public enum UserStatus {
    ONLINE,
    AWAY,
    DND,
    INVISIBLE
}
