import { useState } from "react";

export default function CreateServerModal({ onClose, onCreate }) {
  const [name, setName] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    if (!name.trim()) return;
    setSubmitting(true);
    setError("");
    try {
      await onCreate(name.trim());
      onClose();
    } catch (err) {
      setError(err.response?.data?.error || "Não foi possível criar o servidor");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <form className="modal" onClick={(e) => e.stopPropagation()} onSubmit={handleSubmit}>
        <h2>Criar servidor</h2>
        <p className="admin-hint">
          Um servidor novo já vem com um canal de texto "geral" e um canal de voz "Geral". Depois use o painel de
          administração para liberar o acesso de usuários a ele.
        </p>

        <label className="settings-label">Nome do servidor</label>
        <input
          autoFocus
          placeholder="Ex: Equipe de Produto"
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
            {submitting ? "Criando..." : "Criar servidor"}
          </button>
        </div>
      </form>
    </div>
  );
}
