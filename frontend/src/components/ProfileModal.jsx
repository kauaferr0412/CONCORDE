import { useEffect, useState } from "react";
import api from "../api/client";
import { useAuth } from "../context/AuthContext.jsx";
import Avatar from "./Avatar.jsx";

const STATUS_LABEL = { ONLINE: "Online", AWAY: "Ausente", DND: "Não perturbe", OFFLINE: "Offline" };
const STATUS_DOT_CLASS = { ONLINE: "online", AWAY: "away", DND: "dnd", OFFLINE: "offline" };

/**
 * Cartao de perfil de QUALQUER usuario (clicavel a partir do chat, lista de membros, canal
 * de voz - ver useProfile/ProfileContext). Quando e' o seu proprio perfil, vira editavel
 * (apelido + bio) na hora, sem precisar de outra tela.
 */
export default function ProfileModal({ userId, onClose }) {
  const { user: me, updateUser } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loadError, setLoadError] = useState("");
  const [editing, setEditing] = useState(false);
  const [nickname, setNickname] = useState("");
  const [bio, setBio] = useState("");
  const [saveError, setSaveError] = useState("");
  const [saving, setSaving] = useState(false);

  const isMe = me?.id === userId;

  useEffect(() => {
    setProfile(null);
    setLoadError("");
    setEditing(false);
    setSaveError("");
    api
      .get(`/api/users/${userId}/profile`)
      .then(({ data }) => {
        setProfile(data);
        setNickname(data.nickname || "");
        setBio(data.bio || "");
      })
      .catch((err) => setLoadError(err.response?.data?.error || "Não foi possível carregar esse perfil"));
  }, [userId]);

  async function handleSave(e) {
    e.preventDefault();
    setSaveError("");
    setSaving(true);
    try {
      const { data } = await api.put("/api/users/me/profile", { nickname: nickname.trim(), bio: bio.trim() });
      setProfile((prev) => ({ ...prev, nickname: data.nickname, bio: data.bio }));
      updateUser({ nickname: data.nickname, bio: data.bio });
      setEditing(false);
    } catch (err) {
      setSaveError(err.response?.data?.error || "Falha ao salvar perfil");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal profile-modal" onClick={(e) => e.stopPropagation()}>
        {loadError && <p className="auth-error">{loadError}</p>}

        {profile && !editing && (
          <>
            <div className="profile-header">
              <div className="member-avatar-wrap profile-avatar-wrap">
                <Avatar name={profile.username} url={profile.avatarUrl} className="profile-avatar" />
                <span className={"status-dot " + STATUS_DOT_CLASS[profile.status]} title={STATUS_LABEL[profile.status]} />
              </div>
              <div className="profile-heading">
                <h2>{profile.nickname || profile.username}</h2>
                <p className="profile-username">@{profile.username}</p>
              </div>
            </div>

            <p className="admin-hint">
              {STATUS_LABEL[profile.status]} · Membro desde {new Date(profile.createdAt).toLocaleDateString()}
            </p>

            {profile.bio && <p className="profile-bio">{profile.bio}</p>}

            <div className="settings-actions">
              <button type="button" className="link-btn" onClick={onClose}>
                Fechar
              </button>
              {isMe && (
                <button type="button" onClick={() => setEditing(true)}>
                  Editar perfil
                </button>
              )}
            </div>
          </>
        )}

        {profile && editing && (
          <form onSubmit={handleSave}>
            <h2>Editar perfil</h2>

            <div className="settings-field">
              <label className="settings-label">Apelido</label>
              <input
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                placeholder={profile.username}
                maxLength={32}
              />
              <p className="admin-hint" style={{ margin: 0 }}>
                Aparece no seu perfil no lugar de "{profile.username}". Em branco = usa o nome de usuário.
              </p>
            </div>

            <div className="settings-field">
              <label className="settings-label">Sobre mim</label>
              <textarea
                value={bio}
                onChange={(e) => setBio(e.target.value)}
                maxLength={190}
                rows={3}
                placeholder="Conte um pouco sobre você..."
              />
            </div>

            {saveError && <p className="auth-error">{saveError}</p>}

            <div className="settings-actions">
              <button type="button" className="link-btn" onClick={() => setEditing(false)} disabled={saving}>
                Cancelar
              </button>
              <button type="submit" disabled={saving}>
                {saving ? "Salvando..." : "Salvar"}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
