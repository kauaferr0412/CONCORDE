import { useEffect, useRef, useState } from "react";
import { useMicLevel } from "../utils/useMicLevel";
import {
  getNoiseSuppressionEnabled,
  getSavedAudioInput,
  getSavedAudioOutput,
  getSoundEffectsEnabled,
  setNoiseSuppressionEnabled,
  setSavedAudioInput,
  setSavedAudioOutput,
  setSoundEffectsEnabled,
} from "../utils/audioSettings";
import {
  formatShortcut,
  getDeafenShortcut,
  getMuteShortcut,
  isOnlyModifier,
  setDeafenShortcut,
  setMuteShortcut,
  shortcutFromEvent,
} from "../utils/keyboardShortcuts";
import { playJoinSound } from "../utils/soundEffects";
import { useAuth } from "../context/AuthContext.jsx";
import api from "../api/client";
import Avatar from "./Avatar.jsx";

/** Mesmos 4 status do Discord - "Invisível" nunca aparece como tal pros outros, so' como offline
    (ver PresenceStatus.java no backend). O proprio usuario sempre ve seu status real aqui. */
const STATUS_OPTIONS = [
  { value: "ONLINE", label: "Online", hint: "Aparece disponível pros outros", dotClass: "online" },
  { value: "AWAY", label: "Ausente", hint: "Aparece com um ícone de ausente", dotClass: "away" },
  { value: "DND", label: "Não perturbe", hint: "Aparece com um ícone vermelho", dotClass: "dnd" },
  { value: "INVISIBLE", label: "Invisível", hint: "Aparece offline pra todo mundo, mas continua usando o app normalmente", dotClass: "offline" },
];

/** Campo de "gravar atalho": clica em Alterar, aperta a combinacao desejada, pronto. */
function ShortcutRecorder({ value, onChange }) {
  const [recording, setRecording] = useState(false);

  function handleKeyDown(e) {
    e.preventDefault();
    if (isOnlyModifier(e)) return; // espera uma tecla "de verdade" junto do Ctrl/Shift/etc
    if (e.key === "Escape") {
      setRecording(false);
      return;
    }
    onChange(shortcutFromEvent(e));
    setRecording(false);
  }

  return (
    <div className="shortcut-row">
      <kbd className="shortcut-kbd">{formatShortcut(value)}</kbd>
      {recording ? (
        <input
          autoFocus
          className="shortcut-capture"
          placeholder="Pressione as teclas..."
          onKeyDown={handleKeyDown}
          onBlur={() => setRecording(false)}
          readOnly
        />
      ) : (
        <button type="button" className="link-btn" onClick={() => setRecording(true)}>
          Alterar
        </button>
      )}
    </div>
  );
}

/**
 * Configuracoes de audio: escolher microfone/alto-falante, testar o microfone ouvindo a
 * propria voz, e atalhos de teclado pra mutar/ensurdecer sem precisar clicar em nada.
 * Tudo fica salvo no localStorage e vale a partir da proxima call (ver VoiceCallContext.jsx).
 */
export default function SettingsModal({ onClose }) {
  const { user, updateUser } = useAuth();
  const [avatarUploading, setAvatarUploading] = useState(false);
  const [avatarError, setAvatarError] = useState("");
  const avatarInputRef = useRef(null);
  const [visibilitySaving, setVisibilitySaving] = useState(false);
  const [visibilityError, setVisibilityError] = useState("");

  const [inputDevices, setInputDevices] = useState([]);
  const [outputDevices, setOutputDevices] = useState([]);
  const [selectedInput, setSelectedInput] = useState(getSavedAudioInput());
  const [selectedOutput, setSelectedOutput] = useState(getSavedAudioOutput());
  const [soundEffects, setSoundEffects] = useState(getSoundEffectsEnabled());
  const [noiseSuppression, setNoiseSuppression] = useState(getNoiseSuppressionEnabled());
  const [muteShortcut, setMuteShortcutState] = useState(getMuteShortcut());
  const [deafenShortcut, setDeafenShortcutState] = useState(getDeafenShortcut());
  const [permissionError, setPermissionError] = useState("");
  const [testing, setTesting] = useState(false);
  const [outputSupported, setOutputSupported] = useState(true);

  const streamRef = useRef(null);
  const audioElRef = useRef(null);
  const { level, start: startMeter, stop: stopMeter } = useMicLevel();

  useEffect(() => {
    // O navegador so mostra os nomes dos dispositivos depois de uma permissao de microfone
    // ser concedida pelo menos uma vez - por isso pedimos getUserMedia so pra "destravar" os labels.
    navigator.mediaDevices
      .getUserMedia({ audio: true })
      .then((stream) => {
        stream.getTracks().forEach((t) => t.stop());
        return navigator.mediaDevices.enumerateDevices();
      })
      .then((devices) => {
        setInputDevices(devices.filter((d) => d.kind === "audioinput"));
        setOutputDevices(devices.filter((d) => d.kind === "audiooutput"));
      })
      .catch((err) => setPermissionError("Não foi possível acessar o microfone: " + err.message));

    setOutputSupported(typeof HTMLMediaElement !== "undefined" && "setSinkId" in HTMLMediaElement.prototype);

    return () => stopTest();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function startTest() {
    setPermissionError("");
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        audio: selectedInput ? { deviceId: { exact: selectedInput } } : true,
      });
      streamRef.current = stream;

      // Toca de volta o que o microfone esta captando - e' assim que voce "se ouve".
      // Recomenda-se usar fone de ouvido para evitar microfonia (eco/feedback).
      if (audioElRef.current) {
        audioElRef.current.srcObject = stream;
        audioElRef.current.muted = false;
        await audioElRef.current.play();
        if (selectedOutput && outputSupported) {
          try {
            await audioElRef.current.setSinkId(selectedOutput);
          } catch (err) {
            console.warn("Não foi possível trocar o dispositivo de saída:", err);
          }
        }
      }

      startMeter(stream.getAudioTracks()[0]);
      setTesting(true);
    } catch (err) {
      setPermissionError("Não consegui abrir o microfone selecionado: " + err.message);
    }
  }

  function stopTest() {
    stopMeter();
    if (streamRef.current) {
      streamRef.current.getTracks().forEach((t) => t.stop());
      streamRef.current = null;
    }
    if (audioElRef.current) audioElRef.current.srcObject = null;
    setTesting(false);
  }

  function handleSave() {
    setSavedAudioInput(selectedInput);
    setSavedAudioOutput(selectedOutput);
    setSoundEffectsEnabled(soundEffects);
    setNoiseSuppressionEnabled(noiseSuppression);
    setMuteShortcut(muteShortcut);
    setDeafenShortcut(deafenShortcut);
    stopTest();
    onClose();
  }

  async function handleAvatarChange(e) {
    const file = e.target.files?.[0];
    e.target.value = "";
    if (!file) return;

    setAvatarError("");
    setAvatarUploading(true);
    try {
      const formData = new FormData();
      formData.append("file", file);
      const { data } = await api.post("/api/users/me/avatar", formData);
      updateUser({ avatarUrl: data.avatarUrl });
    } catch (err) {
      setAvatarError(err.response?.data?.error || "Falha ao enviar a foto");
    } finally {
      setAvatarUploading(false);
    }
  }

  async function handleStatusChange(status) {
    setVisibilityError("");
    setVisibilitySaving(true);
    try {
      const { data } = await api.put("/api/users/me/status", { status });
      updateUser({ status: data.status });
    } catch (err) {
      setVisibilityError(err.response?.data?.error || "Não foi possível salvar isso agora");
    } finally {
      setVisibilitySaving(false);
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal settings-modal" onClick={(e) => e.stopPropagation()}>
        <h2>Foto de perfil</h2>
        <div className="avatar-picker">
          <Avatar name={user?.username} url={user?.avatarUrl} className="avatar-picker-preview" />
          <div>
            <input
              type="file"
              accept="image/png,image/jpeg,image/gif,image/webp"
              ref={avatarInputRef}
              onChange={handleAvatarChange}
              hidden
            />
            <button type="button" onClick={() => avatarInputRef.current?.click()} disabled={avatarUploading}>
              {avatarUploading ? "Enviando..." : "Trocar foto"}
            </button>
            <p className="admin-hint" style={{ margin: "6px 0 0" }}>
              PNG, JPG, GIF ou WEBP, até 8MB.
            </p>
          </div>
        </div>
        {avatarError && <p className="auth-error">{avatarError}</p>}

        <div className="settings-divider" />

        <p className="settings-section-title">Status</p>
        <div className="status-picker">
          {STATUS_OPTIONS.map((opt) => (
            <button
              key={opt.value}
              type="button"
              className={"status-option" + (user?.status === opt.value ? " active" : "")}
              onClick={() => handleStatusChange(opt.value)}
              disabled={visibilitySaving}
            >
              <span className={"status-dot " + opt.dotClass} />
              <span>
                <strong>{opt.label}</strong>
                <small>{opt.hint}</small>
              </span>
            </button>
          ))}
        </div>
        {visibilityError && <p className="auth-error">{visibilityError}</p>}

        <div className="settings-divider" />

        <p className="settings-section-title">Dispositivos de áudio</p>
        <p className="admin-hint">A escolha aqui vale para a próxima vez que você entrar em uma call de voz.</p>

        {permissionError && <p className="auth-error">{permissionError}</p>}

        <div className="settings-field">
          <label className="settings-label">Microfone (entrada)</label>
          <select value={selectedInput} onChange={(e) => setSelectedInput(e.target.value)}>
            <option value="">Padrão do sistema</option>
            {inputDevices.map((d) => (
              <option key={d.deviceId} value={d.deviceId}>
                {d.label || `Microfone ${d.deviceId.slice(0, 6)}`}
              </option>
            ))}
          </select>
        </div>

        <div className="settings-field">
          <label className="settings-label">Alto-falante / fone (saída)</label>
          <select value={selectedOutput} onChange={(e) => setSelectedOutput(e.target.value)} disabled={!outputSupported}>
            <option value="">Padrão do sistema</option>
            {outputDevices.map((d) => (
              <option key={d.deviceId} value={d.deviceId}>
                {d.label || `Saída ${d.deviceId.slice(0, 6)}`}
              </option>
            ))}
          </select>
          {!outputSupported && (
            <p className="admin-hint" style={{ margin: 0 }}>
              Seu navegador não permite escolher a saída de áudio por código (comum no Firefox) — vai usar sempre o
              dispositivo padrão do sistema.
            </p>
          )}
        </div>

        <div className="settings-test-row">
          {!testing ? (
            <button type="button" onClick={startTest}>
              🎙️ Testar microfone (ouvir a si mesmo)
            </button>
          ) : (
            <button type="button" className="danger" onClick={stopTest}>
              Parar teste
            </button>
          )}
        </div>

        {testing && (
          <div className="settings-field">
            <div className="mic-meter-row">
              <span>Nível captado:</span>
              <div className="mic-meter-track">
                <div className="mic-meter-fill" style={{ width: `${level}%` }} />
              </div>
              <span className="mic-meter-value">{level}%</span>
            </div>
            <p className="admin-hint" style={{ margin: 0 }}>
              Fale algo — você deve ouvir sua própria voz (com um pequeno atraso) pelo dispositivo de saída
              escolhido, e a barra deve se mexer. Use fone de ouvido para evitar eco.
            </p>
          </div>
        )}

        {/* eslint-disable-next-line jsx-a11y/media-has-caption */}
        <audio ref={audioElRef} autoPlay />

        <label className="settings-checkbox-row">
          <input
            type="checkbox"
            checked={noiseSuppression}
            onChange={(e) => setNoiseSuppression(e.target.checked)}
          />
          Supressão de ruído no microfone
        </label>
        <p className="admin-hint" style={{ margin: "4px 0 0" }}>
          Filtra ruído de fundo (ventilador, teclado, etc). Desative se seu microfone soar
          estranho ou abafado com ela ligada (comum em microfones de estúdio/instrumentos).
        </p>

        <div className="settings-divider" />

        <p className="settings-section-title">Atalhos de teclado</p>
        <p className="admin-hint">Funcionam de qualquer tela do app, desde que você esteja numa call.</p>

        <div className="settings-field">
          <label className="settings-label">Mutar / desmutar microfone</label>
          <ShortcutRecorder value={muteShortcut} onChange={setMuteShortcutState} />
        </div>
        <div className="settings-field">
          <label className="settings-label">Ensurdecer / reativar áudio</label>
          <ShortcutRecorder value={deafenShortcut} onChange={setDeafenShortcutState} />
        </div>

        <div className="settings-divider" />

        <label className="settings-checkbox-row">
          <input type="checkbox" checked={soundEffects} onChange={(e) => setSoundEffects(e.target.checked)} />
          Tocar som quando alguém entrar ou sair de uma call
          <button type="button" className="link-btn" onClick={playJoinSound} style={{ marginLeft: "auto" }}>
            Testar som
          </button>
        </label>

        <div className="settings-actions">
          <button type="button" className="link-btn" onClick={onClose}>
            Cancelar
          </button>
          <button type="button" onClick={handleSave}>
            Salvar
          </button>
        </div>
      </div>
    </div>
  );
}
