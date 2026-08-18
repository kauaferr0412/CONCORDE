package com.codagis.discordclone.ws;

/**
 * Status EFETIVO que os outros veem de alguem (diferente de UserStatus, que e' a
 * preferencia do proprio usuario) - OFFLINE cobre tanto "desconectado de verdade" quanto
 * "escolheu aparecer invisivel", de propositio: ninguem de fora consegue diferenciar os
 * dois casos, so' o proprio usuario ve seu status real em Configuracoes.
 */
public enum PresenceStatus {
    ONLINE,
    AWAY,
    DND,
    OFFLINE
}
