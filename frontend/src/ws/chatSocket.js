import { Client } from "@stomp/stompjs";

// Monta o WS a partir da MESMA pagina que carregou o app (mesma logica do api/client.js) -
// wss:// se a pagina for https (ngrok), ws:// se for http (localhost). O proxy do Vite
// (ver vite.config.js) encaminha "/ws" pro backend de verdade.
const WS_URL =
  import.meta.env.VITE_WS_URL ||
  `${window.location.protocol === "https:" ? "wss" : "ws"}://${window.location.host}/ws`;

export function createChatClient(token) {
  const client = new Client({
    brokerURL: WS_URL,
    connectHeaders: { Authorization: `Bearer ${token}` },
    reconnectDelay: 3000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
  });
  return client;
}

/** onEvent recebe um ChatEvent: { type: "CREATED"|"UPDATED"|"DELETED", message?, messageId? } */
export function subscribeToChannel(client, channelId, onEvent) {
  return client.subscribe(`/topic/channel.${channelId}`, (frame) => {
    onEvent(JSON.parse(frame.body));
  });
}

export function sendChatMessage(client, channelId, content, imageUrl) {
  client.publish({
    destination: `/app/channel.${channelId}.send`,
    body: JSON.stringify({ content: content || "", imageUrl: imageUrl || null }),
  });
}

export function editChatMessage(client, channelId, messageId, content) {
  client.publish({
    destination: `/app/channel.${channelId}.edit`,
    body: JSON.stringify({ messageId, content }),
  });
}

export function deleteChatMessage(client, channelId, messageId) {
  client.publish({
    destination: `/app/channel.${channelId}.delete`,
    body: JSON.stringify({ messageId }),
  });
}

/**
 * Presenca de canal de voz: "quem esta conectado agora" e' visivel para QUALQUER membro
 * do servidor (nao precisa ter entrado na call). Ja o indicador de "quem esta falando"
 * fica restrito a quem realmente entrou na call, vindo do proprio LiveKit (ver VoiceChannel.jsx).
 */
export function subscribeToVoicePresence(client, channelId, onUpdate) {
  return client.subscribe(`/topic/channel.${channelId}.voice`, (frame) => {
    onUpdate(JSON.parse(frame.body));
  });
}

export function publishVoiceJoin(client, channelId) {
  client.publish({ destination: `/app/channel.${channelId}.voice.join`, body: "{}" });
}

export function publishVoiceLeave(client, channelId) {
  client.publish({ destination: `/app/channel.${channelId}.voice.leave`, body: "{}" });
}

export function publishVoiceMicState(client, channelId, micEnabled) {
  client.publish({
    destination: `/app/channel.${channelId}.voice.mic`,
    body: JSON.stringify({ micEnabled }),
  });
}

export function publishVoiceDeafenState(client, channelId, deafened) {
  client.publish({
    destination: `/app/channel.${channelId}.voice.deafen`,
    body: JSON.stringify({ deafened }),
  });
}
