import axios from "axios";

// Vazio = usa a MESMA origem que carregou a pagina (ex: http://localhost:5173 ou
// https://xxxx.ngrok-free.dev) e deixa o proxy do Vite (ver vite.config.js) encaminhar
// pro backend de verdade. So' defina VITE_API_URL se quiser apontar pra um backend
// especifico, fora do fluxo normal.
const API_URL = import.meta.env.VITE_API_URL || "";

export const api = axios.create({
  baseURL: API_URL,
});

api.interceptors.request.use((config) => {
  // sessionStorage (isolado por aba) - ver AuthContext.jsx pro motivo de nao ser localStorage.
  const token = sessionStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // Quando a API estiver atras de um tunel ngrok gratuito, o ngrok mostra uma pagina de
  // aviso (HTML) antes de deixar passar a requisicao de verdade - isso quebraria toda
  // chamada da API (o axios receberia HTML em vez do JSON esperado). Esse header manda
  // o ngrok pular esse aviso so' pras chamadas que o proprio app faz. Inofensivo quando
  // nao esta atras do ngrok (o backend direto simplesmente ignora um header que nao conhece).
  config.headers["ngrok-skip-browser-warning"] = "true";
  return config;
});

export default api;
