import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [usernameOrEmail, setUsernameOrEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    try {
      await login(usernameOrEmail, password);
      navigate("/servers");
    } catch (err) {
      setError(err.response?.data?.error || "Falha ao entrar");
    }
  }

  return (
    <div className="auth-screen">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>Entrar</h1>
        <input placeholder="Usuário ou email" value={usernameOrEmail} onChange={(e) => setUsernameOrEmail(e.target.value)} />
        <input placeholder="Senha" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        {error && <p className="auth-error">{error}</p>}
        <button type="submit">Entrar</button>
        <p className="auth-note">Não há cadastro público — peça acesso ao administrador.</p>
      </form>
    </div>
  );
}
