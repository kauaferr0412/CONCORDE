# --- build: gera o bundle estatico (frontend/dist) ---
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# --- gateway: Caddy servindo o estatico + fazendo proxy pro backend e pro LiveKit,
# tudo atras do MESMO dominio/porta 443 (com HTTPS automatico via Let's Encrypt).
# Ver Caddyfile pras rotas.
FROM caddy:2-alpine
COPY --from=build /app/dist /srv
COPY Caddyfile /etc/caddy/Caddyfile
