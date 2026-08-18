import { createContext, useContext, useEffect, useState } from "react";
import api from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  // sessionStorage (nao localStorage) de proposito: localStorage e' compartilhado entre
  // TODAS as abas/janelas do mesmo navegador (mesmo perfil), entao logar com uma conta
  // numa aba trocava o token de qualquer outra aba aberta na hora - inclusive no meio de
  // uma call de voz ja conectada, fazendo duas pessoas conectarem com a MESMA identidade
  // no LiveKit (a call so mostrava 1 participante). sessionStorage isola cada aba de
  // verdade - abrir uma aba nova pede login de novo, mas evita esse tipo de contaminacao.
  const [token, setToken] = useState(() => sessionStorage.getItem("token"));
  const [user, setUser] = useState(() => {
    const raw = sessionStorage.getItem("user");
    return raw ? JSON.parse(raw) : null;
  });

  useEffect(() => {
    if (token) sessionStorage.setItem("token", token);
    else sessionStorage.removeItem("token");
  }, [token]);

  useEffect(() => {
    if (user) sessionStorage.setItem("user", JSON.stringify(user));
    else sessionStorage.removeItem("user");
  }, [user]);

  async function login(usernameOrEmail, password) {
    const { data } = await api.post("/api/auth/login", { usernameOrEmail, password });
    setToken(data.token);
    setUser(data.user);
  }

  function logout() {
    setToken(null);
    setUser(null);
  }

  /** Usado apos trocar a foto de perfil (ver SettingsModal), pra refletir na UI toda sem precisar relogar. */
  function updateUser(patch) {
    setUser((prev) => (prev ? { ...prev, ...patch } : prev));
  }

  const isAdmin = user?.role === "ADMIN";

  return (
    <AuthContext.Provider value={{ token, user, login, logout, updateUser, isAuthenticated: !!token, isAdmin }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
