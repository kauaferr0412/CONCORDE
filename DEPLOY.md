# Deploy na VPS (Docker)

Passo a passo pra rodar o Concorde inteiro (Postgres + backend + LiveKit + frontend) numa
VPS Ubuntu/Debian com IP público, sem depender de ngrok.

## 1. Instalar o Docker na VPS

Via SSH, como root (ou usuário com sudo):

```bash
curl -fsSL https://get.docker.com | sh
```

## 2. Abrir as portas no firewall

```bash
ufw allow 22/tcp    # SSH - nao esqueca, senao voce se tranca de fora
ufw allow 80/tcp     # HTTP (validacao do certificado HTTPS)
ufw allow 443/tcp    # HTTPS (site, API, chat, sinalizacao de voz)
ufw allow 7881/tcp   # LiveKit - fallback TCP do WebRTC
ufw allow 50000:50100/udp  # LiveKit - midia (audio/video)
ufw enable
```

Se a VPS estiver atrás de um firewall do provedor (ex: "Cloud Firewall" da DigitalOcean/Oracle/etc,
separado do `ufw`), libere as mesmas portas lá também — é comum esquecer esse e o `ufw` não ser
o problema.

## 3. Levar o código pra VPS

```bash
git clone <url-do-seu-repo> concorde
cd concorde
```

(ou `scp -r` a pasta do projeto, se não estiver num repo git ainda)

## 4. Configurar as variáveis de produção

```bash
cp .env.prod.example .env.prod
nano .env.prod
```

Preencha pelo menos:
- `DOMAIN`: se não tiver domínio próprio, pegue o IP público da VPS (`curl ifconfig.me`) e monte
  `IP-com-hifen.sslip.io` — ex. IP `203.0.113.7` vira `DOMAIN=203-0-113-7.sslip.io`. Esse domínio
  já resolve pro seu IP automaticamente, de graça, sem cadastro em lugar nenhum.
- `DB_PASSWORD`, `ADMIN_PASSWORD`: senhas fortes, à sua escolha.
- `LIVEKIT_API_KEY` / `LIVEKIT_API_SECRET`: invente valores próprios (o secret precisa ter
  32+ caracteres) — **não** reaproveite o `devkey`/`devsecret...` do `docker-compose.yml` de dev.

## 5. Subir tudo

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

Primeira subida demora um pouco (build do backend com Maven + build do frontend com npm).
Acompanhe os logs:

```bash
docker compose -f docker-compose.prod.yml logs -f
```

## 6. Testar

Abra `https://SEU_DOMINIO` no navegador (ex: `https://203-0-113-7.sslip.io`). O Caddy busca o
certificado HTTPS sozinho na primeira requisição — pode levar alguns segundos a mais na primeira
vez. Faça login com `admin` / a senha que você colocou em `ADMIN_PASSWORD`, crie um servidor e
teste o canal de voz. Manda o link do domínio pros seus amigos — não precisa mais de ngrok nem
de deixar seu PC ligado.

## Atualizando depois de mudar código

```bash
git pull
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

## Sobre as portas de voz (LiveKit)

- `443` (HTTPS/Caddy): site, API, chat e a *sinalização* da chamada de voz — tudo nessa porta só.
- `50000-50100/udp` e `7881/tcp` (LiveKit, direto, sem passar pelo Caddy): a *mídia* de
  áudio/vídeo em si. É por isso que essas portas precisam estar abertas separadamente — diferente
  do que acontecia com o ngrok, aqui dá pra abrir UDP de verdade porque é sua própria VPS.
