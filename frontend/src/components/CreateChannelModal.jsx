import { useState } from "react";

export default function CreateChannelModal({ type, onClose, onCreate }) {
  const [name, setName] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");
  const isVoice = type === "VOICE";

  async function handleSubmit(e) {
    e.preventDefault();
    if (!name.trim()) return;
    setSubmitting(true);
    setError("");
    try {
      await onCreate(name.trim());
      onClose();
    } catch (err) {
      setError(err.response?.data?.error || "Não foi possível criar o canal");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <form className="modal" onClick={(e) => e.stopPropagation()} onSubmit={handleSubmit}>
        <h2>{isVoice ? "Criar canal de voz" : "Criar canal de texto"}</h2>
        <p className="admin-hint">
          {isVoice
            ? "Canais de voz permitem chamadas com áudio, vídeo e compartilhamento de tela."
            : "Canais de texto são para conversas escritas em tempo real."}
        </p>

        <label className="settings-label">Nome do canal</label>
        <input
          autoFocus
          placeholder={isVoice ? "Ex: Reunião" : "Ex: geral"}
          value={name}
          onChange={(e) => setName(e.target.value)}
          maxLength={100}
        />

        {error && <p className="auth-error">{error}</p>}

        <div className="settings-actions">
          <button type="button" className="link-btn" onClick={onClose}>
            Cancelar
          </button>
          <button type="submit" disabled={!name.trim() || submitting}>
            {submitting ? "Criando..." : "Criar canal"}
          </button>
        </div>
      </form>
    </div>
  );
}
