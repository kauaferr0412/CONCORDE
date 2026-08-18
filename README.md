# Concorde — clone do Discord (React + Spring Boot + LiveKit)

Plataforma de chat com texto em tempo real, canais de voz/vídeo, compartilhamento de
tela com áudio, empacotável como app desktop.

## Arquitetura

```
frontend/   React (Vite) — UI, chat texto via WebSocket/STOMP, voz/vídeo via LiveKit
backend/    Spring Boot  — auth (JWT), servers/channels/mensagens, tokens do LiveKit
docker-compose.yml  — sobe o LiveKit (servidor de voz/vídeo/tela via WebRTC)
```

O Spring Boot **não carrega áudio/vídeo** — ele só autentica os usuários e decide quem
pode entrar em qual canal. A mídia de voz/vídeo/tela trafega direto entre o app e o
LiveKit (um SFU WebRTC), que roda separado via Docker.

## Como rodar em desenvolvimento

### 1. Servidor de voz (LiveKit)

Precisa do Docker Desktop instalado.

```bash
docker compose up -d
```

Isso sobe o LiveKit em `ws://localhost:7880` com `devkey` / `devsecret...` (já
configurados em `backend/src/main/resources/application.yml`). Troque essas chaves
antes de ir para produção.

### 2. Backend (Spring Boot)

Precisa de JDK 17+ e Maven (ou use o `mvnw` se você gerar o wrapper).

```bash
cd backend
mvn spring-boot:run
```

Sobe em `http://localhost:8080`. Usa H2 em arquivo (`backend/data/`) por padrão —
zero configuração de banco. Para produção, troque para Postgres no `application.yml`
(o bloco já vem comentado, pronto para descomentar).

### 3. Frontend (React)

Precisa de Node 18+.

```bash
cd frontend
npm install
npm run dev
```

Abre em `http://localhost:5173`.

### 4. Testar o fluxo completo

1. Crie uma conta em `/register`.
2. Crie um servidor (botão "+" na barra lateral esquerda) — já vem com um canal de
   texto "geral" e um canal de voz "Geral".
3. Mande mensagens no canal de texto — chegam em tempo real via WebSocket.
4. Entre no canal de voz, teste mutar/desmutar e "Compartilhar tela (com áudio)".
5. Não existe cadastro público nem convite por link — o admin cria as contas e libera
   o acesso de cada usuário a um servidor pelo Painel do administrador.

## Empacotar como app desktop (Electron)

```bash
cd frontend
npm run build          # gera frontend/dist
npm run electron        # abre a janela desktop apontando pro build/dev server
```

O `electron/main.cjs` já registra o protocolo `concorde://` no sistema, pronto pra uso
como deep link caso o app volte a precisar disso no futuro. Para gerar o instalador
(.exe/.dmg/.AppImage) de verdade, adicione `electron-builder` como próximo passo.

## O que falta para produção

- Trocar H2 por Postgres.
- Regras de permissão/cargo por canal (hoje todo membro do servidor vê todos os canais).
- Presença online/offline e "digitando..." via WebSocket.
- Push notifications e upload de arquivos/imagens no chat.
- `electron-builder` para gerar instaladores assinados com auto-update.
- LiveKit em produção: usar LiveKit Cloud ou hospedar com TURN configurado (essencial
  para funcionar atrás de NAT/firewall corporativo).
