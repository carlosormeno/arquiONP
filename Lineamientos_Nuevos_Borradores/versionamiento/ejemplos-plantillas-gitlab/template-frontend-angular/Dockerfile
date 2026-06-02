# EJEMPLO DE REFERENCIA — LIN-VER-001 Anexo F / LIN-K8S-001 §5.4
# Dockerfile multi-stage para Angular SPA — imagen institucional ONP
# Plataforma/Infraestructura es responsable de mantener este archivo en el proyecto GitLab real.

# ── Etapa 1: Build Angular ─────────────────────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci --prefer-offline

COPY . .
# El argumento de configuración selecciona el environment Angular correcto.
# El pipeline CI pasa --build-arg CONFIGURATION=production|staging según rama.
ARG CONFIGURATION=production
RUN npm run build -- --configuration=${CONFIGURATION} --output-path=dist/app

# ── Etapa 2: Runtime nginx ─────────────────────────────────────────────────────
FROM nginxinc/nginx-unprivileged:1.27-alpine

LABEL org.opencontainers.image.title="frontend-nombre-sistema" \
      org.opencontainers.image.vendor="ONP" \
      org.opencontainers.image.base.name="nginxinc/nginx-unprivileged:1.27-alpine"

# Configuración nginx institucional (SPA routing + gzip + seguridad)
# Ver LIN-K8S-001 Anexo D para el detalle completo
COPY nginx.conf /etc/nginx/nginx.conf

COPY --from=build /app/dist/app /usr/share/nginx/html

EXPOSE 8080
