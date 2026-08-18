import { useEffect, useState } from "react";
import api from "../api/client";
import { subscribeToPresence } from "../ws/chatSocket";
import { ChevronsLeftIcon, ChevronsRightIcon, UsersIcon } from "./icons.jsx";
import Avatar from "./Avatar.jsx";

/**
 * Lista de TODOS os membros do servidor (nao so' quem esta numa call de voz - isso ja e'
 * o "CONECTADOS AGORA" do ChannelSidebar), com uma bolinha verde/cinza de online/offline.
 * "Online" aqui = tem o app aberto em algum dispositivo, ver OnlinePresenceService no
 * backend - e o proprio usuario pode escolher aparecer offline mesmo estando conectado
 * (Configuracoes > Aparecer offline).
 */
export default function MemberList({ serverId, stompClient, stompConnected }) {
  const [members, setMembers] = useState([]);
  const [collapsed, setCollapsed] = useState(() => localStorage.getItem("memberListCollapsed") === "true");

  function toggleCollapsed() {
    setCollapsed((prev) => {
      const next = !prev;
      localStorage.setItem("memberListCollapsed", String(next));
      return next;
    });
  }

  useEffect(() => {
    if (!serverId) return;
    let cancelled = false;
    api.get(`/api/servers/${serverId}/members`).then(({ data }) => {
      if (!cancelled) setMembers(data);
    });
    return () => {
      cancelled = true;
    };
  }, [serverId]);

  // Atualiza so' o membro que mudou (nao precisa buscar a lista inteira de novo a cada
  // conexao/desconexao de qualquer pessoa no app inteiro).
  useEffect(() => {
    if (!stompClient || !stompConnected) return;
    const sub = subscribeToPresence(stompClient, ({ userId, online }) => {
      setMembers((prev) => {
        if (!prev.some((m) => m.userId === userId)) return prev; // nao e' membro desse servidor
        return prev
          .map((m) => (m.userId === userId ? { ...m, online } : m))
          .sort((a, b) => (a.online !== b.online ? (a.online ? -1 : 1) : a.username.localeCompare(b.username)));
      });
    });
    return () => sub.unsubscribe();
  }, [stompClient, stompConnected]);

  if (!serverId) return null;

  const online = members.filter((m) => m.online);
  const offline = members.filter((m) => !m.online);

  return (
    <div className={"member-list" + (collapsed ? " collapsed" : "")}>
      <div className="member-list-header">
        {!collapsed && <span className="channel-group-title">MEMBROS — {members.length}</span>}
        <button
          className="icon-btn"
          onClick={toggleCollapsed}
          title={collapsed ? "Abrir lista de membros" : "Fechar lista de membros"}
        >
          {collapsed ? <ChevronsLeftIcon /> : <ChevronsRightIcon />}
        </button>
      </div>

      {!collapsed && (
        <div className="member-list-body">
          {online.length > 0 && (
            <>
              <p className="channel-group-title member-list-group">ONLINE — {online.length}</p>
              {online.map((m) => (
                <MemberRow key={m.userId} member={m} />
              ))}
            </>
          )}
          {offline.length > 0 && (
            <>
              <p className="channel-group-title member-list-group">OFFLINE — {offline.length}</p>
              {offline.map((m) => (
                <MemberRow key={m.userId} member={m} />
              ))}
            </>
          )}
          {members.length === 0 && (
            <div className="member-list-empty">
              <UsersIcon size={22} />
              <p>Nenhum membro ainda</p>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function MemberRow({ member }) {
  return (
    <div className={"member-row" + (member.online ? "" : " offline")}>
      <div className="member-avatar-wrap">
        <Avatar name={member.username} url={member.avatarUrl} className="voice-avatar small" />
        <span className={"status-dot" + (member.online ? " online" : " offline")} />
      </div>
      <span className="member-row-name">{member.username}</span>
    </div>
  );
}
