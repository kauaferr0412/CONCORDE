const DESKTOP_NOTIFICATIONS_KEY = "desktopNotificationsEnabled";

/**
 * Notificacao do PC quando chega mensagem nova - DESLIGADA por padrao (diferente dos outros
 * toggles do app) porque exige o navegador pedir permissao ao usuario explicitamente; nao
 * faz sentido pedir isso sem ele ter escolhido ligar primeiro.
 */
export function getDesktopNotificationsEnabled() {
  return localStorage.getItem(DESKTOP_NOTIFICATIONS_KEY) === "true";
}
export function setDesktopNotificationsEnabled(enabled) {
  localStorage.setItem(DESKTOP_NOTIFICATIONS_KEY, String(enabled));
}
