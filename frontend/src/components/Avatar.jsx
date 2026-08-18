/**
 * Avatar reutilizavel: mostra a foto (GCS) se existir, senao cai nas iniciais do nome -
 * mesmo comportamento visual de antes, so que agora com foto de verdade quando disponivel.
 * `className` deve ser uma das classes ja estilizadas (ex: "voice-avatar", "user-bar-avatar").
 */
export default function Avatar({ name, url, className = "voice-avatar" }) {
  const initials = (name || "?").slice(0, 2).toUpperCase();
  if (url) {
    return <img src={url} alt={name || "avatar"} className={className} />;
  }
  return <span className={className}>{initials}</span>;
}
