import { createContext, useContext, useState } from "react";
import ProfileModal from "../components/ProfileModal.jsx";

const ProfileContext = createContext(null);

/**
 * Deixa QUALQUER componente abrir o cartao de perfil de um usuario so' chamando
 * openProfile(userId) - sem precisar passar callback por prop por toda a arvore
 * (chat, lista de membros, canal de voz todos usam isso independentemente).
 */
export function ProfileProvider({ children }) {
  const [openUserId, setOpenUserId] = useState(null);

  return (
    <ProfileContext.Provider value={{ openProfile: setOpenUserId }}>
      {children}
      {openUserId && <ProfileModal userId={openUserId} onClose={() => setOpenUserId(null)} />}
    </ProfileContext.Provider>
  );
}

export function useProfile() {
  return useContext(ProfileContext);
}
