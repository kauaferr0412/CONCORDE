import { useEffect, useRef } from "react";
import { useVoiceCall } from "../context/VoiceCallContext.jsx";
import { EyeIcon, EyeOffIcon, HeadphonesOffIcon, MaximizeIcon, MicOffIcon, VolumeIcon } from "./icons.jsx";
import Avatar from "./Avatar.jsx";

/** Slider de volume 0-200% (o Discord/navegador so vai ate 100% - aqui passa disso via
    Web Audio, ver webAudioMix em VoiceCallContext.jsx). Reaproveitado pra voz e transmissão. */
function VolumeSlider({ value, onChange, label }) {
  return (
    <div className="volume-slider-row" title={label}>
      <VolumeIcon size={13} className="voice-status-icon" />
      <input
        type="range"
        min={0}
        max={200}
        step={5}
        value={value}
        onClick={(e) => e.stopPropagation()}
        onChange={(e) => onChange(Number(e.target.value))}
        className={"volume-slider" + (value > 100 ? " boosted" : "")}
      />
      <span className="volume-slider-value">{value}%</span>
    </div>
  );
}

/**
 * Vista de UM canal de voz. A conexao em si (LiveKit) vive no VoiceCallContext, entao
 * ela sobrevive mesmo se voce sair desse canal para ler um canal de texto - igual ao
 * Discord, que te mantem na call enquanto voce navega pelo servidor.
 */
export default function VoiceChannel({ channel }) {
  const {
    activeChannel,
    connected,
    micEnabled,
    participants,
    speakingIds,
    micLevel,
    screenShares,
    selectedScreenShareSid,
    selectScreenShare,
    toggleWatchScreenShare,
    participantVolumes,
    streamVolumes,
    setParticipantVolume,
    setStreamVolume,
    joinChannel,
    registerVideoContainer,
  } = useVoiceCall();

  const videoContainerRef = useRef(null);
  const isThisChannelActive = connected && activeChannel?.id === channel.id;
  const selectedShare = screenShares.find((s) => s.sid === selectedScreenShareSid) || null;

  useEffect(() => {
    if (!isThisChannelActive) return;
    registerVideoContainer(videoContainerRef.current);
    return () => registerVideoContainer(null);
  }, [isThisChannelActive, registerVideoContainer]);

  function handleFullscreen() {
    const videoEl = videoContainerRef.current?.querySelector("video");
    if (!videoEl) return;
    const request = videoEl.requestFullscreen || videoEl.webkitRequestFullscreen;
    request?.call(videoEl);
  }

  if (!isThisChannelActive) {
    return (
      <div className="voice-channel">
        <div className="chat-header">🔊 {channel.name}</div>
        <div className="voice-join">
          {activeChannel && activeChannel.id !== channel.id ? (
            <div style={{ textAlign: "center" }}>
              <p className="voice-hint" style={{ marginBottom: 12 }}>
                Você está conectado em 🔊 {activeChannel.name}. Entrar aqui vai te tirar de lá.
              </p>
              <button onClick={() => joinChannel(channel)}>Trocar para esta call</button>
            </div>
          ) : (
            <button onClick={() => joinChannel(channel)}>Entrar na call</button>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="voice-channel">
      <div className="chat-header">🔊 {channel.name}</div>

      <div className="voice-body">
        <section className="voice-section">
          <p className="voice-section-title">SEU MICROFONE</p>
          <div className="mic-meter-row">
            <div className="mic-meter-track">
              <div className="mic-meter-fill" style={{ width: `${micLevel}%` }} />
            </div>
            <span className="mic-meter-value">{micEnabled ? `${micLevel}%` : "mutado"}</span>
          </div>
          <p className="voice-hint">
            Fale perto do microfone — a barra acima deve se mexer instantaneamente. Use os ícones 🎤/🎧 na barra
            inferior esquerda para mutar ou ensurdecer.
          </p>
        </section>

        <section className="voice-section">
          <p className="voice-section-title">NA CALL — {participants.length}</p>
          <div className="voice-participants">
            {participants.map((p) => (
              <div key={p.identity} className={"voice-participant" + (speakingIds.has(p.identity) ? " speaking" : "")}>
                <div className="voice-participant-row">
                  <Avatar name={p.name} url={p.avatarUrl} className="voice-avatar" />
                  <span>{p.name}</span>
                  {p.deafened ? (
                    <span className="voice-status-badge" title="Ensurdecido - não está ouvindo ninguém">
                      <HeadphonesOffIcon size={13} /> ensurdecido
                    </span>
                  ) : (
                    !p.micEnabled && (
                      <span className="voice-status-badge danger" title="Microfone mutado">
                        <MicOffIcon size={13} /> mudo
                      </span>
                    )
                  )}
                </div>
                {/* Volume so' faz sentido pra voz dos OUTROS - a sua propria voz voce nao ouve. */}
                {!p.isLocal && (
                  <VolumeSlider
                    value={participantVolumes[p.identity] ?? 100}
                    onChange={(v) => setParticipantVolume(p.identity, v)}
                    label={`Volume de ${p.name} (padrão 100%, pode passar de 100%)`}
                  />
                )}
              </div>
            ))}
          </div>
          <p className="voice-hint">
            O anel verde acende em volta de quem está falando de verdade — só quem está dentro da call vê esse
            indicador. Arraste o controle de volume de cada pessoa para deixá-la mais alta (até 200%) ou mais baixa.
          </p>
        </section>

        <section className="voice-section">
          <div className="voice-section-header">
            <p className="voice-section-title">COMPARTILHAMENTO DE TELA</p>
            {screenShares.length > 0 && (
              <button className="icon-btn" onClick={handleFullscreen} title="Ver em tela cheia">
                <MaximizeIcon />
              </button>
            )}
          </div>
          {screenShares.length === 0 ? (
            <p className="voice-hint" style={{ margin: 0 }}>
              Ninguém está compartilhando a tela agora.
            </p>
          ) : (
            screenShares.length > 1 && (
              <div className="screenshare-tabs">
                {screenShares.map((s) => (
                  <button
                    key={s.sid}
                    className={"screenshare-tab" + (s.sid === selectedScreenShareSid ? " active" : "")}
                    onClick={() => selectScreenShare(s.sid)}
                  >
                    🖥️ {s.name}
                  </button>
                ))}
              </div>
            )
          )}

          {selectedShare && !selectedShare.isLocal && (
            <div className="screenshare-controls">
              <button
                type="button"
                className="link-btn screenshare-watch-btn"
                onClick={() => toggleWatchScreenShare(selectedShare.sid)}
                title={
                  selectedShare.watching
                    ? "Parar de assistir (economiza dados - você continua na call de voz normalmente)"
                    : "Voltar a assistir esta transmissão"
                }
              >
                {selectedShare.watching ? (
                  <>
                    <EyeOffIcon size={14} /> Parar de assistir
                  </>
                ) : (
                  <>
                    <EyeIcon size={14} /> Assistir transmissão
                  </>
                )}
              </button>
              <VolumeSlider
                value={streamVolumes[selectedShare.participantIdentity] ?? 100}
                onChange={(v) => setStreamVolume(selectedShare.participantIdentity, v)}
                label={`Volume do áudio da transmissão de ${selectedShare.name} (padrão 100%, pode passar de 100%)`}
              />
            </div>
          )}

          <div className="screenshare-stage">
            {/* Fica sempre montado (mesmo sem ninguem compartilhando) para o VoiceCallContext
                ter uma referencia estavel de onde anexar o video quando alguem comecar. */}
            <div
              className={"voice-video-grid" + (screenShares.length === 0 || !selectedShare?.watching ? " empty" : "")}
              ref={videoContainerRef}
              onDoubleClick={handleFullscreen}
            />
            {selectedShare && !selectedShare.watching && (
              <p className="screenshare-paused-hint">
                Você não está assistindo esta transmissão agora. Clique em "Assistir transmissão" acima para voltar.
              </p>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
